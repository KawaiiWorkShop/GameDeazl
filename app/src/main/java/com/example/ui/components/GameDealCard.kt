package com.example.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.GameDeal
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.DarkCardBg
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.GreenDiscount
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarningAmber

@Composable
fun GameDealCard(
    deal: GameDeal,
    isTracked: Boolean,
    onToggleTrack: () -> Unit,
    onCardClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("game_deal_card_${deal.gameId}")
            .clickable { onCardClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = DarkCardBg
        ),
        border = BorderStroke(1.dp, DarkCardBorder)
    ) {
        Column {
            // Header Image with Badges
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(145.dp)
                    .background(Color(0xFF0F121C))
            ) {
                AsyncImage(
                    model = deal.thumbUrl,
                    contentDescription = deal.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Dark gradient overlay at bottom of image
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.35f),
                                    Color.Transparent,
                                    DarkCardBg.copy(alpha = 0.95f)
                                )
                            )
                        )
                )

                // Top left store badge
                StoreBadge(
                    storeType = deal.storeType,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(10.dp)
                )

                // Top right discount tag
                if (deal.savingsPercent > 0 || deal.isFree) {
                    DiscountBadge(
                        savingsPercent = deal.savingsPercent,
                        isFree = deal.isFree,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(10.dp)
                    )
                }
            }

            // Body
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                // Title
                Text(
                    text = deal.title,
                    color = TextPrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Ratings row (Metacritic & Steam Ratings)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (deal.metacriticScore != null && deal.metacriticScore > 0) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    if (deal.metacriticScore >= 75) GreenDiscount.copy(alpha = 0.2f)
                                    else WarningAmber.copy(alpha = 0.2f)
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Metacritic ${deal.metacriticScore}",
                                color = if (deal.metacriticScore >= 75) GreenDiscount else WarningAmber,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    if (deal.steamRatingPercent != null && deal.steamRatingPercent > 0) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Rating",
                                tint = WarningAmber,
                                modifier = Modifier.size(13.dp)
                            )
                            val reviewText = if (deal.steamRatingCount != null && deal.steamRatingCount > 0) {
                                if (deal.steamRatingCount >= 1000) "${deal.steamRatingCount / 1000}k inceleme"
                                else "${deal.steamRatingCount} inceleme"
                            } else deal.steamRatingText ?: "Olumlu"
                            Text(
                                text = "%${deal.steamRatingPercent} ($reviewText)",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Price Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Prices
                    Row(verticalAlignment = Alignment.Bottom) {
                        if (deal.isFree) {
                            Text(
                                text = "Ücretsiz",
                                color = CyanAccent,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        } else {
                            Text(
                                text = "$${deal.salePrice}",
                                color = CyanAccent,
                                fontSize = 21.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            if (deal.normalPrice > deal.salePrice) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "$${deal.normalPrice}",
                                    color = TextMuted,
                                    fontSize = 14.sp,
                                    textDecoration = TextDecoration.LineThrough,
                                    modifier = Modifier.padding(bottom = 2.dp)
                                )
                            }
                        }
                    }

                    // External Store Link Icon
                    IconButton(
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(deal.dealUrl))
                            context.startActivity(intent)
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.OpenInNew,
                            contentDescription = "Mağazada Aç",
                            tint = TextSecondary,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Action Buttons Row: "İndirimi Haber Ver" / "Takip Ediliyor"
                val buttonBg by animateColorAsState(
                    targetValue = if (isTracked) CyanAccent.copy(alpha = 0.15f) else Color.Transparent,
                    label = "trackBtnBg"
                )

                if (isTracked) {
                    FilledTonalButton(
                        onClick = onToggleTrack,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                            .testTag("track_button_${deal.gameId}"),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = buttonBg,
                            contentColor = CyanAccent
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = "Takip Ediliyor",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Takip Ediliyor (İndirim Haberi Açık)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                } else {
                    OutlinedButton(
                        onClick = onToggleTrack,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                            .testTag("track_button_${deal.gameId}"),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, CyanAccent.copy(alpha = 0.6f)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = CyanAccent
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.BookmarkBorder,
                            contentDescription = "İndirimi Haber Ver",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "İndirimi Haber Ver (Takip Et)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}
