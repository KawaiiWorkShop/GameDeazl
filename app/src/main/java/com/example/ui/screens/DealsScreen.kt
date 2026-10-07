package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.TrackedGameEntity
import com.example.model.GameDeal
import com.example.model.SortOption
import com.example.model.StoreFilter
import com.example.ui.components.GameDealCard
import com.example.ui.theme.AmoledBackground
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material3.OutlinedButton
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import coil.compose.AsyncImage
import com.example.ui.components.StoreBadge
import com.example.ui.theme.GreenDiscount
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.DealsUiState

@Composable
fun DealsScreen(
    state: DealsUiState,
    trackedGames: List<TrackedGameEntity>,
    onFilterSelected: (StoreFilter) -> Unit,
    onSortSelected: (SortOption) -> Unit,
    onToggleTrack: (GameDeal) -> Unit,
    onCardClick: (GameDeal) -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    val trackedIds = remember(trackedGames) {
        trackedGames.map { it.gameId }.toSet()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AmoledBackground)
    ) {
        // Top filter bar (Steam / Epic Games / Tümü)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkSurface)
                .padding(vertical = 10.dp)
        ) {
            // Store Filters Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                StoreFilter.values().forEach { filter ->
                    val isSelected = state.filter == filter
                    FilterChip(
                        selected = isSelected,
                        onClick = { onFilterSelected(filter) },
                        label = {
                            Text(
                                text = filter.label,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CyanAccent,
                            selectedLabelColor = AmoledBackground,
                            containerColor = Color(0xFF141824),
                            labelColor = TextSecondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = DarkCardBorder,
                            selectedBorderColor = CyanAccent
                        ),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.testTag("filter_chip_${filter.name}")
                    )
                }

                IconButton(
                    onClick = onRefresh,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Yenile",
                        tint = CyanAccent,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Sort Options Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.FilterList,
                    contentDescription = "Sıralama",
                    tint = TextMuted,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                SortOption.values().forEach { sort ->
                    val isSelected = state.sort == sort
                    FilterChip(
                        selected = isSelected,
                        onClick = { onSortSelected(sort) },
                        label = {
                            Text(
                                text = sort.label,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF1E293B),
                            selectedLabelColor = CyanAccent,
                            containerColor = Color.Transparent,
                            labelColor = TextMuted
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if (isSelected) CyanAccent else Color.Transparent,
                            selectedBorderColor = CyanAccent
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("sort_chip_${sort.name}")
                    )
                }
            }
        }

        // Content Area
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            when {
                state.isLoading && state.deals.isEmpty() -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            color = CyanAccent,
                            modifier = Modifier.size(44.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Steam ve Epic Games indirimleri alınıyor...",
                            color = TextSecondary,
                            fontSize = 14.sp
                        )
                    }
                }

                state.error != null && state.deals.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = state.error,
                            color = TextSecondary,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = onRefresh,
                            colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(text = "Tekrar Dene", color = AmoledBackground, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                else -> {
                    val freeGames = remember(state.freeDeals, state.deals) {
                        (state.freeDeals + state.deals.filter { it.isFree || it.savingsPercent >= 100 }).distinctBy { it.gameId }
                    }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("deals_list"),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // %100 Discount (Free) Games Showcase at the top of the home page
                        if (freeGames.isNotEmpty()) {
                            item(key = "free_games_showcase") {
                                val context = LocalContext.current
                                FreeGamesShowcaseSection(
                                    freeGames = freeGames,
                                    trackedIds = trackedIds,
                                    onToggleTrack = onToggleTrack,
                                    onCardClick = onCardClick,
                                    onOpenUrl = { url ->
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                        context.startActivity(intent)
                                    }
                                )
                            }
                        } else if (state.sort == SortOption.SAVINGS) {
                            item(key = "savings_header_banner") {
                                SavingsHeaderBanner()
                            }
                        }

                        // Main deals list sorted descending by discount percentage (%100, %90, %80... %10)
                        items(
                            items = state.deals,
                            key = { it.dealId }
                        ) { deal ->
                            GameDealCard(
                                deal = deal,
                                isTracked = trackedIds.contains(deal.gameId),
                                onToggleTrack = { onToggleTrack(deal) },
                                onCardClick = { onCardClick(deal) }
                            )
                        }

                        // Special Event & Seasonal Sales Section (Özel Gün ve Mevsimsel İndirimler)
                        item(key = "special_event_sales") {
                            val context = LocalContext.current
                            SpecialEventSalesSection(
                                onOpenUrl = { url ->
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                    context.startActivity(intent)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SpecialEventSalesSection(
    onOpenUrl: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 20.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Celebration,
                contentDescription = null,
                tint = CyanAccent,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Özel Gün & Mevsimsel İndirimler",
                color = TextPrimary,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Text(
            text = "Steam ve Epic Games'in aktif indirim festivalleri ve yayıncı etkinlikleri",
            color = TextSecondary,
            fontSize = 12.sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Event 1: Steam Seasonal & Festivals
        EventSaleCard(
            title = "Steam Büyük İndirim Festivalleri",
            subtitle = "Yaz/Kış indirimleri, Cadılar Bayramı, Next Fest ve yayıncı haftasonu fırsatları.",
            storeLabel = "Steam İndirim Merkezi",
            storeIcon = Icons.Default.SportsEsports,
            accentColor = CyanAccent,
            onOpen = { onOpenUrl("https://store.steampowered.com/specials") }
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Event 2: Epic Games Mega Sale & Weekly Free Games
        EventSaleCard(
            title = "Epic Games Mega Sale & Ücretsiz Oyunlar",
            subtitle = "Haftalık %100 ücretsiz oyun dağıtımları ve %33 indirim kuponlu Mega Sale.",
            storeLabel = "Epic Games Fırsatları",
            storeIcon = Icons.Default.Bolt,
            accentColor = Color(0xFFFFFFFF),
            onOpen = { onOpenUrl("https://store.epicgames.com/tr/free-games") }
        )
    }
}

@Composable
private fun EventSaleCard(
    title: String,
    subtitle: String,
    storeLabel: String,
    storeIcon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    onOpen: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF121622)),
        border = BorderStroke(1.dp, Color(0xFF232A3B))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = title,
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(accentColor.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "CANLI ETKİNLİK",
                        color = accentColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                color = TextSecondary,
                fontSize = 12.sp,
                lineHeight = 17.sp
            )

            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onOpen,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1B2232),
                    contentColor = TextPrimary
                ),
                border = BorderStroke(1.dp, accentColor.copy(alpha = 0.4f))
            ) {
                Icon(
                    imageVector = storeIcon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = storeLabel,
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.Default.OpenInNew,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

/**
 * Rule 2: %100 Discount (Free) Games Showcase panel
 */
@Composable
private fun FreeGamesShowcaseSection(
    freeGames: List<GameDeal>,
    trackedIds: Set<String>,
    onToggleTrack: (GameDeal) -> Unit,
    onCardClick: (GameDeal) -> Unit,
    onOpenUrl: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("free_games_showcase"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF091A14)
        ),
        border = BorderStroke(1.5.dp, GreenDiscount)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(GreenDiscount, RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CardGiftcard,
                            contentDescription = null,
                            tint = AmoledBackground,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Günün Ücretsiz Oyunları",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "%100 İndirimli & Bedava Fırsatlar",
                            color = GreenDiscount,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .background(GreenDiscount, RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "%100 ÜCRETSİZ",
                        color = AmoledBackground,
                        fontWeight = FontWeight.Black,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                freeGames.forEach { deal ->
                    FreeGameItemCard(
                        deal = deal,
                        isTracked = trackedIds.contains(deal.gameId),
                        onToggleTrack = { onToggleTrack(deal) },
                        onCardClick = { onCardClick(deal) },
                        onClaim = { onOpenUrl(deal.dealUrl) }
                    )
                }
            }
        }
    }
}

@Composable
private fun FreeGameItemCard(
    deal: GameDeal,
    isTracked: Boolean,
    onToggleTrack: () -> Unit,
    onCardClick: () -> Unit,
    onClaim: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onCardClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF10241C)
        ),
        border = BorderStroke(1.dp, Color(0xFF1E3A2E))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail
            Box(
                modifier = Modifier
                    .size(width = 84.dp, height = 56.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF0A1410))
            ) {
                AsyncImage(
                    model = deal.thumbUrl,
                    contentDescription = deal.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Info & actions
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    StoreBadge(storeType = deal.storeType)
                    Box(
                        modifier = Modifier
                            .background(GreenDiscount, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "%100 İNDİRİM",
                            color = AmoledBackground,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = deal.title,
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (deal.normalPrice > 0) {
                        Text(
                            text = "$${"%.2f".format(deal.normalPrice)}",
                            color = TextMuted,
                            fontSize = 12.sp,
                            textDecoration = TextDecoration.LineThrough
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Text(
                        text = "ÜCRETSİZ ($0.00)",
                        color = GreenDiscount,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onClaim,
                        modifier = Modifier
                            .weight(1f)
                            .height(34.dp),
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GreenDiscount,
                            contentColor = AmoledBackground
                        ),
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.OpenInNew,
                            contentDescription = null,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${deal.storeType.displayName}'de Kap",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }

                    OutlinedButton(
                        onClick = onToggleTrack,
                        modifier = Modifier.height(34.dp),
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(1.dp, if (isTracked) CyanAccent else Color(0xFF2C3E35)),
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Icon(
                            imageVector = if (isTracked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = null,
                            tint = if (isTracked) CyanAccent else TextSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SavingsHeaderBanner(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF0F172A)
        ),
        border = BorderStroke(1.dp, Color(0xFF1E293B))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(Color(0xFF1E293B), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.LocalOffer,
                    contentDescription = null,
                    tint = CyanAccent,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "En Yüksek İndirim Oranına Göre Sıralı",
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "%100, %90, %80... şeklinde azalan sırada listeleniyor",
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }
        }
    }
}

