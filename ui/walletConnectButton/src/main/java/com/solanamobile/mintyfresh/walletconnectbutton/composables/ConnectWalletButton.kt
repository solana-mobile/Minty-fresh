package com.solanamobile.mintyfresh.walletconnectbutton.composables

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MultipleStop
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.solana.mobilewalletadapter.clientlib.ActivityResultSender
import com.solanamobile.mintyfresh.walletconnectbutton.R
import com.solanamobile.mintyfresh.walletconnectbutton.viewmodel.WalletConnectionViewModel

@Composable
fun ConnectWalletButton(
    identityUri: Uri,
    iconUri: Uri,
    identityName: String,
    activityResultSender: ActivityResultSender,
    modifier: Modifier = Modifier,
    walletConnectionViewModel: WalletConnectionViewModel = hiltViewModel(),
) {
    val viewState = walletConnectionViewModel.viewState.collectAsState().value

    Button(
        modifier = modifier,
        shape = RoundedCornerShape(corner = CornerSize(24.dp)),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        contentPadding = PaddingValues(
            start = 8.dp, end = 16.dp, top = 8.dp, bottom = 8.dp
        ),
        onClick = {
            if (viewState.userAddress.isEmpty()) {
                walletConnectionViewModel.connect(identityUri, iconUri, identityName, activityResultSender)
            } else {
                walletConnectionViewModel.disconnect()
            }
        },
        enabled = !viewState.noWallet
    ) {
        val pubKey = viewState.userAddress
        val buttonText = when {
            viewState.noWallet -> "Please install a wallet"
            pubKey.isEmpty() -> stringResource(R.string.connect)
            viewState.userAddress.isNotEmpty() -> pubKey.take(4).plus("...").plus(pubKey.takeLast(4))
            else -> ""
        }

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (pubKey.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.onBackground),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        modifier = Modifier.size(16.dp),
                        imageVector = Icons.Filled.MultipleStop,
                        tint = MaterialTheme.colorScheme.background,
                        contentDescription = null
                    )
                }
            }
            Text(
                modifier = Modifier.padding(start = 8.dp),
                text = buttonText,
                maxLines = 1,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}