package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tracked_games")
data class TrackedGameEntity(
    @PrimaryKey
    val gameId: String,
    val title: String,
    val thumbUrl: String,
    val steamAppId: String?,
    val lastPrice: Double,
    val retailPrice: Double,
    val lowestPriceEver: Double?,
    val savingsPercent: Int,
    val storeName: String,
    val dealId: String?,
    val targetPrice: Double? = null,
    val addedAt: Long = System.currentTimeMillis(),
    val lastCheckedAt: Long = System.currentTimeMillis(),
    val isDiscounted: Boolean = false
)

@Entity(tableName = "price_notifications")
data class PriceNotificationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val gameId: String,
    val gameTitle: String,
    val thumbUrl: String,
    val storeName: String,
    val oldPrice: Double,
    val newPrice: Double,
    val savingsPercent: Int,
    val dealId: String?,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)
