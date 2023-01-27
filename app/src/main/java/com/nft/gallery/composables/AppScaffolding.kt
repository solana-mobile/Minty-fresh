package com.nft.gallery.composables

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MultipleStop
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.nft.gallery.*
import com.nft.gallery.R
import com.nft.gallery.navigation.NavigationItem
import com.solanamobile.mintyfresh.core.walletconnection.viewmodel.WalletConnectionViewModel
import com.solana.mobilewalletadapter.clientlib.ActivityResultSender
import com.solanamobile.mintyfresh.core.walletconnection.viewmodel.ConnectionParams

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScaffoldScreen(
    currentRoute: String,
    navController: NavHostController,
    activityResultSender: ActivityResultSender,
    walletConnectionViewModel: WalletConnectionViewModel = hiltViewModel(),
    content: @Composable () -> Unit
) {
    val viewState = walletConnectionViewModel.viewState.collectAsState().value

    Scaffold(
        floatingActionButton = {
            if (currentRoute == NavigationItem.Photos.route) {
                FloatingActionButton(
                    shape = RoundedCornerShape(corner = CornerSize(16.dp)),
                    backgroundColor = MaterialTheme.colorScheme.onBackground,
                    onClick = {
                        navController.navigate(NavigationItem.Camera.route)
                    }
                ) {
                    Icon(
                        modifier = Modifier.size(24.dp),
                        imageVector = NavigationItem.Camera.icon,
                        contentDescription = stringResource(R.string.take_pic_content_desc),
                        tint = MaterialTheme.colorScheme.background
                    )
                }
            }
        },
        topBar = {
            if (currentRoute == NavigationItem.Photos.route || currentRoute == NavigationItem.MyMints.route) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        shape = RoundedCornerShape(corner = CornerSize(24.dp)),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        contentPadding = PaddingValues(
                            start = 8.dp, end = 16.dp, top = 8.dp, bottom = 8.dp
                        ),
                        onClick = {
                            if (viewState.userAddress.isEmpty()) {
                                walletConnectionViewModel.connect(
                                    ConnectionParams(
                                        identityUri = identityUri,
                                        iconUri = iconUri,
                                        identityName = appName,
                                        rpcCluster = BuildConfig.RPC_CLUSTER
                                    ),
                                    activityResultSender = activityResultSender
                                )
                            } else {
                                walletConnectionViewModel.disconnect()
                            }
                        },
                        enabled = !viewState.noWallet
                    ) {
                        val pubKey = viewState.userAddress
                        val buttonText = if (pubKey.isEmpty()) {
                            stringResource(R.string.connect)
                        } else {
                            pubKey.take(4).plus("...").plus(pubKey.takeLast(4))
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
            }
        },
        bottomBar = {
            if (currentRoute == NavigationItem.Photos.route || currentRoute == NavigationItem.MyMints.route) {
                Column {
                    AnimatedVisibility(
                        visible = viewState.noWallet
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.primary)
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "Hey, be sure to install a Solana wallet before you get started!",
                                color = MaterialTheme.colorScheme.onPrimary,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                    BottomNavigationBar(
                        navController = navController,
                        currentRoute = currentRoute
                    )
                }
            }
        },
        content = { padding ->
            Box(
                modifier = Modifier.padding(padding)
            ) {
                content()
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    )
}

@Composable
private fun BottomNavigationBar(
    navController: NavHostController,
    currentRoute: String
) {
    val items = listOf(
        NavigationItem.Photos,
        NavigationItem.MyMints,
    )

    Surface(
        color = MaterialTheme.colorScheme.background,
    ) {
        Column {
            Divider(
                modifier = Modifier
                    .fillMaxWidth(),
                thickness = 0.5.dp,
                color = Color(0xFFE5E5EA)
            )

            Row(
                Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .selectableGroup(),
                horizontalArrangement = Arrangement.SpaceBetween,
                content = {
                    items.forEach { item ->
                        BottomNavigationItem(
                            icon = {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .then(
                                            if (currentRoute == item.route) {
                                                Modifier.background(
                                                    color = MaterialTheme.colorScheme.surfaceVariant
                                                )
                                            } else {
                                                Modifier
                                            }
                                        )
                                        .then(
                                            Modifier
                                                .padding(horizontal = 12.dp)
                                        )
                                ) {
                                    Icon(
                                        modifier = Modifier
                                            .padding(top = 5.dp, bottom = 3.dp)
                                            .size(24.dp),
                                        imageVector = item.icon,
                                        contentDescription = item.title
                                    )
                                    Text(
                                        text = item.title,
                                        style = MaterialTheme.typography.labelMedium,
                                        modifier = Modifier.padding(bottom = 10.dp)
                                    )
                                }
                            },
                            selected = currentRoute == item.route,
                            onClick = {
                                navController.navigate(item.route) {
                                    // Pop up to the start destination of the graph to
                                    // avoid building up a large stack of destinations
                                    // on the back stack as users select items
                                    navController.graph.startDestinationRoute?.let { route ->
                                        popUpTo(route) {
                                            saveState = true
                                        }
                                    }
                                    // Avoid multiple copies of the same destination when
                                    // re-selecting the same item
                                    launchSingleTop = true
                                    // Restore state when re-selecting a previously selected item
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            )

            Spacer(
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.background
                    )
                    .navigationBarsPadding()
                    .fillMaxWidth()
            )
        }
    }
}