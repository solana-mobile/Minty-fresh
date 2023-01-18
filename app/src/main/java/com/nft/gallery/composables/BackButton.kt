package com.nft.gallery.composables

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.nft.gallery.R

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
            imageVector = ImageVector.vectorResource(id = R.drawable.arrow_back),
            contentDescription = "Back"
        )
    }
}
