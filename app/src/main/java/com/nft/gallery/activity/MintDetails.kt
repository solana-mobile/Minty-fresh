package com.nft.gallery.activity

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.nft.gallery.viewmodel.PerformMintViewModel

@OptIn(
    ExperimentalMaterial3Api::class, ExperimentalGlideComposeApi::class,
    ExperimentalComposeUiApi::class
)
@Composable
fun MintDetailsPage(
    imagePath: String,
    navigateUp: () -> Boolean = { true },
    performMintViewModel: PerformMintViewModel = hiltViewModel()
) {
    val uiState = performMintViewModel.viewState.collectAsState().value

    Scaffold(
        topBar = {
            TopAppBar(
                backgroundColor = MaterialTheme.colorScheme.background
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
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
                    Text(text = "Add NFT details")
                }

            }
        },
        content = { padding ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(padding)
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 16.dp)
                    .fillMaxWidth()
            ) {
                val title = rememberSaveable { mutableStateOf("") }
                val description = rememberSaveable { mutableStateOf("") }
                val (focusRequester) = FocusRequester.createRefs()
                val keyboardController = LocalSoftwareKeyboardController.current

                GlideImage(
                    model = imagePath,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .width(110.dp)
                        .height(110.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(color = MaterialTheme.colorScheme.surface),
                    contentScale = ContentScale.Crop
                ) {
                    it.thumbnail()
                }
                Text(
                    text = title.value.ifEmpty { "Your NFT" },
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(top = 16.dp)
                )
                Text(
                    text = description.value.ifEmpty { "No description yet." },
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)
                )
                Spacer(modifier = Modifier.weight(1.0f))
                OutlinedTextField(
                    value = title.value,
                    onValueChange = {
                        title.value = it.trimStart().take(32)
                    },
                    label = {
                        Text(text = "NFT title")
                    },
                    placeholder = {
                        Text(text = "Enter a title")
                    },
                    maxLines = 1,
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Next,
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusRequester.requestFocus() }
                    ),
                    modifier = Modifier.focusRequester(focusRequester).fillMaxWidth()
                )
                Text(
                    modifier = Modifier.fillMaxWidth().padding(start = 16.dp),
                    text = "Use up to 32 characters",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    modifier = Modifier
                        .padding(top = 24.dp)
                        .focusRequester(focusRequester)
                        .fillMaxWidth(),
                    value = description.value,
                    onValueChange = {
                        description.value = it.trimStart().take(256)
                    },
                    label = {
                        Text(text = "Description")
                    },
                    placeholder = {
                        Text(text = "Describe your NFT here")
                    },
                    minLines = 3,
                    maxLines = 3,
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done,
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { keyboardController?.hide() }
                    )
                )
                Text(
                    modifier = Modifier.fillMaxWidth().padding(start = 16.dp, bottom = 24.dp),
                    text = "Use up to 256 characters",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.weight(2f))
                Button(
                    shape = RoundedCornerShape(corner = CornerSize(16.dp)),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onBackground),
                    modifier = Modifier.padding(bottom = 32.dp),
                    enabled = title.value.isNotEmpty() && description.value.isNotEmpty(),
                    onClick = {
                        performMintViewModel.performMint(title.value, description.value, imagePath)
                    }
                ) {
                    Text(text = if (uiState.isWalletConnected) "Mint" else "Connect and Mint")
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    )
}