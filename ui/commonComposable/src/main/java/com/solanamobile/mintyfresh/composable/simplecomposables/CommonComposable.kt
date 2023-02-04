package com.solanamobile.mintyfresh.composable.simplecomposables

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.solanamobile.mintyfresh.composable.R

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
fun EmptyView(
    permissionState: MultiplePermissionsState,
    bodyText: String,
    buttonText: String
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            bodyText,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        Button(
            shape = RoundedCornerShape(corner = CornerSize(16.dp)),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onBackground),
            onClick = {
                permissionState.launchMultiplePermissionRequest()
            },
            content = {
                Text(buttonText)
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
