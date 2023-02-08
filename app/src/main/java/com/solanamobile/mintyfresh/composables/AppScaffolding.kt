package com.solanamobile.mintyfresh.composables

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Surface
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.solana.mobilewalletadapter.clientlib.ActivityResultSender
import com.solanamobile.mintyfresh.R
import com.solanamobile.mintyfresh.gallery.navigateToCamera
import com.solanamobile.mintyfresh.navigation.NavigationItem
import com.solanamobile.mintyfresh.walletconnectbutton.composables.ConnectWalletButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScaffoldScreen(
    navController: NavHostController,
    activityResultSender: ActivityResultSender,
    content: @Composable () -> Unit
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val isPhotosScreen =
        navBackStackEntry?.destination?.hierarchy?.any { it.route == NavigationItem.Photos.route } == true

    Scaffold(
        floatingActionButton = {
            if (isPhotosScreen) {
                FloatingActionButton(
                    shape = RoundedCornerShape(corner = CornerSize(16.dp)),
                    backgroundColor = MaterialTheme.colorScheme.onBackground,
                    onClick = {
                        navController.navigateToCamera()
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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                ConnectWalletButton(
                    identityUri = Uri.parse(stringResource(R.string.id_url)),
                    iconUri = Uri.parse(stringResource(R.string.id_favico)),
                    identityName = stringResource(R.string.app_name),
                    activityResultSender = activityResultSender
                )
            }
        },
        bottomBar = {
            BottomNavigationBar(navController = navController)
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
private fun BottomNavigationBar(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

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

                        val selected = currentDestination?.hierarchy?.any {
                            it.route?.split("?")?.firstOrNull() == item.route
                        } == true
                        BottomNavigationItem(
                            icon = {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .then(
                                            if (selected) {
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
                                        contentDescription = stringResource(item.title)
                                    )
                                    Text(
                                        text = stringResource(item.title),
                                        style = MaterialTheme.typography.labelMedium,
                                        modifier = Modifier.padding(bottom = 10.dp)
                                    )
                                }
                            },
                            selected = selected,
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