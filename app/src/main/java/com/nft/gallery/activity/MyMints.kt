package com.nft.gallery.activity

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "My Mints",
            fontSize = 20.sp,
            lineHeight = 30.sp,
        )
    }
}