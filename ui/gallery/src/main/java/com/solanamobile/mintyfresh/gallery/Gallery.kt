package com.solanamobile.mintyfresh.gallery

import android.Manifest
import android.os.Build
import android.provider.MediaStore
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.solanamobile.mintyfresh.composable.simplecomposables.EmptyView
import com.solanamobile.mintyfresh.composable.simplecomposables.PermissionView
import com.solanamobile.mintyfresh.composable.simplecomposables.VideoView
import com.solanamobile.mintyfresh.composable.viewmodel.MediaViewModel

@OptIn(ExperimentalPermissionsApi::class, ExperimentalGlideComposeApi::class)
@Composable
fun Gallery(
    mediaViewModel: MediaViewModel = hiltViewModel(),
    navigateToDetails: (String) -> Unit = { },
) {
    val permissionsRequired = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        listOf(
            Manifest.permission.READ_MEDIA_IMAGES
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
                stringResource(R.string.lets_get),
                spanStyle = SpanStyle(MaterialTheme.colorScheme.onSurfaceVariant)
            ).plus(
                AnnotatedString(
                    stringResource(R.string.minty_fresh),
                    spanStyle = SpanStyle(MaterialTheme.colorScheme.onSurface)
                )
            )
        )
        Text(
            modifier = Modifier.padding(
                top = 30.dp
            ),
            text = stringResource(R.string.select_photo),
            style = MaterialTheme.typography.labelLarge
        )
        PermissionView(
            permissionsRequired,
            content = {
                val uiState = mediaViewModel.getMediaList().collectAsState().value

                LaunchedEffect(
                    key1 = Unit,
                    block = {
                        mediaViewModel.loadAllMediaFiles()
                    }
                )

                LazyVerticalGrid(
                    modifier = Modifier.fillMaxHeight(),
                    columns = GridCells.Adaptive(minSize = 76.dp),
                    contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    itemsIndexed(items = uiState) { _, media ->
                        when (media.mediaType) {
                            MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE -> {
                                GlideImage(
                                    model = media.path,
                                    contentDescription = stringResource(R.string.image_content_desc),
                                    modifier = Modifier
                                        .width(76.dp)
                                        .aspectRatio(1.0f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(color = MaterialTheme.colorScheme.surface)
                                        .clickable {
                                            navigateToDetails(media.path)
                                        },
                                    contentScale = ContentScale.Crop
                                ) {
                                    it.thumbnail()
                                }
                            }
                            MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO -> {
                                VideoView(media)
                            }
                        }
                    }
                }
            },
            emptyView = {
                EmptyView(
                    it, stringResource(id = R.string.gallery_permission_body), stringResource(
                        id = R.string.gallery_permission_button
                    )
                )
            }
        )
    }
}

