package com.nft.gallery.activity

import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Surface
import androidx.compose.material.contentColorFor
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.nft.gallery.theme.AppTheme
import com.nft.gallery.theme.NavigationItem
import com.nft.gallery.viewmodel.ImageViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*


@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private var imageCapture: ImageCapture? = null

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route
            AppTheme {
                Scaffold(
                    floatingActionButton = {
                        if (currentRoute == NavigationItem.Photos.route) {
                            FloatingActionButton(
                                shape = RoundedCornerShape(corner = CornerSize(16.dp)),
                                backgroundColor = MaterialTheme.colorScheme.onBackground,
                                onClick = {
                                    navController.navigate(NavigationItem.Camera.route)
                                }
                            ) {
                                Icon(
                                    modifier = Modifier.size(24.dp),
                                    imageVector = NavigationItem.Camera.icon,
                                    contentDescription = "Take Picture",
                                    tint = MaterialTheme.colorScheme.background
                                )
                            }
                        }
                    },
                    bottomBar = {
                        if (currentRoute == NavigationItem.Photos.route || currentRoute == NavigationItem.MyMints.route) {
                            BottomNavigationBar(navController = navController)
                        }
                    },
                    content = { padding ->
                        Box(modifier = Modifier.padding(padding)) {
                            Navigation(navController = navController)
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.background
                )
            }
        }
    }

    @Composable
    fun Navigation(navController: NavHostController) {
        NavHost(navController, startDestination = NavigationItem.Photos.route) {
            composable(NavigationItem.Camera.route) {
                Camera(
                    navigateToDetails = {
                        navController.navigate("${NavigationItem.MintDetail.route}?imagePath=$it")
                    }
                )
            }
            composable(NavigationItem.Photos.route) {
                Gallery(
                    navigateToDetails = {
                        navController.navigate("${NavigationItem.MintDetail.route}?imagePath=$it")
                    }
                )
            }
            composable(
                route = "${NavigationItem.MintDetail.route}?imagePath={imagePath}",
                arguments = listOf(navArgument("imagePath") { type = NavType.StringType }),
                deepLinks = listOf(navDeepLink {
                    uriPattern = "{imagePath}"
                    action = Intent.ACTION_SEND
                    mimeType = "image/*"
                })
            ) { backStackEntry ->
                val imagePath = backStackEntry.arguments?.getString("imagePath")
                val deepLinkIntent: Intent? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    backStackEntry.arguments?.getParcelable(
                        NavController.KEY_DEEP_LINK_INTENT,
                        Intent::class.java
                    )
                } else {
                    backStackEntry.arguments?.getParcelable(
                        NavController.KEY_DEEP_LINK_INTENT
                    )
                }
                val clipDataUri = deepLinkIntent?.clipData?.getItemAt(0)?.uri?.toString()

                MintDetailsPage(
                    imagePath = imagePath ?: clipDataUri
                    ?: throw IllegalStateException("${NavigationItem.MintDetail.route} requires an \"imagePath\" argument to be launched"),
                    navigateUp = {
                        navController.navigateUp()
                    }
                )
            }
            composable(NavigationItem.MyMints.route) {
                MyMintPage()
            }
        }
    }

    @OptIn(ExperimentalPermissionsApi::class)
    @Composable
    fun Camera(
        navigateToDetails: (String) -> Unit = { },
    ) {
        val permissionsRequired =
            mutableListOf(
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO
            ).apply {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }

        PermissionView(
            permissionsRequired,
            content = {
                StartCamera(navigateToDetails)
            },
            emptyView = {
                Box(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxSize(),
                ) {
                    EmptyView(it)
                }
            }
        )
    }

    @Composable
    private fun StartCamera(
        navigateToDetails: (String) -> Unit = { },
    ) {
        val coroutineScope = rememberCoroutineScope()
        val lifecycleOwner = LocalLifecycleOwner.current
        Box {
            AndroidView(
                factory = { context ->
                    val previewView = PreviewView(context).apply {
                        this.scaleType = PreviewView.ScaleType.FILL_CENTER
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                    }

                    // CameraX Preview UseCase
                    val previewUseCase = Preview.Builder()
                        .build()
                        .also {
                            it.setSurfaceProvider(previewView.surfaceProvider)
                        }

                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                    imageCapture = ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .build()


                    val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
                    coroutineScope.launch {
                        val cameraProvider: ProcessCameraProvider =
                            withContext(Dispatchers.IO) {
                                cameraProviderFuture.get()
                            }
                        try {
                            // Must unbind the use-cases before rebinding them.
                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner, cameraSelector, previewUseCase, imageCapture
                            )
                        } catch (ex: Exception) {
                            Log.e("CameraPreview", "Use case binding failed", ex)
                        }
                    }

                    previewView
                }
            )
            Button(
                shape = RoundedCornerShape(corner = CornerSize(16.dp)),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onBackground),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 48.dp),
                onClick = { takePhoto(navigateToDetails) }
            ) {
                Icon(
                    modifier = Modifier.size(24.dp),
                    imageVector = NavigationItem.Camera.icon,
                    contentDescription = "Take Picture"
                )
            }
        }
    }

    private fun takePhoto(
        navigateToDetails: (String) -> Unit = { },
    ) {
        // Get a stable reference of the modifiable image capture use case
        val imageCapture = imageCapture ?: return

        // Create time stamped name and MediaStore entry.
        val name = SimpleDateFormat(FILENAME_FORMAT, Locale.US)
            .format(System.currentTimeMillis())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CameraX-Image")
            }
        }

        // Create output options object which contains file + metadata
        val outputOptions = ImageCapture.OutputFileOptions
            .Builder(
                contentResolver,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues
            )
            .build()

        // Set up image capture listener, which is triggered after photo has
        // been taken
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e("CameraTakePicture", "Photo capture failed: ${exc.message}", exc)
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    output.savedUri?.let { navigateToDetails(it.toString()) }
                }
            }
        )
    }

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

        Column(modifier = Modifier
            .padding(top = 16.dp)
            .padding(horizontal = 16.dp)) {
            Text(
                text = AnnotatedString(
                    "Let\u2019s get",
                    spanStyle = SpanStyle(MaterialTheme.colorScheme.onSurfaceVariant)
                ).plus(
                    AnnotatedString(
                        " minty fresh.",
                        spanStyle = SpanStyle(MaterialTheme.colorScheme.onSurface)
                    )
                ),
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 30.dp)
            )
            Text(
                text = "Select a photo to mint:",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
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
                        modifier = Modifier.padding(top = 16.dp),
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
                                    .height(76.dp)
                                    .clip(RoundedCornerShape(4.dp))
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

    @OptIn(ExperimentalPermissionsApi::class)
    @Composable
    fun PermissionView(
        permissions: List<String>,
        content: @Composable () -> Unit,
        emptyView: @Composable (permissionState: MultiplePermissionsState) -> Unit
    ) {
        val permissionState = rememberMultiplePermissionsState(permissions)

        if (permissionState.allPermissionsGranted) {
            content()
        } else {
            emptyView(permissionState)
        }
    }

    @OptIn(ExperimentalPermissionsApi::class)
    @Composable
    fun EmptyView(permissionState: MultiplePermissionsState) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val textToShow = if (permissionState.shouldShowRationale) {
                "The camera is important for this app. Please grant the permission."
            } else {
                "Camera permission required for this feature to be available. " +
                        "Please grant the permission"
            }
            Text(textToShow, modifier = Modifier.padding(vertical = 16.dp))

            Button(
                onClick = {
                    permissionState.launchMultiplePermissionRequest()
                },
                content = {
                    Text("Grant")
                }
            )
        }
    }


    @Composable
    fun BottomNavigationBar(navController: NavHostController) {
        val items = listOf(
            NavigationItem.Photos,
            NavigationItem.MyMints,
        )

        Surface(
            color = MaterialTheme.colorScheme.surface,
            contentColor = contentColorFor(MaterialTheme.colorScheme.surface),
            elevation = 8.dp,
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .selectableGroup(),
                horizontalArrangement = Arrangement.SpaceBetween,
                content = {
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentRoute = navBackStackEntry?.destination?.route

                    items.forEach { item ->
                        BottomNavigationItem(
                            icon = {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .then(
                                            if (currentRoute == item.route) {
                                                Modifier.background(
                                                    color = MaterialTheme.colorScheme.surfaceVariant
                                                )
                                            } else {
                                                Modifier
                                            }
                                        )
                                        .then(
                                            Modifier
                                                .padding(horizontal = 12.dp)
                                        )
                                ) {
                                    Icon(
                                        modifier = Modifier
                                            .padding(top = 5.dp, bottom = 3.dp)
                                            .size(24.dp),
                                        imageVector = item.icon,
                                        contentDescription = item.title
                                    )
                                    Text(
                                        text = item.title,
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.padding(bottom = 10.dp)
                                    )
                                }
                            },
                            selectedContentColor = MaterialTheme.colorScheme.onSurface,
                            unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            selected = currentRoute == item.route,
                            onClick = {
                                navController.navigate(item.route) {
                                    // Pop up to the start destination of the graph to
                                    // avoid building up a large stack of destinations
                                    // on the back stack as users select items
                                    navController.graph.startDestinationRoute?.let { route ->
                                        popUpTo(route) {
                                            saveState = true
                                        }
                                    }
                                    // Avoid multiple copies of the same destination when
                                    // re-selecting the same item
                                    launchSingleTop = true
                                    // Restore state when re-selecting a previously selected item
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            )
        }
    }

    companion object {
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
    }
}