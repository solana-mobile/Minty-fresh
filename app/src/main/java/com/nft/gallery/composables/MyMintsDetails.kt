package com.nft.gallery.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.PagerState
import com.nft.gallery.R
import com.nft.gallery.ktx.hiltActivityViewModel
import com.nft.gallery.viewmodel.MyMintsViewModel

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
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    val uiState = myMintsViewModel.viewState.collectAsState().value

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                navigationIcon = {
                    BackButton(navigateUp)
                },
                title = {},
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
            count = uiState.myMints.size,
            state = PagerState(index),
        ) { page ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                val configuration = LocalConfiguration.current
                val imageHeight = configuration.screenHeightDp.dp * 0.4f
                GlideImage(
                    modifier = Modifier
                        .height(imageHeight)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(color = MaterialTheme.colorScheme.background),
                    model = uiState.myMints[page].mediaUrl,
                    contentDescription = uiState.myMints[page].name,
                    contentScale = ContentScale.Fit,
                ) {
                    it.thumbnail()
                }
                Text(
                    modifier = Modifier.padding(top = 24.dp),
                    style = MaterialTheme.typography.titleLarge,
                    text = uiState.myMints[page].name ?: "",
                )
                Text(
                    modifier = Modifier.padding(top = 36.dp),
                    style = MaterialTheme.typography.labelMedium,
                    text = stringResource(id = R.string.description),
                )
                Text(
                    modifier = Modifier.padding(top = 8.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    text = uiState.myMints[page].description ?: "",
                )
                Text(
                    modifier = Modifier.padding(top = 36.dp),
                    style = MaterialTheme.typography.labelMedium,
                    text = stringResource(R.string.metadata),
                )
                Row(
                    modifier = Modifier.padding(top = 8.dp),
                ) {
                    Text(
                        style = MaterialTheme.typography.bodyMedium,
                        text = stringResource(R.string.mint_address),
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        modifier = Modifier.size(96.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        text = uiState.myMints[page].id,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}