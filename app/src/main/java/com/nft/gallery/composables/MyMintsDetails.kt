package com.nft.gallery.composables

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.ImageRequest

@Composable
fun MyMintsDetails(imagePath: String) {
    AsyncImage(
        modifier = Modifier
            .fillMaxSize(),
        model = ImageRequest.Builder(LocalContext.current)
            .data(imagePath)
            .crossfade(true)
            .build(),
        placeholder = ColorPainter(MaterialTheme.colorScheme.surfaceVariant),
        contentDescription = "Detail of My Mint",
        contentScale = ContentScale.Fit,
    )
}