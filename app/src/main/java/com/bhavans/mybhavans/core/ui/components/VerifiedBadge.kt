package com.bhavans.mybhavans.core.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * A blue verified badge (checkmark) displayed next to verified users' names.
 */
@Composable
fun VerifiedBadge(
    modifier: Modifier = Modifier,
    size: Dp = 16.dp,
    tint: Color = Color(0xFF1DA1F2)
) {
    Icon(
        imageVector = Icons.Filled.Verified,
        contentDescription = "Verified",
        tint = tint,
        modifier = modifier.size(size)
    )
}
