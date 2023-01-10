package com.nft.gallery.activity

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
                TopAppBar(
                    backgroundColor = MaterialTheme.colorScheme.background
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .padding(8.dp)
                                .size(48.dp)
                                .clickable {
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
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxWidth()
                ) {
                    val title = rememberSaveable { mutableStateOf("") }
                    val description = rememberSaveable { mutableStateOf("") }
                    GlideImage(
                        model = imagePath,
                        contentDescription = null,
                        modifier = Modifier
                            .padding(top = 16.dp)
                            .width(76.dp)
                            .height(76.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Text(
                        text = title.value.ifEmpty { "Your NFT" },
                        fontSize = 20.sp,
                        lineHeight = 30.sp,
                        modifier = Modifier.padding(top = 24.dp)
                    )
                    Text(
                        text = description.value.ifEmpty { "No description yet." },
                        fontSize = 12.sp,
                        lineHeight = 20.sp,
                        modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
                    )
                    Text(
                        text = "Add Details",
                        fontSize = 18.sp,
                        lineHeight = 22.sp,
                        modifier = Modifier.padding(bottom = 24.dp),
                        fontWeight = FontWeight.Bold
                    )
                    OutlinedTextField(
                        value = title.value,
                        onValueChange = {
                            title.value = it.trim().take(32)
                        },
                        label = {
                            Text(text = "Give your NFT a title")
                        },
                        placeholder = {
                            Text(text = "Enter a title")
                        },
                        maxLines = 1,
                    )
                    OutlinedTextField(
                        modifier = Modifier.padding(vertical = 24.dp),
                        value = description.value,
                        onValueChange = {
                            description.value = it.trim().take(128)
                        },
                        label = {
                            Text(text = "Add a description")
                        },
                        placeholder = {
                            Text(text = "Describe your NFT here")
                        },
                        maxLines = 3,
                    )
                    Button(
                        enabled = title.value.isNotEmpty() && description.value.isNotEmpty(),
                        onClick = {
                            // TODO Mint the NFT here
                        }
                    ) {
                        Text(text = "Mint")
                    }
                }
            },
            containerColor = MaterialTheme.colorScheme.background
        )
    }

}