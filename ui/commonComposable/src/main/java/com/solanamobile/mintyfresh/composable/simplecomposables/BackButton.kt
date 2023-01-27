package com.solanamobile.mintyfresh.composable.simplecomposables

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.solanamobile.mintyfresh.composable.R

@Composable
fun BackButton(
    navigateUp: () -> Boolean = { true },
) {
    IconButton(
        modifier = Modifier
            .size(48.dp),
        onClick = { navigateUp() }
    ) {
        Icon(
            modifier = Modifier
                .size(24.dp),
            tint = MaterialTheme.colorScheme.onSurface,
            imageVector = Icons.Filled.ArrowBack,
            contentDescription = stringResource(R.string.back)
        )
    }
}
