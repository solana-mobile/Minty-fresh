package com.nft.gallery.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import com.nft.gallery.viewmodel.MintState
import com.nft.gallery.viewmodel.PerformMintViewModel
import com.solana.mobilewalletadapter.clientlib.ActivityResultSender

@OptIn(
    ExperimentalMaterial3Api::class, ExperimentalGlideComposeApi::class,
    ExperimentalComposeUiApi::class
)
@Composable
fun MintDetailsPage(
    imagePath: String,
    navigateUp: () -> Boolean = { true },
    onMintCompleted: () -> Unit = { },
    performMintViewModel: PerformMintViewModel = hiltViewModel(),
    intentSender: ActivityResultSender
) {
    val uiState = performMintViewModel.viewState.collectAsState().value

    if (uiState.mintState == MintState.COMPLETE)
        navigateUp()

    Scaffold(
        topBar = {
            TopAppBar(
                backgroundColor = MaterialTheme.colorScheme.background,
                elevation = 0.dp
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
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
                    Text(
                        text = "Add NFT details",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        },
        content = { padding ->
            if (uiState.mintState != MintState.NONE) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.background)
                        .fillMaxWidth()
                        .fillMaxHeight()
                ) {
                    CircularProgressIndicator()
                    Text(
                        modifier = Modifier.padding(
                            top = 28.dp
                        ),
                        text = when (uiState.mintState) {
                            MintState.UPLOADING_FILE -> "Uploading file..."
                            MintState.CREATING_METADATA -> "Processing..."
                            MintState.MINTING -> "Minting..."
                            MintState.SIGNING -> "Requesting wallet signature..."
                            else -> ""
                        },
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.background)
                        .padding(padding)
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 16.dp)
                        .verticalScroll(rememberScrollState())
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
                            .width(210.dp)
                            .height(210.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                color = MaterialTheme.colorScheme.surface
                            ),
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
                        modifier = Modifier.padding(
                            top = 10.dp
                        )
                    )
                    Spacer(
                        modifier = Modifier.weight(1.0f)
                    )
                    OutlinedTextField(
                        modifier = Modifier
                            .focusRequester(focusRequester)
                            .fillMaxWidth()
                            .padding(
                                top = 30.dp
                            ),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            unfocusedLabelColor = MaterialTheme.colorScheme.outline,
                            placeholderColor = MaterialTheme.colorScheme.outline
                        ),
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
                        )
                    )
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp),
                        text = "Use up to 32 characters",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                    OutlinedTextField(
                        modifier = Modifier
                            .padding(top = 24.dp)
                            .focusRequester(focusRequester)
                            .fillMaxWidth(),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            unfocusedLabelColor = MaterialTheme.colorScheme.outline,
                            placeholderColor = MaterialTheme.colorScheme.outline
                        ),
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
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, bottom = 24.dp),
                        text = "Use up to 256 characters",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Button(
                        modifier = Modifier
                            .padding(
                                top = 32.dp,
                                bottom = 24.dp
                            ),
                        shape = RoundedCornerShape(corner = CornerSize(16.dp)),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onBackground),
                        //TODO: Uncomment this you slacker
//                        enabled = title.value.isNotEmpty() && description.value.isNotEmpty() && uiState.mintState == MintState.NONE,
                        enabled = true,
                        onClick = {
                            //TODO: Uncomment this you slacker
//                            performMintViewModel.performMint(
//                                intentSender,
//                                title.value,
//                                description.value,
//                                imagePath
//                            )

                            onMintCompleted()
                        }
                    ) {
                        Text(text = if (uiState.isWalletConnected) "Mint" else "Connect and Mint")
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    )
}