package com.nft.gallery.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.unit.dp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.nft.gallery.ktx.hiltActivityViewModel
import com.nft.gallery.viewmodel.MyMintsViewModel
import com.nft.gallery.viewmodel.viewstate.MyMintsViewState

@OptIn(ExperimentalGlideComposeApi::class, ExperimentalMaterialApi::class)
@Composable
fun MyMintPage(
    myMintsViewModel: MyMintsViewModel = hiltActivityViewModel(),
    navigateToDetails: (Int) -> Unit,
) {
    val uiState = myMintsViewModel.viewState.collectAsState().value
    val isRefreshing = myMintsViewModel.isRefreshing.collectAsState().value
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = {
            myMintsViewModel.refresh()
        })

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 16.dp)
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = AnnotatedString(
                "My",
                spanStyle = SpanStyle(MaterialTheme.colorScheme.onSurfaceVariant)
            ).plus(
                AnnotatedString(
                    " mints",
                    spanStyle = SpanStyle(MaterialTheme.colorScheme.onSurface)
                )
            ),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier
                .padding(bottom = 30.dp)
                .align(Alignment.Start)
        )

        Box(
            Modifier
                .fillMaxSize()
                .pullRefresh(pullRefreshState)
        ) {
            when (uiState) {
                is MyMintsViewState.Error -> {
                    Column(
                        Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        ErrorView(
                            text = uiState.error.message
                                ?: "An error happened while fetching your Mints",
                            buttonText = "Retry",
                            modifier = Modifier
                                .padding(vertical = 16.dp)
                        ) {
                            myMintsViewModel.loadMyMints(forceRefresh = true)
                        }
                    }
                }
                is MyMintsViewState.Empty -> {
                    Column(
                        Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        EmptyView(
                            text = uiState.message,
                            modifier = Modifier
                                .padding(vertical = 16.dp)
                        )
                    }
                }
                else -> {
                    LazyVerticalGrid(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 16.dp),
                        columns = GridCells.Adaptive(minSize = 76.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        itemsIndexed(items = uiState.myMints) { index, myMint ->
                            GlideImage(
                                modifier = Modifier
                                    .width(76.dp)
                                    .aspectRatio(1.0f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(color = MaterialTheme.colorScheme.surface)
                                    .loadingPlaceholder(
                                        isLoading = uiState is MyMintsViewState.Loading || myMint.mediaUrl.isEmpty(),
                                        cornerRoundedShapeSize = 8.dp
                                    )
                                    .clickable {
                                        navigateToDetails(index)
                                    },
                                model = myMint.mediaUrl,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                            )
                        }
                    }
                }
            }
            PullRefreshIndicator(
                isRefreshing,
                pullRefreshState,
                Modifier.align(Alignment.TopCenter)
            )
        }
    }
}