package com.solanamobile.mintyfresh.gallery

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddAPhoto
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.solanamobile.mintyfresh.composable.simplecomposables.EmptyView
import com.solanamobile.mintyfresh.composable.simplecomposables.PermissionView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

private var imageCapture: ImageCapture? = null
private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"

fun NavController.navigateToCamera(navOptions: NavOptions? = null) {
    this.navigate("camera", navOptions)
}

@OptIn(ExperimentalAnimationApi::class)
fun NavGraphBuilder.cameraScreen(
    navigateToDetails: (String) -> Unit = { },
) {
    composable(route = "camera") {
        Camera(
            navigateToDetails = navigateToDetails
        )
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun Camera(
    navigateToDetails: (String) -> Unit = { },
) {
    val permissionsRequired =
        mutableListOf(
            Manifest.permission.CAMERA
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
            LaunchedEffect(key1 = Unit) {
                if (!it.allPermissionsGranted) {
                    it.launchMultiplePermissionRequest()
                }
            }
            Box(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxSize(),
            ) {
                EmptyView(
                    it,
                    stringResource(id = R.string.camera_permission_body),
                    stringResource(id = R.string.camera_permission_button)
                )
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
    val context = LocalContext.current
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
            onClick = { takePhoto(context, navigateToDetails) }
        ) {
            Icon(
                modifier = Modifier.size(24.dp),
                imageVector = Icons.Outlined.AddAPhoto,
                contentDescription = stringResource(id = R.string.take_pic_content_desc)
            )
        }
    }
}

private fun takePhoto(
    context: Context,
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
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/MintyFresh")
        }
    }

    // Create output options object which contains file + metadata
    val outputOptions = ImageCapture.OutputFileOptions
        .Builder(
            context.contentResolver,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        )
        .build()

    // Set up image capture listener, which is triggered after photo has
    // been taken
    imageCapture.takePicture(
        outputOptions,
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback {
            override fun onError(exc: ImageCaptureException) {
                Log.e("CameraTakePicture", "Photo capture failed: ${exc.message}", exc)
            }

            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                output.savedUri?.let {
                    val path = getRealPathFromURI(it)
                    navigateToDetails(path)
                }
            }

            private fun getRealPathFromURI(contentURI: Uri): String {
                val result: String
                val cursor = context.contentResolver.query(contentURI, null, null, null, null)
                if (cursor == null) {
                    result = contentURI.toString()
                } else {
                    cursor.moveToFirst()
                    val idx: Int = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
                    result = cursor.getString(idx)
                    cursor.close()
                }
                return result
            }
        }
    )
}

