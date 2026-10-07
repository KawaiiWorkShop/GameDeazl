package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.GreenDiscount

@Composable
fun DiscountBadge(
    savingsPercent: Int,
    isFree: Boolean = false,
    modifier: Modifier = Modifier
) {
    val text = if (isFree) "ÜCRETSİZ" else "-$savingsPercent%"
    val bgColor = if (isFree) Color(0xFF8B5CF6) else GreenDiscount

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .padding(horizontal = 7.dp, vertical = 3.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.Black,
            fontSize = 12.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}
