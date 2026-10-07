package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.StoreType
import com.example.ui.theme.EpicBg
import com.example.ui.theme.EpicColor
import com.example.ui.theme.SteamBg
import com.example.ui.theme.SteamColor

@Composable
fun StoreBadge(
    storeType: StoreType,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, icon, label) = when (storeType) {
        StoreType.STEAM -> Quadruple(
            SteamBg,
            SteamColor,
            Icons.Default.SportsEsports,
            "STEAM"
        )
        StoreType.EPIC -> Quadruple(
            EpicBg,
            EpicColor,
            Icons.Default.Bolt,
            "EPIC GAMES"
        )
        StoreType.OTHER -> Quadruple(
            Color(0xFF2D3748),
            Color(0xFFE2E8F0),
            Icons.Default.SportsEsports,
            "MAĞAZA"
        )
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = textColor,
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                color = textColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }
    }
}

private data class Quadruple<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)
