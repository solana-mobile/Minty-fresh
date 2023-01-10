package com.nft.gallery.activity

import android.Manifest
import android.content.ContentValues
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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.nft.gallery.AppTheme
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
            AppTheme {
                Scaffold(
                    bottomBar = { BottomNavigationBar(navController) },
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
                Camera()
            }
            composable(NavigationItem.Photos.route) {
                Gallery()
            }
            composable(NavigationItem.Videos.route) {
                Videos()
            }
        }
    }

    @OptIn(ExperimentalPermissionsApi::class)
    @Composable
    fun Camera() {
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
                StartCamera()
            },
            emptyView = {
                EmptyView(it)
            }
        )
    }

    @Composable
    private fun StartCamera() {
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
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 48.dp),
                onClick = { takePhoto() }
            ) {
                Icon(
                    modifier = Modifier.size(24.dp),
                    painter = painterResource(id = NavigationItem.Camera.icon),
                    contentDescription = "Take Picture"
                )
            }
        }
    }

    private fun takePhoto() {
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
                }
            }
        )
    }

    @OptIn(ExperimentalPermissionsApi::class, ExperimentalGlideComposeApi::class)
    @Composable
    fun Gallery(
        imageViewModel: ImageViewModel = hiltViewModel()
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
                    columns = GridCells.Adaptive(minSize = 128.dp)
                ) {
                    itemsIndexed(items = uiState) { _, path ->
                        GlideImage(
                            model = path,
                            contentDescription = null,
                            modifier = Modifier
                                .width(128.dp)
                                .height(128.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            },
            emptyView = {
                EmptyView(it)
            }
        )
    }

    @OptIn(ExperimentalPermissionsApi::class, ExperimentalGlideComposeApi::class)
    @Composable
    fun Videos(
        imageViewModel: ImageViewModel = hiltViewModel()
    ) {
        val permissionsRequired = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            listOf(
                Manifest.permission.READ_MEDIA_VIDEO,
            )
        } else {
            listOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
            )
        }

        PermissionView(
            permissionsRequired,
            content = {
                val uiState = imageViewModel.getVideoList().collectAsState().value

                LaunchedEffect(
                    key1 = Unit,
                    block = {
                        imageViewModel.loadAllVideos()
                    }
                )
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 128.dp)
                ) {
                    itemsIndexed(items = uiState) { _, path ->
                        GlideImage(
                            model = path,
                            contentDescription = null,
                            modifier = Modifier
                                .width(128.dp)
                                .height(128.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            },
            emptyView = {
                EmptyView(it)
            }
        )
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
        Column(modifier = Modifier.fillMaxSize()) {
            val textToShow = if (permissionState.shouldShowRationale) {
                "The camera is important for this app. Please grant the permission."
            } else {
                "Camera permission required for this feature to be available. " +
                        "Please grant the permission"
            }
            Text(textToShow)

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
            NavigationItem.Camera,
            NavigationItem.Photos,
        )

        BottomNavigation(
            backgroundColor = MaterialTheme.colorScheme.surface,
        ) {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route

            items.forEach { item ->
                BottomNavigationItem(
                    icon = {
                        Icon(
                            modifier = Modifier.size(24.dp),
                            painter = painterResource(id = item.icon),
                            contentDescription = item.title
                        )
                    },
                    label = {
                        Text(text = item.title)
                    },
                    selectedContentColor = MaterialTheme.colorScheme.onSurface,
                    unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    alwaysShowLabel = true,
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
    }

    companion object {
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
    }
}