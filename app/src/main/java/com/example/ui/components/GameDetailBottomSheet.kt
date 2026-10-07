package com.example.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.DeveloperBoard
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.GameDetails
import com.example.model.StoreDealInfo
import com.example.model.StoreType
import com.example.model.SystemRequirements
import com.example.ui.theme.AmoledBackground
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.DarkCardBg
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.EpicBg
import com.example.ui.theme.EpicColor
import com.example.ui.theme.GreenDiscount
import com.example.ui.theme.SteamBg
import com.example.ui.theme.SteamColor
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarningAmber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameDetailBottomSheet(
    details: GameDetails,
    isTracked: Boolean,
    onToggleTrack: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedReqTab by remember { mutableIntStateOf(0) } // 0: Minimum, 1: Önerilen

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = AmoledBackground,
        scrimColor = Color.Black.copy(alpha = 0.85f),
        dragHandle = null,
        modifier = modifier.testTag("game_detail_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 36.dp)
        ) {
            // Hero Banner with dark gradient overlay
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp)
                    .background(Color(0xFF0A0D14))
            ) {
                AsyncImage(
                    model = details.bannerUrl ?: details.thumbUrl,
                    contentDescription = details.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // AMOLED Gradient fade into black
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.4f),
                                    Color.Transparent,
                                    AmoledBackground.copy(alpha = 0.95f),
                                    AmoledBackground
                                )
                            )
                        )
                )

                // Close button
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(14.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.Black.copy(alpha = 0.7f))
                        .size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Kapat",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Body Content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp)
            ) {
                // Title
                Text(
                    text = details.title,
                    color = TextPrimary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    lineHeight = 28.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Metadata Badges Row (Metacritic, Release Date, Steam ID)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Metacritic Badge
                    if (details.metacriticScore != null && details.metacriticScore > 0) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    if (details.metacriticScore >= 75) GreenDiscount.copy(alpha = 0.2f)
                                    else WarningAmber.copy(alpha = 0.2f)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "🏆 Metacritic ${details.metacriticScore}",
                                color = if (details.metacriticScore >= 75) GreenDiscount else WarningAmber,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Release Date Badge
                    if (!details.releaseDate.isNullOrBlank()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF161B28))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = null,
                                tint = TextSecondary,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = details.releaseDate,
                                color = TextSecondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // Description (Özet)
                if (!details.description.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Oyun Özeti",
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = details.description,
                        color = TextSecondary,
                        fontSize = 13.sp,
                        lineHeight = 19.sp
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // SECTION 1: Store Redirection & Direct Links (Mağazaya Yönlendirme)
                Text(
                    text = "Mağaza Fırsatları & Yönlendirme",
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(10.dp))

                if (details.deals.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = DarkCardBg),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, DarkCardBorder)
                    ) {
                        Text(
                            text = "Steam ve Epic Games üzerinde şu an listelenmiş aktif indirim bulunamadı.",
                            color = TextMuted,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(14.dp)
                        )
                    }
                } else {
                    details.deals.forEach { deal ->
                        StoreActionCard(
                            deal = deal,
                            onOpenStore = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(deal.dealUrl))
                                context.startActivity(intent)
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                // Historical Lowest Price card
                if (details.cheapestPriceEver != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = DarkCardBg),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, DarkCardBorder)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(CyanAccent.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.History,
                                    contentDescription = "Geçmiş",
                                    tint = CyanAccent,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Tüm Zamanların En Düşük Fiyatı",
                                    color = TextMuted,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = "$${details.cheapestPriceEver}",
                                    color = GreenDiscount,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // SECTION 2: System Requirements (Sistem Gereksinimleri)
                Text(
                    text = "Sistem Gereksinimleri",
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                val hasMin = details.minRequirements != null
                val hasRec = details.recRequirements != null

                if (hasMin || hasRec) {
                    // Tab Bar: Minimum vs Önerilen
                    TabRow(
                        selectedTabIndex = selectedReqTab,
                        containerColor = DarkSurface,
                        contentColor = CyanAccent,
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[selectedReqTab]),
                                color = CyanAccent
                            )
                        },
                        modifier = Modifier.clip(RoundedCornerShape(8.dp))
                    ) {
                        Tab(
                            selected = selectedReqTab == 0,
                            onClick = { selectedReqTab = 0 },
                            text = {
                                Text(
                                    text = "⚡ Minimum",
                                    fontSize = 13.sp,
                                    fontWeight = if (selectedReqTab == 0) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedReqTab == 0) CyanAccent else TextSecondary
                                )
                            }
                        )
                        Tab(
                            selected = selectedReqTab == 1,
                            onClick = { selectedReqTab = 1 },
                            text = {
                                Text(
                                    text = "🚀 Önerilen",
                                    fontSize = 13.sp,
                                    fontWeight = if (selectedReqTab == 1) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedReqTab == 1) CyanAccent else TextSecondary
                                )
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    val activeReq = if (selectedReqTab == 0) details.minRequirements else details.recRequirements
                    if (activeReq != null) {
                        SystemRequirementsContent(req = activeReq)
                    } else {
                        Text(
                            text = if (selectedReqTab == 1) "Bu oyun için önerilen sistem gereksinimleri belirtilmemiş."
                            else "Minimum sistem gereksinimleri belirtilmemiş.",
                            color = TextMuted,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    }
                } else {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = DarkCardBg),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, DarkCardBorder)
                    ) {
                        Text(
                            text = "Bu oyunun sistem gereksinimleri mağaza sayfasında mevcuttur. Detaylar için yukarıdaki mağaza butonuna tıklayabilirsiniz.",
                            color = TextMuted,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(14.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // SECTION 3: Watchlist Tracking Button (Takip Et)
                if (isTracked) {
                    OutlinedButton(
                        onClick = onToggleTrack,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("toggle_tracking_sheet_btn"),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.6f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444))
                    ) {
                        Text(text = "Takip Listesinden Çıkar", fontWeight = FontWeight.SemiBold)
                    }
                } else {
                    Button(
                        onClick = onToggleTrack,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("toggle_tracking_sheet_btn"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CyanAccent,
                            contentColor = AmoledBackground
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = "Takip Et",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "İndirimi Haber Ver (Takip Et)", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

/**
 * Platform-specific Store Redirection Card (Steam & Epic Games)
 */
@Composable
private fun StoreActionCard(
    deal: StoreDealInfo,
    onOpenStore: () -> Unit
) {
    val isSteam = deal.storeType == StoreType.STEAM
    val buttonBg = if (isSteam) SteamBg else EpicBg
    val buttonTextColor = if (isSteam) SteamColor else EpicColor
    val storeLabel = if (isSteam) "Steam'de İncele" else "Epic Games'te İncele"
    val storeIcon = if (isSteam) Icons.Default.SportsEsports else Icons.Default.Bolt

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCardBg),
        border = BorderStroke(1.dp, if (isSteam) SteamColor.copy(alpha = 0.35f) else DarkCardBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Price & Savings Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StoreBadge(storeType = deal.storeType)

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (deal.retailPrice > deal.price) {
                        Text(
                            text = "$${deal.retailPrice}",
                            color = TextMuted,
                            fontSize = 13.sp,
                            textDecoration = TextDecoration.LineThrough
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        text = if (deal.price == 0.0) "Ücretsiz" else "$${deal.price}",
                        color = CyanAccent,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    if (deal.savingsPercent > 0) {
                        Spacer(modifier = Modifier.width(8.dp))
                        DiscountBadge(savingsPercent = deal.savingsPercent)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Direct Store Redirection Button
            Button(
                onClick = onOpenStore,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("open_store_button_${deal.storeType.name}"),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = buttonBg,
                    contentColor = buttonTextColor
                ),
                border = BorderStroke(1.dp, buttonTextColor.copy(alpha = 0.4f))
            ) {
                Icon(
                    imageVector = storeIcon,
                    contentDescription = null,
                    tint = buttonTextColor,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = storeLabel,
                    color = buttonTextColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.Default.OpenInNew,
                    contentDescription = null,
                    tint = buttonTextColor.copy(alpha = 0.7f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

/**
 * Clean System Requirements Specification Cards (OS, CPU, RAM, GPU, Storage)
 */
@Composable
private fun SystemRequirementsContent(req: SystemRequirements) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // OS
        req.os?.let {
            RequirementSpecItem(icon = Icons.Default.Computer, label = "İşletim Sistemi (OS)", value = it)
        }
        // CPU
        req.processor?.let {
            RequirementSpecItem(icon = Icons.Default.Speed, label = "İşlemci (CPU)", value = it)
        }
        // RAM
        req.memory?.let {
            RequirementSpecItem(icon = Icons.Default.Memory, label = "Bellek (RAM)", value = it)
        }
        // GPU
        req.graphics?.let {
            RequirementSpecItem(icon = Icons.Default.DeveloperBoard, label = "Ekran Kartı (GPU)", value = it)
        }
        // Storage
        req.storage?.let {
            RequirementSpecItem(icon = Icons.Default.Save, label = "Depolama (Disk)", value = it)
        }

        // Fallback: If no structured keys were parsed, show clean raw text
        if (req.os == null && req.processor == null && req.memory == null && req.graphics == null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCardBg),
                border = BorderStroke(1.dp, DarkCardBorder)
            ) {
                Text(
                    text = req.rawText,
                    color = TextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
    }
}

@Composable
private fun RequirementSpecItem(
    icon: ImageVector,
    label: String,
    value: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCardBg),
        border = BorderStroke(1.dp, DarkCardBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF151B28)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = CyanAccent,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    color = TextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = value,
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
