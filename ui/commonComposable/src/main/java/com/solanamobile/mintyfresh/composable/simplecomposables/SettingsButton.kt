package com.solanamobile.mintyfresh.composable.simplecomposables

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ManageAccounts
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.solanamobile.mintyfresh.composable.R


@Composable
fun SettingsButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = { },
) {
    IconButton(
        modifier = modifier
            .size(48.dp)
            .clip(CircleShape),
        colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        onClick = onClick
    ) {
        Icon(
            modifier = Modifier.size(24.dp),
            imageVector = Icons.Outlined.ManageAccounts,
            tint = MaterialTheme.colorScheme.onSurface,
            contentDescription = stringResource(R.string.settings)
        )
    }
}
