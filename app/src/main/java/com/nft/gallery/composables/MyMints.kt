package com.nft.gallery.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
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

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun MyMintPage(
    myMintsViewModel: MyMintsViewModel = hiltActivityViewModel(),
    navigateToDetails: (Int) -> Unit,
) {
    val uiState = myMintsViewModel.viewState.collectAsState().value

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
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

        when (uiState) {
            is MyMintsViewState.Error -> {
                ErrorView(
                    text = uiState.error.message ?: "An error happened while fetching your Mints",
                    buttonText = "Retry",
                    modifier = Modifier.padding(vertical = 16.dp)
                ) {
                    myMintsViewModel.loadMyMints(forceRefresh = true)
                }
            }
            is MyMintsViewState.Empty -> {
                EmptyView(
                    text = uiState.message,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }
            else -> {
                LazyVerticalGrid(
                    modifier = Modifier.padding(top = 16.dp),
                    columns = GridCells.Adaptive(minSize = 76.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    itemsIndexed(items = uiState.myMints) { index, myMint ->
                        GlideImage(
                            modifier = Modifier
                                .height(76.dp)
                                .width(76.dp)
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
    }
}