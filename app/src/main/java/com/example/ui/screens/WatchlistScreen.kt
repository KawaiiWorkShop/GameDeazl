package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkRemove
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.local.TrackedGameEntity
import com.example.ui.components.DiscountBadge
import com.example.ui.theme.AmoledBackground
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.DarkCardBg
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.GreenDiscount
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun WatchlistScreen(
    trackedGames: List<TrackedGameEntity>,
    isRefreshingPrices: Boolean,
    refreshFeedback: String?,
    onRefreshPrices: () -> Unit,
    onUntrackGame: (String) -> Unit,
    onUpdateTargetPrice: (String, Double?) -> Unit,
    onGameClick: (String) -> Unit,
    onDismissFeedback: () -> Unit,
    modifier: Modifier = Modifier
) {
    var editingTargetGame by remember { mutableStateOf<TrackedGameEntity?>(null) }
    val discountedCount = remember(trackedGames) {
        trackedGames.count { it.isDiscounted || it.savingsPercent > 0 }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AmoledBackground)
    ) {
        // Local Storage Privacy Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkSurface)
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF131A26))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = CyanAccent,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Üyeliksiz Yerel Depolama: Takip listeniz cihazınızda saklanır.",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Stats & Refresh Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "${trackedGames.size} Oyun Takipte",
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "$discountedCount oyunda aktif indirim var",
                    color = if (discountedCount > 0) GreenDiscount else TextMuted,
                    fontSize = 12.sp
                )
            }

            Button(
                onClick = onRefreshPrices,
                enabled = !isRefreshingPrices && trackedGames.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CyanAccent,
                    contentColor = AmoledBackground
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("refresh_prices_button")
            ) {
                if (isRefreshingPrices) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = AmoledBackground,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Taranıyor...", fontSize = 12.sp)
                } else {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Fiyatları Güncelle",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Fiyatları Denetle", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Feedback Banner
        AnimatedVisibility(visible = refreshFeedback != null) {
            refreshFeedback?.let { msg ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F291E)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = msg, color = GreenDiscount, fontSize = 13.sp)
                        IconButton(onClick = onDismissFeedback, modifier = Modifier.size(24.dp)) {
                            Icon(imageVector = Icons.Default.Check, contentDescription = "Kapat", tint = GreenDiscount)
                        }
                    }
                }
            }
        }

        // Content
        if (trackedGames.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.NotificationsActive,
                    contentDescription = null,
                    tint = TextMuted.copy(alpha = 0.5f),
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Henüz takip ettiğiniz oyun yok",
                    color = TextPrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "İndirimler veya Arama ekranından 'İndirimi Haber Ver' butonuna tıklayarak oyun ekleyebilirsiniz.",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("watchlist_items_list"),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = trackedGames,
                    key = { it.gameId }
                ) { game ->
                    TrackedGameCard(
                        game = game,
                        onUntrack = { onUntrackGame(game.gameId) },
                        onEditTarget = { editingTargetGame = game },
                        onClick = { onGameClick(game.gameId) }
                    )
                }
            }
        }
    }

    // Target Price Edit Dialog
    editingTargetGame?.let { game ->
        TargetPriceDialog(
            game = game,
            onDismiss = { editingTargetGame = null },
            onConfirm = { target ->
                onUpdateTargetPrice(game.gameId, target)
                editingTargetGame = null
            }
        )
    }
}

@Composable
private fun TrackedGameCard(
    game: TrackedGameEntity,
    onUntrack: () -> Unit,
    onEditTarget: () -> Unit,
    onClick: () -> Unit
) {
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("tracked_card_${game.gameId}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCardBg),
        border = BorderStroke(1.dp, DarkCardBorder)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Artwork
                Box(
                    modifier = Modifier
                        .size(width = 80.dp, height = 50.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF0F121C))
                ) {
                    AsyncImage(
                        model = game.thumbUrl,
                        contentDescription = game.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = game.title,
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Mağaza: ${game.storeName}",
                        color = TextMuted,
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (game.lastPrice == 0.0) "Ücretsiz" else "$${game.lastPrice}",
                            color = CyanAccent,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (game.savingsPercent > 0) {
                            Spacer(modifier = Modifier.width(6.dp))
                            DiscountBadge(savingsPercent = game.savingsPercent)
                        }
                    }
                }

                // Delete untrack icon
                IconButton(onClick = onUntrack) {
                    Icon(
                        imageVector = Icons.Default.BookmarkRemove,
                        contentDescription = "Takipten Çıkar",
                        tint = Color(0xFFEF4444).copy(alpha = 0.8f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Target Price & Store actions row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF141A26))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = null,
                        tint = CyanAccent,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (game.targetPrice != null) "Hedef Fiyat: $${game.targetPrice}" else "Tüm indirimleri haber ver",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }

                Row {
                    IconButton(onClick = onEditTarget, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Hedef Fiyatı Değiştir",
                            tint = CyanAccent,
                            modifier = Modifier.size(15.dp)
                        )
                    }

                    if (game.dealId != null) {
                        IconButton(
                            onClick = {
                                val url = "https://www.cheapshark.com/redirect?dealID=${game.dealId}"
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                context.startActivity(intent)
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.OpenInNew,
                                contentDescription = "Mağazada Aç",
                                tint = TextSecondary,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TargetPriceDialog(
    game: TrackedGameEntity,
    onDismiss: () -> Unit,
    onConfirm: (Double?) -> Unit
) {
    var priceInput by remember {
        mutableStateOf(game.targetPrice?.toString() ?: "")
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = {
            Text(
                text = "Hedef İndirim Fiyatı",
                color = TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    text = "${game.title} oyunu için belirlediğiniz fiyatın altına düştüğünde bildirim alırsınız.",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = priceInput,
                    onValueChange = { priceInput = it },
                    label = { Text("Hedef Fiyat ($)", color = TextMuted) },
                    placeholder = { Text("Örn: 9.99", color = TextMuted) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanAccent,
                        unfocusedBorderColor = DarkCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Boş bırakırsanız herhangi bir indirim olduğunda haber verilir.",
                    color = TextMuted,
                    fontSize = 11.sp
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val price = priceInput.toDoubleOrNull()
                    onConfirm(price)
                },
                colors = ButtonDefaults.buttonColors(containerColor = CyanAccent)
            ) {
                Text("Kaydet", color = AmoledBackground, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("İptal", color = TextSecondary)
            }
        }
    )
}
