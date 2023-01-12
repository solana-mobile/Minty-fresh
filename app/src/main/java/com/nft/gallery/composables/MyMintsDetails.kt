package com.nft.gallery.composables

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.PagerState
import com.nft.gallery.ktx.hiltActivityViewModel
import com.nft.gallery.viewmodel.MyMintsViewModel
import com.solana.core.PublicKey

@OptIn(ExperimentalPagerApi::class)
@Composable
fun MyMintsDetails(
    index: Int,
    myMintsViewModel: MyMintsViewModel = hiltActivityViewModel(),
) {
    val uiState = myMintsViewModel.viewState.collectAsState().value

    LaunchedEffect(myMintsViewModel.wasLaunched) {
        if (!myMintsViewModel.wasLaunched) {
            myMintsViewModel.loadMyMints(
                PublicKey("5nmoLTjaCYxDY2iZEAHEnbkTyPRrqtF6mrGwXxuJGr4C") // TODO real public key from MWA
            )
        }
    }

    HorizontalPager(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxSize(),
        count = uiState.size,
        state = PagerState(index),
    ) { page ->
        AsyncImage(
            modifier = Modifier
                .fillMaxSize(),
            model = ImageRequest.Builder(LocalContext.current)
                .data(uiState[page].mediaUrl)
                .crossfade(true)
                .build(),
            placeholder = ColorPainter(MaterialTheme.colorScheme.surfaceVariant),
            contentDescription = "Detail of My Mint",
            contentScale = ContentScale.Fit,
        )
    }
}