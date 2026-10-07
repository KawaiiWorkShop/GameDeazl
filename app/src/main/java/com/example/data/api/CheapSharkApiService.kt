package com.example.data.api

import retrofit2.http.GET
import retrofit2.http.Query

interface CheapSharkApiService {

    /**
     * Get deals with filtering and sorting
     * storeID: "1" for Steam, "25" for Epic Games, "1,25" for both
     */
    @GET("deals")
    suspend fun getDeals(
        @Query("storeID") storeID: String = "1,25",
        @Query("pageNumber") pageNumber: Int = 0,
        @Query("pageSize") pageSize: Int = 60,
        @Query("sortBy") sortBy: String = "Savings",
        @Query("desc") desc: Int = 1,
        @Query("lowerPrice") lowerPrice: Double? = null,
        @Query("upperPrice") upperPrice: Double? = null,
        @Query("onSale") onSale: Int = 1,
        @Query(value = "title", encoded = true) title: String? = null
    ): List<CheapSharkDealDto>

    /**
     * Search games by title safely with URL-encoded query
     */
    @GET("games")
    suspend fun searchGames(
        @Query(value = "title", encoded = true) title: String,
        @Query("limit") limit: Int = 30
    ): List<CheapSharkGameSearchDto>

    /**
     * Get game details including deals across all stores
     */
    @GET("games")
    suspend fun getGameLookup(
        @Query("id") id: String
    ): CheapSharkGameLookupDto

    /**
     * Get stores information
     */
    @GET("stores")
    suspend fun getStores(): List<CheapSharkStoreDto>
}
