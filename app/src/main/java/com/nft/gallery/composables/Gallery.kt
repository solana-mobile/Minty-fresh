package com.nft.gallery.composables

import android.Manifest
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.nft.gallery.viewmodel.ImageViewModel

@OptIn(ExperimentalPermissionsApi::class, ExperimentalGlideComposeApi::class)
@Composable
fun Gallery(
    imageViewModel: ImageViewModel = hiltViewModel(),
    navigateToDetails: (String) -> Unit = { },
) {
    val permissionsRequired = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        listOf(
            Manifest.permission.READ_MEDIA_IMAGES,
        )
    } else {
        listOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
        )
    }

    Column(
        modifier = Modifier
            .background(
                color = MaterialTheme.colorScheme.background
            )
            .padding(top = 16.dp)
            .padding(horizontal = 16.dp)
            .fillMaxHeight()
    ) {
        Text(
            style = MaterialTheme.typography.headlineMedium,
            text = AnnotatedString(
                "Let\u2019s get",
                spanStyle = SpanStyle(MaterialTheme.colorScheme.onSurfaceVariant)
            ).plus(
                AnnotatedString(
                    " minty fresh.",
                    spanStyle = SpanStyle(MaterialTheme.colorScheme.onSurface)
                )
            )
        )
        Text(
            modifier = Modifier.padding(
                top = 30.dp
            ),
            text = "Select a photo to mint:",
            style = MaterialTheme.typography.labelLarge
        )
        PermissionView(
            permissionsRequired,
            content = {
                val uiState = imageViewModel.getImageList().collectAsState().value

                LaunchedEffect(
                    key1 = Unit,
                    block = {
                        imageViewModel.loadAllImages()
                    }
                )

                LazyVerticalGrid(
                    modifier = Modifier
                        .padding(
                            top = 16.dp
                        )
                        .fillMaxHeight(),
                    columns = GridCells.Adaptive(minSize = 76.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    itemsIndexed(items = uiState) { _, path ->
                        GlideImage(
                            model = path,
                            contentDescription = null,
                            modifier = Modifier
                                .width(76.dp)
                                .aspectRatio(1.0f)
                                .background(color = MaterialTheme.colorScheme.surface)
                                .clickable {
                                    navigateToDetails(path)
                                },
                            contentScale = ContentScale.Crop
                        ) {
                            it.thumbnail()
                        }
                    }
                }
            },
            emptyView = {
                EmptyView(it)
            }
        )
    }
}

