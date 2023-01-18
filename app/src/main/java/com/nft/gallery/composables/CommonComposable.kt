package com.nft.gallery.composables

import android.Manifest
import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.rememberMultiplePermissionsState

private val PERMISSION_TO_DESCRIPTION = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
    mapOf(
        Manifest.permission.CAMERA to "Camera",
        Manifest.permission.RECORD_AUDIO to "Microphone",
        Manifest.permission.READ_EXTERNAL_STORAGE to "Storage",
        Manifest.permission.READ_MEDIA_IMAGES to "Photos and Media"
    )
} else {
    mapOf(
        Manifest.permission.CAMERA to "Camera",
        Manifest.permission.RECORD_AUDIO to "Microphone",
        Manifest.permission.WRITE_EXTERNAL_STORAGE to "Storage",
        Manifest.permission.READ_EXTERNAL_STORAGE to "Storage",
    )
}

@Composable
fun EmptyView(
    text: String,
    modifier: Modifier,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text,
            modifier = modifier,
        )
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
        val revokedPermissions = permissionState.revokedPermissions.map {
            PERMISSION_TO_DESCRIPTION.getOrDefault(
                it.permission,
                ""
            )
        }
        val textToShow = if (permissionState.shouldShowRationale) {
            "${revokedPermissions.joinToString(separator = ", ")} permission is important for this app. Please grant the permission."
        } else {
            "${revokedPermissions.joinToString(separator = ", ")} permission required for this feature to be available. " +
                    "Please grant the permission"
        }
        Text(textToShow, modifier = Modifier.padding(vertical = 16.dp))

        Button(
            shape = RoundedCornerShape(corner = CornerSize(16.dp)),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onBackground),
            onClick = {
                permissionState.launchMultiplePermissionRequest()
            },
            content = {
                Text("Grant")
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

@Composable
fun ErrorView(
    text: String,
    buttonText: String,
    modifier: Modifier,
    onButtonClick: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text,
            modifier = modifier,
        )
        Button(
            shape = RoundedCornerShape(corner = CornerSize(16.dp)),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onBackground),
            onClick = {
                onButtonClick()
            },
            content = {
                Text(buttonText)
            }
        )
    }
}
