package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackedGameDao {
    @Query("SELECT * FROM tracked_games ORDER BY addedAt DESC")
    fun getAllTrackedGames(): Flow<List<TrackedGameEntity>>

    @Query("SELECT * FROM tracked_games")
    suspend fun getAllTrackedGamesSync(): List<TrackedGameEntity>

    @Query("SELECT * FROM tracked_games WHERE gameId = :gameId LIMIT 1")
    fun getTrackedGame(gameId: String): Flow<TrackedGameEntity?>

    @Query("SELECT EXISTS(SELECT 1 FROM tracked_games WHERE gameId = :gameId)")
    fun isTracked(gameId: String): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrackedGame(game: TrackedGameEntity)

    @Update
    suspend fun updateTrackedGame(game: TrackedGameEntity)

    @Query("UPDATE tracked_games SET targetPrice = :targetPrice WHERE gameId = :gameId")
    suspend fun updateTargetPrice(gameId: String, targetPrice: Double?)

    @Query("DELETE FROM tracked_games WHERE gameId = :gameId")
    suspend fun deleteTrackedGame(gameId: String)
}

@Dao
interface PriceNotificationDao {
    @Query("SELECT * FROM price_notifications ORDER BY timestamp DESC")
    fun getAllNotifications(): Flow<List<PriceNotificationEntity>>

    @Query("SELECT COUNT(*) FROM price_notifications WHERE isRead = 0")
    fun getUnreadCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: PriceNotificationEntity)

    @Query("UPDATE price_notifications SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: Long)

    @Query("UPDATE price_notifications SET isRead = 1")
    suspend fun markAllAsRead()

    @Query("DELETE FROM price_notifications WHERE id = :id")
    suspend fun deleteNotification(id: Long)

    @Query("DELETE FROM price_notifications")
    suspend fun clearAllNotifications()
}
