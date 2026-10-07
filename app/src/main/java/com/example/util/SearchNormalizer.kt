package com.example.util

import com.example.model.GameSearchResult
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.Locale

data class NormalizedQueryInfo(
    val rawQuery: String,
    val sanitizedQuery: String,
    val resolvedQuery: String,
    val matchedAlias: String? = null
)

object SearchNormalizer {

    /**
     * Map of common gaming acronyms, abbreviations, and informal names
     * to their full canonical titles.
     */
    private val ALIAS_MAP = mapOf(
        // Garry's Mod & Popular Simulators
        "gmod" to "Garry's Mod",
        "garrys mod" to "Garry's Mod",
        "garry mod" to "Garry's Mod",
        "ics" to "Internet Cafe Simulator",
        "internet cafe" to "Internet Cafe Simulator",
        "internet cafe simulator" to "Internet Cafe Simulator",
        "how to fish" to "How to Fish",

        // Grand Theft Auto
        "gta" to "Grand Theft Auto",
        "gtav" to "Grand Theft Auto V",
        "gta v" to "Grand Theft Auto V",
        "gta 5" to "Grand Theft Auto V",
        "gta5" to "Grand Theft Auto V",
        "gtaiv" to "Grand Theft Auto IV",
        "gta iv" to "Grand Theft Auto IV",
        "gta 4" to "Grand Theft Auto IV",
        "gta4" to "Grand Theft Auto IV",
        "gta sa" to "Grand Theft Auto: San Andreas",
        "gtasa" to "Grand Theft Auto: San Andreas",
        "gta vc" to "Grand Theft Auto: Vice City",
        "gtavc" to "Grand Theft Auto: Vice City",

        // Red Dead Redemption
        "rdr" to "Red Dead Redemption",
        "rdr2" to "Red Dead Redemption 2",
        "rdr 2" to "Red Dead Redemption 2",
        "red dead" to "Red Dead Redemption",

        // Counter-Strike
        "cs" to "Counter-Strike",
        "csgo" to "Counter-Strike",
        "cs go" to "Counter-Strike",
        "cs2" to "Counter-Strike",
        "cs 2" to "Counter-Strike",

        // Call of Duty
        "cod" to "Call of Duty",
        "cod mw" to "Call of Duty: Modern Warfare",
        "cod mw2" to "Call of Duty: Modern Warfare II",
        "cod mw3" to "Call of Duty: Modern Warfare III",
        "mw" to "Modern Warfare",
        "mw2" to "Modern Warfare II",
        "mw3" to "Modern Warfare III",
        "cod bo" to "Call of Duty: Black Ops",
        "cod bo6" to "Call of Duty: Black Ops 6",
        "bo6" to "Black Ops 6",
        "bo3" to "Black Ops III",
        "warzone" to "Call of Duty: Warzone",

        // Minecraft
        "mc" to "Minecraft",

        // Cyberpunk
        "cp2077" to "Cyberpunk 2077",
        "cp 2077" to "Cyberpunk 2077",
        "cyberpunk" to "Cyberpunk 2077",

        // Baldur's Gate
        "bg3" to "Baldur's Gate 3",
        "bg 3" to "Baldur's Gate 3",
        "baldurs gate" to "Baldur's Gate 3",
        "baldurs gate 3" to "Baldur's Gate 3",

        // FromSoftware
        "er" to "Elden Ring",
        "elden ring" to "Elden Ring",
        "ds" to "Dark Souls",
        "ds1" to "Dark Souls",
        "ds2" to "Dark Souls II",
        "ds3" to "Dark Souls III",
        "sekiro" to "Sekiro: Shadows Die Twice",
        "bloodborne" to "Bloodborne",

        // The Witcher
        "tw3" to "The Witcher 3",
        "witcher" to "The Witcher 3",
        "witcher 3" to "The Witcher 3: Wild Hunt",

        // God of War
        "gow" to "God of War",
        "gow ragnarok" to "God of War Ragnarok",

        // Assassin's Creed
        "ac" to "Assassin's Creed",
        "ac valhalla" to "Assassin's Creed Valhalla",
        "ac mirage" to "Assassin's Creed Mirage",
        "ac odyssey" to "Assassin's Creed Odyssey",
        "ac origins" to "Assassin's Creed Origins",

        // Resident Evil
        "re" to "Resident Evil",
        "re2" to "Resident Evil 2",
        "re3" to "Resident Evil 3",
        "re4" to "Resident Evil 4",
        "re7" to "Resident Evil 7",
        "re8" to "Resident Evil Village",
        "re village" to "Resident Evil Village",

        // Battlefield
        "bf" to "Battlefield",
        "bf1" to "Battlefield 1",
        "bfv" to "Battlefield V",
        "bf 2042" to "Battlefield 2042",
        "bf2042" to "Battlefield 2042",

        // Escape from Tarkov
        "eft" to "Escape from Tarkov",
        "tarkov" to "Escape from Tarkov",

        // Rainbow Six
        "r6" to "Rainbow Six Siege",
        "r6s" to "Rainbow Six Siege",
        "siege" to "Rainbow Six Siege",

        // Team Fortress & Valve
        "tf2" to "Team Fortress 2",
        "hl" to "Half-Life",
        "hl2" to "Half-Life 2",
        "l4d" to "Left 4 Dead",
        "l4d2" to "Left 4 Dead 2",
        "portal" to "Portal 2",

        // PUBG & Battle Royale
        "pubg" to "PUBG: BATTLEGROUNDS",
        "apex" to "Apex Legends",
        "fortnite" to "Fortnite",

        // Dead by Daylight
        "dbd" to "Dead by Daylight",

        // PAYDAY
        "pd2" to "PAYDAY 2",
        "pd3" to "PAYDAY 3",
        "payday" to "PAYDAY 2",

        // Helldivers
        "hd2" to "HELLDIVERS 2",
        "helldivers" to "HELLDIVERS 2",

        // Black Myth Wukong
        "bmw" to "Black Myth: Wukong",
        "wukong" to "Black Myth: Wukong",

        // Civilization
        "civ" to "Civilization VI",
        "civ6" to "Civilization VI",
        "civ 6" to "Civilization VI",

        // Sports / Racing
        "fm" to "Football Manager",
        "fm24" to "Football Manager 2024",
        "fifa" to "EA SPORTS FC",
        "eafc" to "EA SPORTS FC",
        "fc24" to "EA SPORTS FC 24",
        "fc25" to "EA SPORTS FC 25",
        "nfs" to "Need for Speed",
        "rl" to "Rocket League",

        // Monster Hunter
        "mh" to "Monster Hunter: World",
        "mhw" to "Monster Hunter: World",
        "mhr" to "Monster Hunter Rise",

        // The Elder Scrolls & Fallout
        "tes" to "The Elder Scrolls",
        "skyrim" to "The Elder Scrolls V: Skyrim",
        "oblivion" to "The Elder Scrolls IV: Oblivion",
        "fo4" to "Fallout 4",
        "fallout" to "Fallout 4",

        // Borderlands & Bioshock
        "bl" to "Borderlands",
        "bl2" to "Borderlands 2",
        "bl3" to "Borderlands 3",
        "bioshock" to "BioShock Remastered"
    )

    /**
     * Sanitizes input and maps aliases to canonical names.
     */
    fun normalize(rawQuery: String): NormalizedQueryInfo {
        val trimmed = rawQuery.trim()
        if (trimmed.isBlank()) {
            return NormalizedQueryInfo(rawQuery, "", "")
        }

        // Clean apostrophes, quotes, and punctuation
        val lower = trimmed.lowercase(Locale.ROOT)
        val cleanNoQuotes = lower.replace("'", "")
            .replace("’", "")
            .replace("`", "")
            .replace(":", " ")
            .replace("-", " ")
            .replace(Regex("\\s+"), " ")
            .trim()

        val cleanAlphanumericOnly = lower.filter { it.isLetterOrDigit() || it.isWhitespace() }
            .replace(Regex("\\s+"), " ")
            .trim()

        // 1. Direct match in alias map
        val alias = ALIAS_MAP[lower]
            ?: ALIAS_MAP[cleanNoQuotes]
            ?: ALIAS_MAP[cleanAlphanumericOnly]

        val resolved = alias ?: trimmed.replace(Regex("\\s+"), " ")

        return NormalizedQueryInfo(
            rawQuery = trimmed,
            sanitizedQuery = cleanNoQuotes,
            resolvedQuery = resolved,
            matchedAlias = alias
        )
    }

    /**
     * Encodes query string safely matching JavaScript's encodeURIComponent(),
     * preventing HTTP 400.
     */
    fun safeUrlEncode(input: String): String {
        return try {
            URLEncoder.encode(input.trim(), StandardCharsets.UTF_8.name())
                .replace("+", "%20")
        } catch (_: Exception) {
            input.trim()
        }
    }

    /**
     * Ranks search results by relevance and popularity:
     * - Highest priority: Exact match or starts with target game
     * - High priority: Core flagship game title (e.g. GTA V, RDR 2, Garry's Mod)
     * - Low priority / penalty: Soundtracks, DLCs, passes, skins, artbooks
     */
    fun rankResults(
        results: List<GameSearchResult>,
        rawQuery: String,
        resolvedQuery: String
    ): List<GameSearchResult> {
        if (results.isEmpty()) return results

        val rawLower = rawQuery.trim().lowercase(Locale.ROOT)
        val resolvedLower = resolvedQuery.trim().lowercase(Locale.ROOT)
        val rawNoPunct = rawLower.filter { it.isLetterOrDigit() }
        val resolvedNoPunct = resolvedLower.filter { it.isLetterOrDigit() }

        val dlcKeywords = listOf(
            "soundtrack", "ost", "dlc", "expansion", "artbook", "upgrade pack",
            "season pass", "battle pass", "skin pack", "bonus pack", "demo"
        )

        return results.sortedByDescending { item ->
            var score = 0
            val titleLower = item.title.lowercase(Locale.ROOT)
            val titleNoPunct = titleLower.filter { it.isLetterOrDigit() }

            // 1. Exact match
            if (titleLower == resolvedLower || titleLower == rawLower ||
                titleNoPunct == resolvedNoPunct || titleNoPunct == rawNoPunct
            ) {
                score += 2000
            }
            // 2. Starts with query / resolved title
            else if (titleLower.startsWith(resolvedLower) || titleLower.startsWith(rawLower)) {
                score += 1200
            }
            // 3. Contains as distinct phrase
            else if (titleLower.contains(resolvedLower) || titleLower.contains(rawLower)) {
                score += 600
            }

            // 4. Heavy penalty for DLCs and soundtracks
            if (dlcKeywords.any { titleLower.contains(it) }) {
                score -= 800
            }

            // 5. Popularity bonuses for prominent flagship games
            if (rawLower.startsWith("gta") || resolvedLower.contains("grand theft auto")) {
                if (titleLower.contains("grand theft auto v") || titleLower.contains("gta v")) {
                    score += 500
                }
            }
            if (rawLower.startsWith("rdr") || resolvedLower.contains("red dead")) {
                if (titleLower.contains("red dead redemption 2")) {
                    score += 400
                }
            }
            if (rawLower == "gmod" || resolvedLower == "garry's mod") {
                if (titleLower == "garry's mod" || titleNoPunct == "garrysmod") {
                    score += 1500
                }
            }

            // 6. Prefer items with Steam App ID (official released title)
            if (!item.steamAppId.isNullOrBlank()) {
                score += 50
            }

            score
        }
    }
}
