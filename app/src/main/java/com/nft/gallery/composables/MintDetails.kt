package com.nft.gallery.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.nft.gallery.usecase.MintState
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
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

    if (uiState.mintState is MintState.Complete){
        onMintCompleted()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    BackButton(navigateUp)
                },
                title = {
                    Text(
                        text = "Add NFT details",
                    )
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.background,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurface
                ),
                scrollBehavior = scrollBehavior
            )
        },
        content = { padding ->
            if (uiState.mintState !is MintState.None) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.background)
                        .padding(padding)
                        .fillMaxWidth()
                        .fillMaxHeight()
                ) {
                    CircularProgressIndicator()
                    Text(
                        modifier = Modifier.padding(
                            top = 28.dp
                        ),
                        text = when (uiState.mintState) {
                            is MintState.UploadingMedia -> "Uploading file..."
                            is MintState.CreatingMetadata -> "Processing..."
                            is MintState.Minting -> "Minting..."
                            is MintState.Signing-> "Requesting wallet signature..."
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
                    val keyboardController = LocalSoftwareKeyboardController.current
                    val focusManager = LocalFocusManager.current

                    GlideImage(
                        model = imagePath,
                        contentDescription = "Image",
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
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Next,
                            capitalization = KeyboardCapitalization.Sentences
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = {
                                focusManager.moveFocus(FocusDirection.Down)
                            }
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
                            capitalization = KeyboardCapitalization.Sentences
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
                        enabled = title.value.isNotEmpty() && description.value.isNotEmpty(),
                        onClick = {
                            performMintViewModel.performMint(
                                intentSender,
                                title.value,
                                description.value,
                                imagePath
                            )
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