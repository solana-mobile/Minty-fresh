package com.solanamobile.mintyfresh.composable.simplecomposables

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.unit.Dp
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.placeholder
import com.google.accompanist.placeholder.shimmer
import com.solanamobile.mintyfresh.composable.theme.shimmerBase
import com.solanamobile.mintyfresh.composable.theme.shimmerOverlay

/**
 * Unified method for placeholder/loading animation
 */
fun Modifier.loadingPlaceholder(
    isLoading: Boolean = false,
    cornerRoundedShapeSize: Dp,
): Modifier = composed {
    val base = shimmerBase()
    val overlay = shimmerOverlay()

    this.placeholder(
        visible = isLoading,
        color = base,
        shape = RoundedCornerShape(cornerRoundedShapeSize),
        highlight = PlaceholderHighlight.shimmer(
            highlightColor = overlay
        )
    )
}