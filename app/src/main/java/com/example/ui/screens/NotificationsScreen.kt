package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.local.PriceNotificationEntity
import com.example.ui.components.DiscountBadge
import com.example.ui.theme.AmoledBackground
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.DarkCardBg
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun NotificationsScreen(
    notifications: List<PriceNotificationEntity>,
    onMarkAllRead: () -> Unit,
    onClearAll: () -> Unit,
    onNotificationClick: (PriceNotificationEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AmoledBackground)
    ) {
        // Top Action Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkSurface)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "İndirim Bildirimleri (${notifications.size})",
                color = TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )

            Row {
                if (notifications.any { !it.isRead }) {
                    TextButton(onClick = onMarkAllRead) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = CyanAccent,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Okundu Say",
                            color = CyanAccent,
                            fontSize = 12.sp
                        )
                    }
                }

                if (notifications.isNotEmpty()) {
                    IconButton(onClick = onClearAll) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "Temizle",
                            tint = TextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // Notification list
        if (notifications.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.NotificationsNone,
                    contentDescription = null,
                    tint = TextMuted.copy(alpha = 0.5f),
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Henüz yeni bir fiyat bildirimi yok",
                    color = TextPrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Takip ettiğiniz oyunlarda fiyat indirimi tespit edildiğinde burada görünecektir.",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("notifications_list"),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(
                    items = notifications,
                    key = { it.id }
                ) { notif ->
                    NotificationCard(
                        notification = notif,
                        onClick = { onNotificationClick(notif) }
                    )
                }
            }
        }
    }
}

@Composable
private fun NotificationCard(
    notification: PriceNotificationEntity,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val formattedTime = remember(notification.timestamp) {
        val sdf = SimpleDateFormat("dd MMM, HH:mm", Locale("tr"))
        sdf.format(Date(notification.timestamp))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("notification_card_${notification.id}")
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (notification.isRead) DarkCardBg else Color(0xFF131A28)
        ),
        border = BorderStroke(
            1.dp,
            if (notification.isRead) DarkCardBorder else CyanAccent.copy(alpha = 0.4f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Unread indicator dot
            if (!notification.isRead) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(CyanAccent)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }

            // Thumbnail
            Box(
                modifier = Modifier
                    .size(width = 64.dp, height = 44.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF0F121C))
            ) {
                AsyncImage(
                    model = notification.thumbUrl,
                    contentDescription = notification.gameTitle,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Body
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = notification.gameTitle,
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = formattedTime,
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }

                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "${notification.storeName} mağazasında indirim başladı!",
                    color = TextSecondary,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "$${notification.newPrice}",
                        color = CyanAccent,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (notification.oldPrice > notification.newPrice) {
                        Text(
                            text = "$${notification.oldPrice}",
                            color = TextMuted,
                            fontSize = 12.sp,
                            textDecoration = TextDecoration.LineThrough
                        )
                    }
                    if (notification.savingsPercent > 0) {
                        DiscountBadge(savingsPercent = notification.savingsPercent)
                    }
                }
            }

            // Open deal
            if (notification.dealId != null) {
                IconButton(
                    onClick = {
                        val url = "https://www.cheapshark.com/redirect?dealID=${notification.dealId}"
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        context.startActivity(intent)
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.OpenInNew,
                        contentDescription = "Mağazada Aç",
                        tint = CyanAccent,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
