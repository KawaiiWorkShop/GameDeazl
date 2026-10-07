package com.example.model

enum class StoreType(val id: String, val displayName: String) {
    STEAM("1", "Steam"),
    EPIC("25", "Epic Games"),
    OTHER("0", "Diğer");

    companion object {
        fun fromId(id: String): StoreType = when (id) {
            "1" -> STEAM
            "25" -> EPIC
            else -> OTHER
        }
    }
}

enum class StoreFilter(val storeIds: String, val label: String) {
    ALL("1,25", "Tümü (Steam & Epic)"),
    STEAM("1", "Steam Fırsatları"),
    EPIC("25", "Epic Games Fırsatları")
}

enum class SortOption(val apiValue: String, val label: String) {
    SAVINGS("Savings", "🔥 En Yüksek İndirim (%)"),
    METACRITIC("Metacritic", "🏆 En Popüler (Metacritic)"),
    DEAL_RATING("Deal Rating", "⭐ En İyi Fırsat"),
    PRICE_LOW("Price", "🏷️ Fiyat: En Düşük")
}

data class GameDeal(
    val dealId: String,
    val gameId: String,
    val title: String,
    val storeType: StoreType,
    val salePrice: Double,
    val normalPrice: Double,
    val savingsPercent: Int,
    val metacriticScore: Int?,
    val steamRatingText: String?,
    val steamRatingPercent: Int?,
    val steamRatingCount: Int? = null,
    val steamAppId: String?,
    val dealRating: Double?,
    val thumbUrl: String
) {
    val dealUrl: String
        get() = "https://www.cheapshark.com/redirect?dealID=$dealId"

    val isFree: Boolean
        get() = salePrice <= 0.0 || savingsPercent >= 100
}

data class GameSearchResult(
    val gameId: String,
    val title: String,
    val cheapestPrice: Double,
    val cheapestDealId: String?,
    val steamAppId: String?,
    val thumbUrl: String
)

data class StoreDealInfo(
    val storeType: StoreType,
    val dealId: String,
    val price: Double,
    val retailPrice: Double,
    val savingsPercent: Int,
    val isDealActive: Boolean
) {
    val dealUrl: String
        get() = "https://www.cheapshark.com/redirect?dealID=$dealId"
}

data class SystemRequirements(
    val rawText: String,
    val os: String? = null,
    val processor: String? = null,
    val memory: String? = null,
    val graphics: String? = null,
    val storage: String? = null
)

data class GameDetails(
    val gameId: String,
    val title: String,
    val thumbUrl: String,
    val bannerUrl: String? = null,
    val description: String? = null,
    val releaseDate: String? = null,
    val metacriticScore: Int? = null,
    val steamAppId: String? = null,
    val cheapestPriceEver: Double? = null,
    val cheapestEverDate: Long? = null,
    val minRequirements: SystemRequirements? = null,
    val recRequirements: SystemRequirements? = null,
    val deals: List<StoreDealInfo> = emptyList()
)
