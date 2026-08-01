package com.pamneuroncraft.jobapplicationtracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PremiumBadge(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(Color(0xFFFFD700).copy(alpha = 0.2f))
            .padding(horizontal = 4.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Icon(
            imageVector = Icons.Default.AutoAwesome,
            contentDescription = null,
            modifier = Modifier.size(10.dp),
            tint = Color(0xFFFFD700)
        )
        Text(
            text = "PRO",
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFFD700)
            )
        )
    }
}

@Composable
fun PremiumIcon(
    modifier: Modifier = Modifier
) {
    Icon(
        imageVector = Icons.Default.AutoAwesome,
        contentDescription = "Premium Feature",
        modifier = modifier.size(16.dp),
        tint = Color(0xFFFFD700)
    )
}
