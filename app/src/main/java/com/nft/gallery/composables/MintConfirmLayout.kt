package com.nft.gallery.composables

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.nft.gallery.R

@Composable
fun MintConfirmLayout(
    onDoneClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .navigationBarsPadding()
            .fillMaxWidth()
            .wrapContentHeight()
            .background(
                color = MaterialTheme.colorScheme.background,
                shape = RoundedCornerShape(
                    topStart = 24.dp,
                    topEnd = 24.dp
                )
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            modifier = Modifier
                .padding(
                    top = 42.dp
                )
                .width(293.dp)
                .height(122.dp),
            painter = painterResource(
                id = R.drawable.mint_confirm
            ),
            contentDescription = "Sparkle image",
            contentScale = ContentScale.Inside
        )
        Text(
            modifier = Modifier
                .padding(
                    top = 19.dp
                ),
            text = "You minted an NFT!",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            modifier = Modifier
                .padding(
                    top = 10.dp
                ),
            text = "It's been added to your connected wallet",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Button(
            modifier = Modifier
                .padding(
                    top = 70.dp,
                    bottom = 44.dp
                ),
            shape = RoundedCornerShape(corner = CornerSize(16.dp)),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onBackground),
            onClick = onDoneClick
        ) {
            Text(
                text = "Done"
            )
        }
    }
}