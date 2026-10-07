package com.example.data.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CheapSharkDealDto(
    @Json(name = "dealID") val dealID: String,
    @Json(name = "title") val title: String,
    @Json(name = "storeID") val storeID: String,
    @Json(name = "gameID") val gameID: String,
    @Json(name = "salePrice") val salePrice: String,
    @Json(name = "normalPrice") val normalPrice: String,
    @Json(name = "isOnSale") val isOnSale: String,
    @Json(name = "savings") val savings: String,
    @Json(name = "metacriticScore") val metacriticScore: String? = null,
    @Json(name = "steamRatingText") val steamRatingText: String? = null,
    @Json(name = "steamRatingPercent") val steamRatingPercent: String? = null,
    @Json(name = "steamRatingCount") val steamRatingCount: String? = null,
    @Json(name = "steamAppID") val steamAppID: String? = null,
    @Json(name = "releaseDate") val releaseDate: Long? = null,
    @Json(name = "dealRating") val dealRating: String? = null,
    @Json(name = "thumb") val thumb: String? = null
)

@JsonClass(generateAdapter = true)
data class CheapSharkGameSearchDto(
    @Json(name = "gameID") val gameID: String,
    @Json(name = "steamAppID") val steamAppID: String? = null,
    @Json(name = "cheapest") val cheapest: String? = null,
    @Json(name = "cheapestDealID") val cheapestDealID: String? = null,
    @Json(name = "external") val external: String,
    @Json(name = "internalName") val internalName: String? = null,
    @Json(name = "thumb") val thumb: String? = null
)

@JsonClass(generateAdapter = true)
data class CheapSharkGameLookupDto(
    @Json(name = "info") val info: CheapSharkGameInfo? = null,
    @Json(name = "cheapestPriceEver") val cheapestPriceEver: CheapSharkCheapestPriceEver? = null,
    @Json(name = "deals") val deals: List<CheapSharkLookupDealDto>? = null
)

@JsonClass(generateAdapter = true)
data class CheapSharkGameInfo(
    @Json(name = "title") val title: String? = null,
    @Json(name = "steamAppID") val steamAppID: String? = null,
    @Json(name = "thumb") val thumb: String? = null
)

@JsonClass(generateAdapter = true)
data class CheapSharkCheapestPriceEver(
    @Json(name = "price") val price: String? = null,
    @Json(name = "date") val date: Long? = null
)

@JsonClass(generateAdapter = true)
data class CheapSharkLookupDealDto(
    @Json(name = "storeID") val storeID: String,
    @Json(name = "dealID") val dealID: String,
    @Json(name = "price") val price: String,
    @Json(name = "retailPrice") val retailPrice: String,
    @Json(name = "savings") val savings: String
)

@JsonClass(generateAdapter = true)
data class CheapSharkStoreDto(
    @Json(name = "storeID") val storeID: String,
    @Json(name = "storeName") val storeName: String,
    @Json(name = "isActive") val isActive: Int
)
