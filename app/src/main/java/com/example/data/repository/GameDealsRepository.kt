package com.example.data.repository

import com.example.data.api.ApiClient
import com.example.data.api.CheapSharkApiService
import com.example.data.local.PriceNotificationDao
import com.example.data.local.PriceNotificationEntity
import com.example.data.local.TrackedGameDao
import com.example.data.local.TrackedGameEntity
import com.example.model.GameDeal
import com.example.model.GameDetails
import com.example.model.GameSearchResult
import com.example.model.SortOption
import com.example.model.StoreDealInfo
import com.example.model.StoreFilter
import com.example.model.StoreType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import kotlin.math.roundToInt

class GameDealsRepository(
    private val apiService: CheapSharkApiService = ApiClient.apiService,
    private val trackedGameDao: TrackedGameDao,
    private val notificationDao: PriceNotificationDao
) {

    /**
     * URL encodes string similar to encodeURIComponent() in JavaScript
     * Handles spaces (%20 instead of +) and special characters safely
     */
    private fun safeUrlEncode(input: String?): String? {
        if (input.isNullOrBlank()) return null
        return try {
            URLEncoder.encode(input.trim(), StandardCharsets.UTF_8.name())
                .replace("+", "%20")
        } catch (_: Exception) {
            input.trim()
        }
    }

    suspend fun getDeals(
        storeFilter: StoreFilter,
        sortBy: SortOption,
        page: Int = 0,
        title: String? = null
    ): Result<List<GameDeal>> = withContext(Dispatchers.IO) {
        runCatching {
            val encodedTitle = title?.let { safeUrlEncode(it) }
            val response = apiService.getDeals(
                storeID = storeFilter.storeIds,
                pageNumber = page,
                pageSize = 60,
                sortBy = sortBy.apiValue,
                desc = if (sortBy == SortOption.PRICE_LOW) 0 else 1,
                onSale = 1,
                title = encodedTitle
            )
            val mapped = response.map { dto ->
                GameDeal(
                    dealId = dto.dealID,
                    gameId = dto.gameID,
                    title = dto.title,
                    storeType = StoreType.fromId(dto.storeID),
                    salePrice = dto.salePrice.toDoubleOrNull() ?: 0.0,
                    normalPrice = dto.normalPrice.toDoubleOrNull() ?: 0.0,
                    savingsPercent = (dto.savings.toDoubleOrNull() ?: 0.0).roundToInt(),
                    metacriticScore = dto.metacriticScore?.toIntOrNull(),
                    steamRatingText = dto.steamRatingText,
                    steamRatingPercent = dto.steamRatingPercent?.toIntOrNull(),
                    steamRatingCount = dto.steamRatingCount?.toIntOrNull(),
                    steamAppId = dto.steamAppID,
                    dealRating = dto.dealRating?.toDoubleOrNull(),
                    thumbUrl = dto.thumb ?: ""
                )
            }

            // Apply sorting logic (Descending Savings is default per user prompt)
            when (sortBy) {
                SortOption.SAVINGS -> sortDealsBySavings(mapped)
                else -> sortAndFilterDealsByPopularity(mapped, sortBy)
            }
        }
    }

    /**
     * Rule 1 & 3:
     * Sorts deals strictly by savings percentage descending (b.savings - a.savings).
     * %100, %90, %80 ... %10. Lower discounts remain at the bottom.
     * Tie-breaker uses deal rating, metacritic score, and review count.
     */
    fun sortDealsBySavings(deals: List<GameDeal>): List<GameDeal> {
        return deals.sortedWith { a, b ->
            // Descending by discount percentage
            if (a.savingsPercent != b.savingsPercent) {
                b.savingsPercent.compareTo(a.savingsPercent)
            } else {
                val aDeal = a.dealRating ?: 0.0
                val bDeal = b.dealRating ?: 0.0
                if (aDeal != bDeal) {
                    bDeal.compareTo(aDeal)
                } else {
                    val aMeta = a.metacriticScore ?: 0
                    val bMeta = b.metacriticScore ?: 0
                    if (aMeta != bMeta) {
                        bMeta.compareTo(aMeta)
                    } else {
                        val aReviews = a.steamRatingCount ?: 0
                        val bReviews = b.steamRatingCount ?: 0
                        bReviews.compareTo(aReviews)
                    }
                }
            }
        }
    }

    /**
     * Rule 2:
     * Fetches %100 discount (completely free) games on Steam and Epic Games.
     */
    suspend fun getFreeDeals(storeFilter: StoreFilter = StoreFilter.ALL): List<GameDeal> = withContext(Dispatchers.IO) {
        runCatching {
            val response = apiService.getDeals(
                storeID = storeFilter.storeIds,
                pageNumber = 0,
                pageSize = 20,
                sortBy = "Savings",
                upperPrice = 0.0,
                desc = 1,
                onSale = 1
            )
            response.map { dto ->
                GameDeal(
                    dealId = dto.dealID,
                    gameId = dto.gameID,
                    title = dto.title,
                    storeType = StoreType.fromId(dto.storeID),
                    salePrice = dto.salePrice.toDoubleOrNull() ?: 0.0,
                    normalPrice = dto.normalPrice.toDoubleOrNull() ?: 0.0,
                    savingsPercent = (dto.savings.toDoubleOrNull() ?: 100.0).roundToInt(),
                    metacriticScore = dto.metacriticScore?.toIntOrNull(),
                    steamRatingText = dto.steamRatingText,
                    steamRatingPercent = dto.steamRatingPercent?.toIntOrNull(),
                    steamRatingCount = dto.steamRatingCount?.toIntOrNull(),
                    steamAppId = dto.steamAppID,
                    dealRating = dto.dealRating?.toDoubleOrNull(),
                    thumbUrl = dto.thumb ?: ""
                )
            }.filter { it.isFree || it.savingsPercent >= 100 }
        }.getOrDefault(emptyList())
    }

    /**
     * Popularity sorting for Metacritic and Deal Rating options
     */
    fun sortAndFilterDealsByPopularity(deals: List<GameDeal>, sortBy: SortOption): List<GameDeal> {
        return deals.sortedWith { a, b ->
            val aIsUnknown = isUnknownShovelware(a)
            val bIsUnknown = isUnknownShovelware(b)

            // Known games always come before unknown shovelware for popularity options
            if (aIsUnknown != bIsUnknown) {
                return@sortedWith if (aIsUnknown) 1 else -1
            }

            when (sortBy) {
                SortOption.METACRITIC -> {
                    val aMeta = a.metacriticScore ?: 0
                    val bMeta = b.metacriticScore ?: 0
                    if (aMeta != bMeta) {
                        bMeta.compareTo(aMeta)
                    } else {
                        val aDeal = a.dealRating ?: 0.0
                        val bDeal = b.dealRating ?: 0.0
                        if (aDeal != bDeal) {
                            bDeal.compareTo(aDeal)
                        } else {
                            val aReviews = a.steamRatingCount ?: 0
                            val bReviews = b.steamRatingCount ?: 0
                            bReviews.compareTo(aReviews)
                        }
                    }
                }
                SortOption.DEAL_RATING -> {
                    val aDeal = a.dealRating ?: 0.0
                    val bDeal = b.dealRating ?: 0.0
                    if (aDeal != bDeal) {
                        bDeal.compareTo(aDeal)
                    } else {
                        val aMeta = a.metacriticScore ?: 0
                        val bMeta = b.metacriticScore ?: 0
                        if (aMeta != bMeta) {
                            bMeta.compareTo(aMeta)
                        } else {
                            val aReviews = a.steamRatingCount ?: 0
                            val bReviews = b.steamRatingCount ?: 0
                            bReviews.compareTo(aReviews)
                        }
                    }
                }
                SortOption.SAVINGS -> {
                    if (a.savingsPercent != b.savingsPercent) {
                        b.savingsPercent.compareTo(a.savingsPercent)
                    } else {
                        val aDeal = a.dealRating ?: 0.0
                        val bDeal = b.dealRating ?: 0.0
                        bDeal.compareTo(aDeal)
                    }
                }
                SortOption.PRICE_LOW -> {
                    a.salePrice.compareTo(b.salePrice)
                }
            }
        }
    }

    private fun isUnknownShovelware(deal: GameDeal): Boolean {
        val meta = deal.metacriticScore ?: 0
        val reviews = deal.steamRatingCount ?: 0
        val normalPrice = deal.normalPrice

        // If the game has a real Metacritic score, it is definitely recognized
        if (meta > 0) return false

        // If it has substantial Steam reviews (>= 150), it is a known popular game
        if (reviews >= 150) return false

        // If normal retail price is significant (>= $10) and has some reviews
        if (normalPrice >= 10.0 && reviews >= 50) return false

        // Otherwise: 0 Metacritic, fewer than 150 reviews, low price -> unknown game
        return true
    }

    suspend fun searchGames(query: String): Result<List<GameSearchResult>> = withContext(Dispatchers.IO) {
        runCatching {
            val normalized = com.example.util.SearchNormalizer.normalize(query)
            // HTTP 400 Prevention: Don't trigger request if empty or less than 2 valid characters
            if (normalized.sanitizedQuery.length < 2 || normalized.sanitizedQuery.none { it.isLetterOrDigit() }) {
                return@runCatching emptyList()
            }

            val encodedTitle = com.example.util.SearchNormalizer.safeUrlEncode(normalized.resolvedQuery)
            val response = apiService.searchGames(title = encodedTitle, limit = 30)
            val mapped = response.map { dto ->
                GameSearchResult(
                    gameId = dto.gameID,
                    title = dto.external,
                    cheapestPrice = dto.cheapest?.toDoubleOrNull() ?: 0.0,
                    cheapestDealId = dto.cheapestDealID,
                    steamAppId = dto.steamAppID,
                    thumbUrl = dto.thumb ?: ""
                )
            }

            // Intelligent ranking: Exact matches, resolved aliases, and flagship games prioritized
            com.example.util.SearchNormalizer.rankResults(
                results = mapped,
                rawQuery = query,
                resolvedQuery = normalized.resolvedQuery
            )
        }
    }

    suspend fun getGameDetails(gameId: String): Result<GameDetails> = withContext(Dispatchers.IO) {
        runCatching {
            val response = apiService.getGameLookup(id = gameId)
            val info = response.info
            val dealsList = response.deals.orEmpty().map { dealDto ->
                StoreDealInfo(
                    storeType = StoreType.fromId(dealDto.storeID),
                    dealId = dealDto.dealID,
                    price = dealDto.price.toDoubleOrNull() ?: 0.0,
                    retailPrice = dealDto.retailPrice.toDoubleOrNull() ?: 0.0,
                    savingsPercent = (dealDto.savings.toDoubleOrNull() ?: 0.0).roundToInt(),
                    isDealActive = (dealDto.savings.toDoubleOrNull() ?: 0.0) > 0.0
                )
            }.filter { it.storeType != StoreType.OTHER } // Focus on Steam and Epic Games

            val steamAppId = info?.steamAppID
            val steamDetails = if (!steamAppId.isNullOrBlank()) {
                fetchSteamAppDetails(steamAppId)
            } else null

            GameDetails(
                gameId = gameId,
                title = info?.title ?: "Oyun Detayı",
                thumbUrl = info?.thumb ?: "",
                bannerUrl = steamDetails?.bannerUrl ?: info?.thumb,
                description = steamDetails?.description,
                releaseDate = steamDetails?.releaseDate,
                metacriticScore = steamDetails?.metacriticScore,
                steamAppId = steamAppId,
                cheapestPriceEver = response.cheapestPriceEver?.price?.toDoubleOrNull(),
                cheapestEverDate = response.cheapestPriceEver?.date,
                minRequirements = steamDetails?.minRequirements,
                recRequirements = steamDetails?.recRequirements,
                deals = dealsList
            )
        }
    }

    private data class SteamDetails(
        val bannerUrl: String?,
        val description: String?,
        val releaseDate: String?,
        val metacriticScore: Int?,
        val minRequirements: com.example.model.SystemRequirements?,
        val recRequirements: com.example.model.SystemRequirements?
    )

    private fun fetchSteamAppDetails(appId: String): SteamDetails? {
        return try {
            val url = "https://store.steampowered.com/api/appdetails?appids=$appId&l=turkish"
            val request = okhttp3.Request.Builder()
                .url(url)
                .header("User-Agent", "GameDealz/1.0 (contact@gamedealz.app)")
                .build()
            val response = ApiClient.okHttpClient.newCall(request).execute()
            if (!response.isSuccessful) return null
            val body = response.body?.string() ?: return null
            val jsonObject = org.json.JSONObject(body)
            val appObj = jsonObject.optJSONObject(appId) ?: return null
            if (!appObj.optBoolean("success", false)) return null
            val data = appObj.optJSONObject("data") ?: return null

            val headerImage = data.optString("header_image").ifBlank { null }
            val shortDesc = data.optString("short_description").ifBlank { null }
            val releaseDate = data.optJSONObject("release_date")?.optString("date")?.ifBlank { null }
            val metaScore = data.optJSONObject("metacritic")?.optInt("score", 0)

            val pcReq = data.optJSONObject("pc_requirements")
            val minHtml = pcReq?.optString("minimum", "")
            val recHtml = pcReq?.optString("recommended", "")

            SteamDetails(
                bannerUrl = headerImage,
                description = shortDesc,
                releaseDate = releaseDate,
                metacriticScore = if (metaScore != null && metaScore > 0) metaScore else null,
                minRequirements = parseRequirementsHtml(minHtml),
                recRequirements = parseRequirementsHtml(recHtml)
            )
        } catch (_: Exception) {
            null
        }
    }

    private fun parseRequirementsHtml(rawHtml: String?): com.example.model.SystemRequirements? {
        if (rawHtml.isNullOrBlank()) return null

        var text = rawHtml
            .replace(Regex("(?i)<br\\s*/?>"), "\n")
            .replace(Regex("(?i)</li>"), "\n")
            .replace(Regex("<[^>]+>"), "")
            .replace("&amp;", "&")
            .replace("&quot;", "\"")
            .replace("&#39;", "'")
            .replace("&nbsp;", " ")
            .replace("&trade;", "™")
            .replace("&reg;", "®")

        val lines = text.split("\n")
            .map { it.replace(Regex("\\s+"), " ").trim() }
            .filter { line ->
                line.isNotBlank() &&
                !line.equals("Minimum:", ignoreCase = true) &&
                !line.equals("Önerilen:", ignoreCase = true) &&
                !line.equals("Recommended:", ignoreCase = true)
            }

        if (lines.isEmpty()) return null

        var os: String? = null
        var processor: String? = null
        var memory: String? = null
        var graphics: String? = null
        var storage: String? = null

        for (l in lines) {
            val lower = l.lowercase()
            when {
                lower.startsWith("işletim sistemi:") || lower.startsWith("os:") -> {
                    os = l.substringAfter(":").trim()
                }
                lower.startsWith("işlemci:") || lower.startsWith("processor:") || lower.startsWith("cpu:") -> {
                    processor = l.substringAfter(":").trim()
                }
                lower.startsWith("bellek:") || lower.startsWith("memory:") || lower.startsWith("ram:") -> {
                    memory = l.substringAfter(":").trim()
                }
                lower.startsWith("ekran kartı:") || lower.startsWith("graphics:") || lower.startsWith("gpu:") || lower.startsWith("video:") -> {
                    graphics = l.substringAfter(":").trim()
                }
                lower.startsWith("depolama:") || lower.startsWith("storage:") || lower.startsWith("hard drive:") || lower.startsWith("hdd:") -> {
                    storage = l.substringAfter(":").trim()
                }
            }
        }

        return com.example.model.SystemRequirements(
            rawText = lines.joinToString("\n"),
            os = os,
            processor = processor,
            memory = memory,
            graphics = graphics,
            storage = storage
        )
    }

    // Local Storage / Room Watchlist methods
    fun getAllTrackedGames(): Flow<List<TrackedGameEntity>> = trackedGameDao.getAllTrackedGames()

    fun isTracked(gameId: String): Flow<Boolean> = trackedGameDao.isTracked(gameId)

    suspend fun trackGame(game: TrackedGameEntity) = withContext(Dispatchers.IO) {
        trackedGameDao.insertTrackedGame(game)
    }

    suspend fun untrackGame(gameId: String) = withContext(Dispatchers.IO) {
        trackedGameDao.deleteTrackedGame(gameId)
    }

    suspend fun updateTargetPrice(gameId: String, targetPrice: Double?) = withContext(Dispatchers.IO) {
        trackedGameDao.updateTargetPrice(gameId, targetPrice)
    }

    // Notifications
    fun getNotifications(): Flow<List<PriceNotificationEntity>> = notificationDao.getAllNotifications()

    fun getUnreadCount(): Flow<Int> = notificationDao.getUnreadCount()

    suspend fun markNotificationAsRead(id: Long) = withContext(Dispatchers.IO) {
        notificationDao.markAsRead(id)
    }

    suspend fun markAllNotificationsAsRead() = withContext(Dispatchers.IO) {
        notificationDao.markAllAsRead()
    }

    suspend fun clearNotifications() = withContext(Dispatchers.IO) {
        notificationDao.clearAllNotifications()
    }

    suspend fun checkAndUpdateTrackedGamePrices(): Result<Int> = withContext(Dispatchers.IO) {
        runCatching {
            val trackedList = trackedGameDao.getAllTrackedGamesSync()
            var priceDropCount = 0

            for (tracked in trackedList) {
                val detailsResult = getGameDetails(tracked.gameId)
                if (detailsResult.isSuccess) {
                    val details = detailsResult.getOrThrow()
                    val bestDeal = details.deals.minByOrNull { it.price }
                    if (bestDeal != null) {
                        val currentPrice = bestDeal.price
                        val hasDropped = currentPrice < tracked.lastPrice
                        val hitTarget = tracked.targetPrice != null && currentPrice <= tracked.targetPrice

                        if (hasDropped || hitTarget) {
                            priceDropCount++
                            notificationDao.insertNotification(
                                PriceNotificationEntity(
                                    gameId = tracked.gameId,
                                    gameTitle = tracked.title,
                                    thumbUrl = tracked.thumbUrl,
                                    storeName = bestDeal.storeType.displayName,
                                    oldPrice = tracked.lastPrice,
                                    newPrice = currentPrice,
                                    savingsPercent = bestDeal.savingsPercent,
                                    dealId = bestDeal.dealId
                                )
                            )
                        }

                        trackedGameDao.updateTrackedGame(
                            tracked.copy(
                                lastPrice = currentPrice,
                                retailPrice = bestDeal.retailPrice,
                                savingsPercent = bestDeal.savingsPercent,
                                storeName = bestDeal.storeType.displayName,
                                dealId = bestDeal.dealId,
                                lastCheckedAt = System.currentTimeMillis(),
                                isDiscounted = bestDeal.savingsPercent > 0
                            )
                        )
                    }
                }
            }
            priceDropCount
        }
    }
}
