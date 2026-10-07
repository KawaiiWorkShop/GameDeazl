package com.example

import org.junit.Assert.*
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testDescendingDiscountSort() {
    val deal10 = com.example.model.GameDeal(
      dealId = "1", gameId = "101", title = "Game A",
      storeType = com.example.model.StoreType.STEAM,
      salePrice = 18.0, normalPrice = 20.0, savingsPercent = 10,
      metacriticScore = 80, steamRatingText = null, steamRatingPercent = null,
      steamAppId = null, dealRating = 5.0, thumbUrl = ""
    )
    val deal100 = com.example.model.GameDeal(
      dealId = "2", gameId = "102", title = "Free Game",
      storeType = com.example.model.StoreType.EPIC,
      salePrice = 0.0, normalPrice = 30.0, savingsPercent = 100,
      metacriticScore = 90, steamRatingText = null, steamRatingPercent = null,
      steamAppId = null, dealRating = 10.0, thumbUrl = ""
    )
    val deal80 = com.example.model.GameDeal(
      dealId = "3", gameId = "103", title = "Game B",
      storeType = com.example.model.StoreType.STEAM,
      salePrice = 10.0, normalPrice = 50.0, savingsPercent = 80,
      metacriticScore = 85, steamRatingText = null, steamRatingPercent = null,
      steamAppId = null, dealRating = 8.0, thumbUrl = ""
    )
    val deal90 = com.example.model.GameDeal(
      dealId = "4", gameId = "104", title = "Game C",
      storeType = com.example.model.StoreType.STEAM,
      salePrice = 5.0, normalPrice = 50.0, savingsPercent = 90,
      metacriticScore = 75, steamRatingText = null, steamRatingPercent = null,
      steamAppId = null, dealRating = 9.0, thumbUrl = ""
    )

    val input = listOf(deal10, deal80, deal100, deal90)
    val sorted = input.sortedWith { a, b ->
      if (a.savingsPercent != b.savingsPercent) {
        b.savingsPercent.compareTo(a.savingsPercent)
      } else {
        (b.dealRating ?: 0.0).compareTo(a.dealRating ?: 0.0)
      }
    }

    assertEquals(listOf(100, 90, 80, 10), sorted.map { it.savingsPercent })
    assertEquals("Free Game", sorted[0].title)
    assertTrue(sorted[0].isFree)
  }
}
