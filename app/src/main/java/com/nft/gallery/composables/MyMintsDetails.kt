package com.nft.gallery.composables

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.PagerState
import com.nft.gallery.ktx.hiltActivityViewModel
import com.nft.gallery.viewmodel.MyMintsViewModel
import com.solana.core.PublicKey

@OptIn(ExperimentalPagerApi::class, ExperimentalGlideComposeApi::class)
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
        GlideImage(
            modifier = Modifier
                .fillMaxSize(),
            model = uiState[page].mediaUrl,
            contentDescription = "Detail of My Mint",
            contentScale = ContentScale.Fit,
        )
    }
}