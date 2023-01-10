package com.nft.gallery.activity

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.nft.gallery.AppTheme

@OptIn(ExperimentalMaterial3Api::class, ExperimentalGlideComposeApi::class)
@Composable
fun MintDetailsPage(
    imagePath: String,
    navigateUp: () -> Boolean = { true },
) {
    AppTheme {
        Scaffold(
            topBar = {
                TopAppBar (
                    backgroundColor = MaterialTheme.colorScheme.background
                ) {
                    Row {
                        Button(
                            onClick = {
                                navigateUp()
                            }
                        ) {
                            Icon(
                                tint = MaterialTheme.colorScheme.onSurface,
                                imageVector = Icons.Filled.ArrowBack,
                                contentDescription = "back"
                            )
                        }
                    }

                }
            },
            content = { padding ->
                Box(modifier = Modifier.padding(padding)) {
                    GlideImage(
                        model = imagePath,
                        contentDescription = null,
                        modifier = Modifier
                            .width(76.dp)
                            .height(76.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
            },
            containerColor = MaterialTheme.colorScheme.background
        )
    }

}