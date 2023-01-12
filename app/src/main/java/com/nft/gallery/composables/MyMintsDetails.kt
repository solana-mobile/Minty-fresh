package com.nft.gallery.composables

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.PagerState
import com.nft.gallery.ktx.hiltActivityViewModel
import com.nft.gallery.viewmodel.MyMintsViewModel
import com.solana.core.PublicKey

@OptIn(
    ExperimentalPagerApi::class,
    ExperimentalGlideComposeApi::class,
    ExperimentalMaterial3Api::class,
)
@Composable
fun MyMintsDetails(
    index: Int,
    navigateUp: () -> Boolean = { true },
    myMintsViewModel: MyMintsViewModel = hiltActivityViewModel(),
) {
    val uiState = myMintsViewModel.viewState.collectAsState().value
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    val scrollState = rememberScrollState()

    LaunchedEffect(myMintsViewModel.wasLaunched) {
        if (!myMintsViewModel.wasLaunched) {
            myMintsViewModel.loadMyMints(
                PublicKey("5nmoLTjaCYxDY2iZEAHEnbkTyPRrqtF6mrGwXxuJGr4C") // TODO real public key from MWA
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    BackButton(navigateUp)
                },
                title = {
                    Text(
                        text = "",
                    )
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.background,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurface
                ),
                scrollBehavior = scrollBehavior
            )
        }
    ) { innerPadding ->
        HorizontalPager(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            count = uiState.size,
            state = PagerState(index),
        ) { page ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                GlideImage(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp)),
                    model = uiState[page].mediaUrl,
                    contentDescription = "Detail of My Mint",
                    contentScale = ContentScale.Fit,
                )
                Text(
                    modifier = Modifier.padding(top = 24.dp),
                    style = MaterialTheme.typography.titleLarge,
                    text = uiState[page].name ?: "",
                )
                Text(
                    modifier = Modifier.padding(top = 36.dp),
                    style = MaterialTheme.typography.labelMedium,
                    text = "Description",
                )
                Text(
                    modifier = Modifier.padding(top = 8.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    text = uiState[page].description ?: "",
                )
                Text(
                    modifier = Modifier.padding(top = 36.dp),
                    style = MaterialTheme.typography.labelMedium,
                    text = "Metadata",
                )
                Row(
                    modifier = Modifier.padding(top = 8.dp),
                ) {
                    Text(
                        style = MaterialTheme.typography.bodyMedium,
                        text = "Mint address",
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        modifier = Modifier.size(96.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        text = uiState[page].id,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}