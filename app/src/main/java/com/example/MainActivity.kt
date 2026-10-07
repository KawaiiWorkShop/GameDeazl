package com.example

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.GameDetailBottomSheet
import com.example.ui.screens.DealsScreen
import com.example.ui.screens.NotificationsScreen
import com.example.ui.screens.SearchScreen
import com.example.ui.screens.WatchlistScreen
import com.example.ui.theme.AmoledBackground
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.GameDealsViewModel

enum class AppNavTab(val label: String) {
    DEALS("İndirimler"),
    SEARCH("Arama"),
    WATCHLIST("Takip Listem"),
    NOTIFICATIONS("Bildirimler")
}

class MainActivity : ComponentActivity() {

    private val viewModel: GameDealsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                MainAppScreen(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(
    viewModel: GameDealsViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var currentTab by remember { mutableStateOf(AppNavTab.DEALS) }

    val dealsState by viewModel.dealsUiState.collectAsStateWithLifecycle()
    val searchState by viewModel.searchUiState.collectAsStateWithLifecycle()
    val trackedGames by viewModel.trackedGames.collectAsStateWithLifecycle()
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()
    val unreadCount by viewModel.unreadAlertsCount.collectAsStateWithLifecycle()
    val selectedGameDetails by viewModel.selectedGameDetails.collectAsStateWithLifecycle()
    val isRefreshingPrices by viewModel.isRefreshingPrices.collectAsStateWithLifecycle()
    val refreshFeedback by viewModel.refreshFeedback.collectAsStateWithLifecycle()
    val isLoadingDetails by viewModel.isLoadingDetails.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("main_scaffold"),
        containerColor = AmoledBackground,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.SportsEsports,
                            contentDescription = null,
                            tint = CyanAccent,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "GameDealz",
                            color = TextPrimary,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Steam • Epic",
                            color = TextMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                },
                actions = {
                    // Top Bell Notification Icon with unread badge
                    IconButton(
                        onClick = { currentTab = AppNavTab.NOTIFICATIONS },
                        modifier = Modifier.testTag("top_bell_button")
                    ) {
                        BadgedBox(
                            badge = {
                                if (unreadCount > 0) {
                                    Badge(
                                        containerColor = CyanAccent,
                                        contentColor = AmoledBackground
                                    ) {
                                        Text(text = "$unreadCount", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Bildirimler",
                                tint = if (currentTab == AppNavTab.NOTIFICATIONS) CyanAccent else TextSecondary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = DarkSurface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = DarkSurface,
                tonalElevation = 0.dp
            ) {
                // Deals Tab
                NavigationBarItem(
                    selected = currentTab == AppNavTab.DEALS,
                    onClick = { currentTab = AppNavTab.DEALS },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.LocalOffer,
                            contentDescription = "İndirimler"
                        )
                    },
                    label = { Text("İndirimler", fontSize = 12.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = AmoledBackground,
                        selectedTextColor = CyanAccent,
                        indicatorColor = CyanAccent,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted
                    ),
                    modifier = Modifier.testTag("nav_tab_deals")
                )

                // Search Tab
                NavigationBarItem(
                    selected = currentTab == AppNavTab.SEARCH,
                    onClick = { currentTab = AppNavTab.SEARCH },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Arama"
                        )
                    },
                    label = { Text("Arama", fontSize = 12.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = AmoledBackground,
                        selectedTextColor = CyanAccent,
                        indicatorColor = CyanAccent,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted
                    ),
                    modifier = Modifier.testTag("nav_tab_search")
                )

                // Watchlist Tab
                NavigationBarItem(
                    selected = currentTab == AppNavTab.WATCHLIST,
                    onClick = { currentTab = AppNavTab.WATCHLIST },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (trackedGames.isNotEmpty()) {
                                    Badge(
                                        containerColor = Color(0xFF1E293B),
                                        contentColor = CyanAccent
                                    ) {
                                        Text(text = "${trackedGames.size}")
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bookmark,
                                contentDescription = "Takip Listem"
                            )
                        }
                    },
                    label = { Text("Takip Listem", fontSize = 12.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = AmoledBackground,
                        selectedTextColor = CyanAccent,
                        indicatorColor = CyanAccent,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted
                    ),
                    modifier = Modifier.testTag("nav_tab_watchlist")
                )

                // Notifications Tab
                NavigationBarItem(
                    selected = currentTab == AppNavTab.NOTIFICATIONS,
                    onClick = { currentTab = AppNavTab.NOTIFICATIONS },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (unreadCount > 0) {
                                    Badge(
                                        containerColor = CyanAccent,
                                        contentColor = AmoledBackground
                                    ) {
                                        Text(text = "$unreadCount", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Bildirimler"
                            )
                        }
                    },
                    label = { Text("Bildirimler", fontSize = 12.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = AmoledBackground,
                        selectedTextColor = CyanAccent,
                        indicatorColor = CyanAccent,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted
                    ),
                    modifier = Modifier.testTag("nav_tab_notifications")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                AppNavTab.DEALS -> {
                    DealsScreen(
                        state = dealsState,
                        trackedGames = trackedGames,
                        onFilterSelected = { viewModel.setStoreFilter(it) },
                        onSortSelected = { viewModel.setSortOption(it) },
                        onToggleTrack = { viewModel.toggleTrackDeal(it) },
                        onCardClick = { viewModel.openGameDetails(it.gameId) },
                        onRefresh = { viewModel.loadDeals(isPullToRefresh = true) }
                    )
                }

                AppNavTab.SEARCH -> {
                    SearchScreen(
                        state = searchState,
                        trackedGames = trackedGames,
                        onQueryChange = { viewModel.onSearchQueryChanged(it) },
                        onTrackResult = { viewModel.trackSearchResult(it) },
                        onResultClick = { viewModel.openGameDetails(it.gameId) }
                    )
                }

                AppNavTab.WATCHLIST -> {
                    WatchlistScreen(
                        trackedGames = trackedGames,
                        isRefreshingPrices = isRefreshingPrices,
                        refreshFeedback = refreshFeedback,
                        onRefreshPrices = { viewModel.refreshTrackedPrices() },
                        onUntrackGame = { viewModel.untrackGame(it) },
                        onUpdateTargetPrice = { gameId, target ->
                            viewModel.updateTargetPrice(gameId, target)
                        },
                        onGameClick = { viewModel.openGameDetails(it) },
                        onDismissFeedback = { viewModel.clearRefreshFeedback() }
                    )
                }

                AppNavTab.NOTIFICATIONS -> {
                    NotificationsScreen(
                        notifications = notifications,
                        onMarkAllRead = { viewModel.markAllAlertsRead() },
                        onClearAll = { viewModel.clearAllAlerts() },
                        onNotificationClick = { notif ->
                            viewModel.markAlertRead(notif.id)
                            viewModel.openGameDetails(notif.gameId)
                        }
                    )
                }
            }
        }

        // Loading indicator for game details
        if (isLoadingDetails) {
            androidx.compose.ui.window.Dialog(onDismissRequest = {}) {
                Box(
                    modifier = Modifier
                        .clip(androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
                        .background(Color(0xFF121622))
                        .padding(horizontal = 24.dp, vertical = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        androidx.compose.material3.CircularProgressIndicator(
                            color = CyanAccent,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 3.dp
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Text(
                            text = "Oyun detayları yükleniyor...",
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // Detailed Game Bottom Sheet
        selectedGameDetails?.let { details ->
            val isTracked = trackedGames.any { it.gameId == details.gameId }
            GameDetailBottomSheet(
                details = details,
                isTracked = isTracked,
                onToggleTrack = {
                    if (isTracked) {
                        viewModel.untrackGame(details.gameId)
                    } else {
                        val cheapestDeal = details.deals.minByOrNull { it.price }
                        val entity = com.example.data.local.TrackedGameEntity(
                            gameId = details.gameId,
                            title = details.title,
                            thumbUrl = details.thumbUrl,
                            steamAppId = details.steamAppId,
                            lastPrice = cheapestDeal?.price ?: 0.0,
                            retailPrice = cheapestDeal?.retailPrice ?: 0.0,
                            lowestPriceEver = details.cheapestPriceEver,
                            savingsPercent = cheapestDeal?.savingsPercent ?: 0,
                            storeName = cheapestDeal?.storeType?.displayName ?: "Steam/Epic",
                            dealId = cheapestDeal?.dealId,
                            targetPrice = cheapestDeal?.price,
                            isDiscounted = (cheapestDeal?.savingsPercent ?: 0) > 0
                        )
                        viewModel.trackCustomEntity(entity)
                    }
                },
                onDismiss = { viewModel.closeGameDetails() }
            )
        }
    }
}
