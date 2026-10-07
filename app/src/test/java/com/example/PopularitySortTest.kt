package com.example

import com.example.data.local.PriceNotificationDao
import com.example.data.local.PriceNotificationEntity
import com.example.data.local.TrackedGameDao
import com.example.data.local.TrackedGameEntity
import com.example.data.repository.GameDealsRepository
import com.example.model.GameDeal
import com.example.model.SortOption
import com.example.model.StoreType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import org.junit.Assert.assertEquals
import org.junit.Test

class PopularitySortTest {

    private val fakeTrackedDao = object : TrackedGameDao {
        override fun getAllTrackedGames(): Flow<List<TrackedGameEntity>> = emptyFlow()
        override suspend fun getAllTrackedGamesSync(): List<TrackedGameEntity> = emptyList()
        override fun getTrackedGame(gameId: String): Flow<TrackedGameEntity?> = emptyFlow()
        override fun isTracked(gameId: String): Flow<Boolean> = emptyFlow()
        override suspend fun insertTrackedGame(game: TrackedGameEntity) {}
        override suspend fun updateTrackedGame(game: TrackedGameEntity) {}
        override suspend fun updateTargetPrice(gameId: String, targetPrice: Double?) {}
        override suspend fun deleteTrackedGame(gameId: String) {}
    }

    private val fakeNotifDao = object : PriceNotificationDao {
        override fun getAllNotifications(): Flow<List<PriceNotificationEntity>> = emptyFlow()
        override fun getUnreadCount(): Flow<Int> = emptyFlow()
        override suspend fun insertNotification(notification: PriceNotificationEntity) {}
        override suspend fun markAsRead(id: Long) {}
        override suspend fun markAllAsRead() {}
        override suspend fun deleteNotification(id: Long) {}
        override suspend fun clearAllNotifications() {}
    }

    private val repository = GameDealsRepository(
        trackedGameDao = fakeTrackedDao,
        notificationDao = fakeNotifDao
    )

    @Test
    fun testMetacriticAndPopularitySorting() {
        val shovelware = GameDeal(
            dealId = "d1",
            gameId = "g1",
            title = "Extraordinary Ball",
            storeType = StoreType.STEAM,
            salePrice = 0.49,
            normalPrice = 0.99,
            savingsPercent = 50,
            metacriticScore = null,
            steamRatingText = null,
            steamRatingPercent = null,
            steamRatingCount = 30, // obscure shovelware
            steamAppId = null,
            dealRating = 0.0,
            thumbUrl = ""
        )

        val popularAaaGame = GameDeal(
            dealId = "d2",
            gameId = "g2",
            title = "Batman: Arkham Asylum",
            storeType = StoreType.STEAM,
            salePrice = 4.99,
            normalPrice = 19.99,
            savingsPercent = 75,
            metacriticScore = 91, // high metacritic
            steamRatingText = "Very Positive",
            steamRatingPercent = 94,
            steamRatingCount = 32000,
            steamAppId = "35140",
            dealRating = 8.5,
            thumbUrl = ""
        )

        val mediumGame = GameDeal(
            dealId = "d3",
            gameId = "g3",
            title = "Trine 2",
            storeType = StoreType.STEAM,
            salePrice = 3.99,
            normalPrice = 19.99,
            savingsPercent = 80,
            metacriticScore = 84,
            steamRatingText = "Very Positive",
            steamRatingPercent = 91,
            steamRatingCount = 5700,
            steamAppId = "35720",
            dealRating = 7.0,
            thumbUrl = ""
        )

        val rawList = listOf(shovelware, mediumGame, popularAaaGame)

        val sorted = repository.sortAndFilterDealsByPopularity(rawList, SortOption.METACRITIC)

        // 1st: Batman (Metacritic 91)
        assertEquals("Batman: Arkham Asylum", sorted[0].title)
        // 2nd: Trine 2 (Metacritic 84)
        assertEquals("Trine 2", sorted[1].title)
        // 3rd (last): Shovelware sunk to bottom
        assertEquals("Extraordinary Ball", sorted[2].title)
    }
}
