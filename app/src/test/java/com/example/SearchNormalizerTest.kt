package com.example

import com.example.model.GameSearchResult
import com.example.util.SearchNormalizer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SearchNormalizerTest {

    @Test
    fun testAliasResolution() {
        // gmod -> Garry's Mod
        val gmod = SearchNormalizer.normalize("gmod")
        assertEquals("Garry's Mod", gmod.resolvedQuery)
        assertEquals("Garry's Mod", gmod.matchedAlias)

        // garrys mod -> Garry's Mod
        val garrysMod = SearchNormalizer.normalize("garrys mod")
        assertEquals("Garry's Mod", garrysMod.resolvedQuery)

        // gta -> Grand Theft Auto
        val gta = SearchNormalizer.normalize("gta")
        assertEquals("Grand Theft Auto", gta.resolvedQuery)

        // gtav -> Grand Theft Auto V
        val gtav = SearchNormalizer.normalize("gtav")
        assertEquals("Grand Theft Auto V", gtav.resolvedQuery)

        // rdr2 -> Red Dead Redemption 2
        val rdr2 = SearchNormalizer.normalize("rdr2")
        assertEquals("Red Dead Redemption 2", rdr2.resolvedQuery)

        // cs2 -> Counter-Strike
        val cs2 = SearchNormalizer.normalize("cs2")
        assertEquals("Counter-Strike", cs2.resolvedQuery)
    }

    @Test
    fun testSanitization() {
        val withApostrophe = SearchNormalizer.normalize("Garry's   Mod")
        assertEquals("Garry's Mod", withApostrophe.resolvedQuery)

        val withExcessiveSpaces = SearchNormalizer.normalize("  Cyberpunk   2077  ")
        assertEquals("Cyberpunk 2077", withExcessiveSpaces.resolvedQuery)
    }

    @Test
    fun testRankingPrioritizesMainGameOverDlc() {
        val sampleResults = listOf(
            GameSearchResult("1", "Garry's Mod Soundtrack", 1.99, "d1", null, ""),
            GameSearchResult("2", "Garry's Mod", 9.99, "d2", "4000", ""),
            GameSearchResult("3", "Garry's Mod Expansion Pack", 4.99, "d3", null, "")
        )

        val ranked = SearchNormalizer.rankResults(
            results = sampleResults,
            rawQuery = "gmod",
            resolvedQuery = "Garry's Mod"
        )

        assertEquals("Garry's Mod", ranked.first().title)
    }
}
