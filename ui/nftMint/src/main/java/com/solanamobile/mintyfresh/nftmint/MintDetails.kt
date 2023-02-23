package com.solanamobile.mintyfresh.nftmint

import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.compose.animation.ExperimentalAnimationApi
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.*
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.google.accompanist.navigation.animation.composable
import com.solana.mobilewalletadapter.clientlib.ActivityResultSender
import com.solanamobile.mintyfresh.composable.simplecomposables.BackButton
import com.solanamobile.mintyfresh.mintycore.usecase.MintState
import java.io.File

private const val MintDetailsRoute = "mint"

fun NavController.navigateToMintDetailsScreen(imagePath: String, navOptions: NavOptions? = null) {
    this.navigate("$MintDetailsRoute?imagePath=$imagePath", navOptions)
}

@OptIn(ExperimentalAnimationApi::class)
fun NavGraphBuilder.mintDetailsScreen(
    navigateUp: () -> Boolean = { true },
    onMintCompleted: () -> Unit,
    activityResultSender: ActivityResultSender,
    contentResolver: ContentResolver,
    cacheDir: File,
    identityUri: Uri,
    iconUri: Uri,
    appName: String
) {
    composable(
        route = "$MintDetailsRoute?imagePath={imagePath}",
        arguments = listOf(navArgument("imagePath") {
            type = NavType.StringType
        }),
        deepLinks = listOf(navDeepLink {
            uriPattern = "{imagePath}"
            action = Intent.ACTION_SEND
            mimeType = "image/*"
        })
    ) { backStackEntry ->
        val imagePath = backStackEntry.arguments?.getString("imagePath")
        val deepLinkIntent: Intent? =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                backStackEntry.arguments?.getParcelable(
                    NavController.KEY_DEEP_LINK_INTENT,
                    Intent::class.java
                )
            } else {
                @Suppress("DEPRECATION")
                backStackEntry.arguments?.getParcelable(
                    NavController.KEY_DEEP_LINK_INTENT
                )
            }
        val clipDataUri = deepLinkIntent?.clipData?.getItemAt(0)?.uri
        val clipDataPath = clipDataUri?.let {
            val input = contentResolver.openInputStream(clipDataUri)
            val file = File.createTempFile("shared", ".image", cacheDir)

            input?.let {
                file.writeBytes(input.readBytes())
                input.close()
                file.toPath()
            }
        }?.toString()

        MintDetailsPage(
            imagePath = imagePath ?: clipDataPath
            ?: throw IllegalStateException("$MintDetailsRoute requires an \"imagePath\" argument to be launched"),
            navigateUp = navigateUp,
            onMintCompleted = onMintCompleted,
            identityUri = identityUri,
            iconUri = iconUri,
            identityName = appName,
            intentSender = activityResultSender
        )
    }
}


@OptIn(
    ExperimentalMaterial3Api::class, ExperimentalGlideComposeApi::class,
    ExperimentalComposeUiApi::class
)
@Composable
fun MintDetailsPage(
    identityUri: Uri,
    iconUri: Uri,
    identityName: String,
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
                    if (uiState.mintState !is MintState.Error) {
                        CircularProgressIndicator()
                    }
                    Text(
                        modifier = Modifier.padding(
                            top = 28.dp
                        ),
                        textAlign = TextAlign.Center,
                        text = when (uiState.mintState) {
                            is MintState.UploadingMedia -> stringResource(R.string.uploading_file)
                            is MintState.CreatingMetadata -> stringResource(R.string.uploading_metadata)
                            is MintState.BuildingTransaction, is MintState.Signing-> stringResource(R.string.requesting_signatuve)
                            is MintState.Minting -> stringResource(R.string.minting)
                            is MintState.AwaitingConfirmation -> stringResource(R.string.waiting_confirmations)
                            is MintState.Error -> stringResource(id = R.string.generic_error_message, uiState.mintState.message)
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
                        .imePadding()
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
                                identityUri,
                                iconUri,
                                identityName,
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