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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.nft.gallery.R
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
                        text = stringResource(R.string.add_details),
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
                            is MintState.UploadingMedia -> stringResource(R.string.uploading_file)
                            is MintState.CreatingMetadata -> stringResource(R.string.uploading_metadata)
                            is MintState.BuildingTransaction, is MintState.Signing-> stringResource(R.string.requesting_signatuve)
                            is MintState.Minting -> stringResource(R.string.minting)
                            is MintState.AwaitingConfirmation -> stringResource(R.string.waiting_confirmations)
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
                        contentDescription = stringResource(id = R.string.image_content_desc),
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
                        text = title.value.ifEmpty { stringResource(R.string.your_nft) },
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                    Text(
                        text = description.value.ifEmpty { stringResource(R.string.no_description_yet) },
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
                            Text(text = stringResource(R.string.nft_title))
                        },
                        placeholder = {
                            Text(text = stringResource(R.string.enter_a_title))
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
                        text = stringResource(R.string.up_to_32_chars),
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
                            Text(text = stringResource(R.string.description))
                        },
                        placeholder = {
                            Text(text = stringResource(R.string.describe_nft_here))
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
                        text = stringResource(R.string.enter_description),
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
                        Text(text = if (uiState.isWalletConnected) stringResource(R.string.mint) else stringResource(R.string.connect_and_mint))
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    )
}