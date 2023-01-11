package com.nft.gallery.activity

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nft.gallery.viewmodel.MyMintsViewModel

@Composable
fun MyMintPage(
    myMintsViewModel: MyMintsViewModel = hiltViewModel()
) {
    val uiState = myMintsViewModel.viewState.collectAsState().value

    LaunchedEffect(
        key1 = Unit,
        block = {
            myMintsViewModel.loadMyMints()
        }
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
    ) {
        Text(
            text = AnnotatedString(
                "My ",
                spanStyle = SpanStyle(MaterialTheme.colorScheme.onSurfaceVariant)
            ).plus(
                AnnotatedString(
                    "mints",
                    spanStyle = SpanStyle(MaterialTheme.colorScheme.onSurface)
                )
            ),
            style = MaterialTheme.typography.headlineMedium,
        )
    }
}