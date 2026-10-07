package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.PriceNotificationEntity
import com.example.data.local.TrackedGameEntity
import com.example.data.repository.GameDealsRepository
import com.example.model.GameDeal
import com.example.model.GameDetails
import com.example.model.GameSearchResult
import com.example.model.SortOption
import com.example.model.StoreFilter
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DealsUiState(
    val deals: List<GameDeal> = emptyList(),
    val freeDeals: List<GameDeal> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val filter: StoreFilter = StoreFilter.ALL,
    val sort: SortOption = SortOption.SAVINGS
)

data class SearchUiState(
    val query: String = "",
    val resolvedQuery: String? = null,
    val matchedAlias: String? = null,
    val results: List<GameSearchResult> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class GameDealsViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val repository = GameDealsRepository(
        trackedGameDao = database.trackedGameDao(),
        notificationDao = database.priceNotificationDao()
    )

    private val _dealsUiState = MutableStateFlow(DealsUiState())
    val dealsUiState: StateFlow<DealsUiState> = _dealsUiState.asStateFlow()

    private val _searchUiState = MutableStateFlow(SearchUiState())
    val searchUiState: StateFlow<SearchUiState> = _searchUiState.asStateFlow()

    private val _selectedGameDetails = MutableStateFlow<GameDetails?>(null)
    val selectedGameDetails: StateFlow<GameDetails?> = _selectedGameDetails.asStateFlow()

    private val _isLoadingDetails = MutableStateFlow(false)
    val isLoadingDetails: StateFlow<Boolean> = _isLoadingDetails.asStateFlow()

    private val _isRefreshingPrices = MutableStateFlow(false)
    val isRefreshingPrices: StateFlow<Boolean> = _isRefreshingPrices.asStateFlow()

    private val _refreshFeedback = MutableStateFlow<String?>(null)
    val refreshFeedback: StateFlow<String?> = _refreshFeedback.asStateFlow()

    val trackedGames: StateFlow<List<TrackedGameEntity>> = repository.getAllTrackedGames()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val notifications: StateFlow<List<PriceNotificationEntity>> = repository.getNotifications()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val unreadAlertsCount: StateFlow<Int> = repository.getUnreadCount()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    private var searchJob: Job? = null

    init {
        fetchDeals()
    }

    fun setStoreFilter(filter: StoreFilter) {
        if (_dealsUiState.value.filter == filter) return
        _dealsUiState.update { it.copy(filter = filter) }
        fetchDeals()
    }

    fun setSortOption(sort: SortOption) {
        if (_dealsUiState.value.sort == sort) return
        _dealsUiState.update { it.copy(sort = sort) }
        fetchDeals()
    }

    /**
     * Alias for fetchDeals to match prompt specification
     */
    fun fetchDeals(isPullToRefresh: Boolean = false) {
        loadDeals(isPullToRefresh)
    }

    fun loadDeals(isPullToRefresh: Boolean = false) {
        viewModelScope.launch {
            _dealsUiState.update {
                if (isPullToRefresh) it.copy(isRefreshing = true, error = null)
                else it.copy(isLoading = true, error = null)
            }
            val currentState = _dealsUiState.value

            // Also check for 100% free / giveaway games
            val freeDealsFound = repository.getFreeDeals(currentState.filter)

            val result = repository.getDeals(
                storeFilter = currentState.filter,
                sortBy = currentState.sort
            )

            result.fold(
                onSuccess = { rawDeals ->
                    // Rule 1 & 3: Ensure descending discount percentage sorting (b.savings - a.savings)
                    val sortedDeals = if (currentState.sort == SortOption.SAVINGS) {
                        repository.sortDealsBySavings(rawDeals)
                    } else {
                        rawDeals
                    }

                    // Rule 2: %100 discount (completely free) games showcase
                    val freeFromList = rawDeals.filter { it.isFree || it.savingsPercent >= 100 }
                    val combinedFree = (freeFromList + freeDealsFound).distinctBy { it.gameId }

                    _dealsUiState.update {
                        it.copy(
                            deals = sortedDeals,
                            freeDeals = combinedFree,
                            isLoading = false,
                            isRefreshing = false,
                            error = if (sortedDeals.isEmpty()) "Gösterilecek indirim bulunamadı." else null
                        )
                    }
                },
                onFailure = { error ->
                    _dealsUiState.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            error = "İndirimler yüklenirken hata oluştu: ${error.localizedMessage ?: "Bağlantı hatası"}"
                        )
                    }
                }
            )
        }
    }

    fun onSearchQueryChanged(query: String) {
        val normalized = com.example.util.SearchNormalizer.normalize(query)
        _searchUiState.update {
            it.copy(
                query = query,
                resolvedQuery = if (normalized.matchedAlias != null) normalized.resolvedQuery else null,
                matchedAlias = normalized.matchedAlias
            )
        }
        searchJob?.cancel()

        if (normalized.sanitizedQuery.length < 2 || normalized.sanitizedQuery.none { it.isLetterOrDigit() }) {
            _searchUiState.update { it.copy(results = emptyList(), isLoading = false, error = null) }
            return
        }

        searchJob = viewModelScope.launch {
            delay(350) // debounce
            _searchUiState.update { it.copy(isLoading = true, error = null) }
            val result = repository.searchGames(query)
            result.fold(
                onSuccess = { results ->
                    _searchUiState.update {
                        it.copy(
                            results = results,
                            isLoading = false,
                            error = if (results.isEmpty()) {
                                if (normalized.matchedAlias != null) {
                                    "'${normalized.matchedAlias}' ($query) ile eşleşen oyun bulunamadı."
                                } else {
                                    "'$query' ile eşleşen oyun bulunamadı."
                                }
                            } else null
                        )
                    }
                },
                onFailure = { err ->
                    _searchUiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Arama sırasında hata oluştu: ${err.localizedMessage ?: "Hata"}"
                        )
                    }
                }
            )
        }
    }

    fun toggleTrackDeal(deal: GameDeal) {
        viewModelScope.launch {
            val isCurrentlyTracked = trackedGames.value.any { it.gameId == deal.gameId }
            if (isCurrentlyTracked) {
                repository.untrackGame(deal.gameId)
            } else {
                val entity = TrackedGameEntity(
                    gameId = deal.gameId,
                    title = deal.title,
                    thumbUrl = deal.thumbUrl,
                    steamAppId = deal.steamAppId,
                    lastPrice = deal.salePrice,
                    retailPrice = deal.normalPrice,
                    lowestPriceEver = null,
                    savingsPercent = deal.savingsPercent,
                    storeName = deal.storeType.displayName,
                    dealId = deal.dealId,
                    targetPrice = deal.salePrice, // defaults to current sale price or lower
                    isDiscounted = deal.savingsPercent > 0
                )
                repository.trackGame(entity)
            }
        }
    }

    fun trackSearchResult(result: GameSearchResult) {
        viewModelScope.launch {
            val isCurrentlyTracked = trackedGames.value.any { it.gameId == result.gameId }
            if (isCurrentlyTracked) {
                repository.untrackGame(result.gameId)
            } else {
                val entity = TrackedGameEntity(
                    gameId = result.gameId,
                    title = result.title,
                    thumbUrl = result.thumbUrl,
                    steamAppId = result.steamAppId,
                    lastPrice = result.cheapestPrice,
                    retailPrice = result.cheapestPrice,
                    lowestPriceEver = null,
                    savingsPercent = 0,
                    storeName = "Steam / Epic",
                    dealId = result.cheapestDealId,
                    targetPrice = result.cheapestPrice,
                    isDiscounted = false
                )
                repository.trackGame(entity)
            }
        }
    }

    fun untrackGame(gameId: String) {
        viewModelScope.launch {
            repository.untrackGame(gameId)
        }
    }

    fun updateTargetPrice(gameId: String, targetPrice: Double?) {
        viewModelScope.launch {
            repository.updateTargetPrice(gameId, targetPrice)
        }
    }

    fun refreshTrackedPrices() {
        viewModelScope.launch {
            _isRefreshingPrices.value = true
            val result = repository.checkAndUpdateTrackedGamePrices()
            _isRefreshingPrices.value = false

            result.fold(
                onSuccess = { dropCount ->
                    _refreshFeedback.value = if (dropCount > 0) {
                        "🎉 $dropCount oyunda yeni fiyat düşüşü tespit edildi! Bildirimlere eklendi."
                    } else {
                        "Tüm takip edilen fiyatlar güncel. Yeni indirim bulunamadı."
                    }
                },
                onFailure = {
                    _refreshFeedback.value = "Fiyatlar güncellenirken bağlantı hatası oluştu."
                }
            )
        }
    }

    fun clearRefreshFeedback() {
        _refreshFeedback.value = null
    }

    fun openGameDetails(gameId: String) {
        viewModelScope.launch {
            _isLoadingDetails.value = true
            _selectedGameDetails.value = null
            val result = repository.getGameDetails(gameId)
            _isLoadingDetails.value = false
            result.onSuccess { details ->
                _selectedGameDetails.value = details
            }
        }
    }

    fun closeGameDetails() {
        _selectedGameDetails.value = null
    }

    fun trackCustomEntity(entity: TrackedGameEntity) {
        viewModelScope.launch {
            repository.trackGame(entity)
        }
    }

    fun markAlertRead(id: Long) {
        viewModelScope.launch {
            repository.markNotificationAsRead(id)
        }
    }

    fun markAllAlertsRead() {
        viewModelScope.launch {
            repository.markAllNotificationsAsRead()
        }
    }

    fun clearAllAlerts() {
        viewModelScope.launch {
            repository.clearNotifications()
        }
    }
}
