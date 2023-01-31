package com.solanamobile.mintyfresh.mymints.composables

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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.unit.dp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.solanamobile.mintyfresh.composable.simplecomposables.EmptyView
import com.solanamobile.mintyfresh.composable.simplecomposables.ErrorView
import com.solanamobile.mintyfresh.composable.simplecomposables.loadingPlaceholder
import com.solanamobile.mintyfresh.mymints.ktx.hiltActivityViewModel
import com.solanamobile.mintyfresh.mymints.viewmodels.MyMintsViewModel
import com.solanamobile.mintyfresh.mymints.viewmodels.viewstate.MyMintsViewState
import com.solanamobile.mintyfresh.mymints.R

@OptIn(ExperimentalGlideComposeApi::class, ExperimentalMaterialApi::class)
@Composable
fun MyMintPage(
    forceRefresh: Boolean,
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

    LaunchedEffect(
        key1 = Unit,
        block = {
            if (forceRefresh) {
                myMintsViewModel.refresh()
            }
        }
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 16.dp)
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = AnnotatedString(
                stringResource(R.string.my),
                spanStyle = SpanStyle(MaterialTheme.colorScheme.onSurfaceVariant)
            ).plus(
                AnnotatedString(
                    stringResource(R.string.mints),
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
                                ?: stringResource(R.string.error_fetching_mints),
                            buttonText = stringResource(R.string.retry),
                            modifier = Modifier
                                .padding(vertical = 16.dp)
                        ) {
                            myMintsViewModel.refresh()
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
                            text = stringResource(R.string.no_mints_yet),
                            modifier = Modifier
                                .padding(vertical = 16.dp)
                        )
                    }
                }
                is MyMintsViewState.NoConnection -> {
                    EmptyView(
                        text = stringResource(R.string.connect_to_see_mints),
                        modifier = Modifier
                            .padding(vertical = 16.dp)
                    )
                }
                else -> {
                    LazyVerticalGrid(
                        modifier = Modifier.fillMaxSize(),
                        columns = GridCells.Adaptive(minSize = 76.dp),
                        contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp),
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
                                        isLoading = myMint.mediaUrl.isEmpty(),
                                        cornerRoundedShapeSize = 8.dp
                                    )
                                    .clickable {
                                        if (myMint.id.isNotEmpty()) {
                                            navigateToDetails(index)
                                        }
                                    },
                                model = myMint.mediaUrl,
                                contentDescription = myMint.name,
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