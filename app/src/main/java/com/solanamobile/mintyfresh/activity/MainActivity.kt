package com.solanamobile.mintyfresh.activity

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddAPhoto
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavOptions
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.solana.mobilewalletadapter.clientlib.ActivityResultSender
import com.solanamobile.mintyfresh.R
import com.solanamobile.mintyfresh.composable.simplecomposables.BottomNavigationBar
import com.solanamobile.mintyfresh.composable.simplecomposables.NavigationItem
import com.solanamobile.mintyfresh.composable.theme.AppTheme
import com.solanamobile.mintyfresh.gallery.cameraScreen
import com.solanamobile.mintyfresh.gallery.galleryRoute
import com.solanamobile.mintyfresh.gallery.galleryScreen
import com.solanamobile.mintyfresh.gallery.navigateToCamera
import com.solanamobile.mintyfresh.mymints.composables.*
import com.solanamobile.mintyfresh.navigation.creatingGraph
import com.solanamobile.mintyfresh.navigation.creatingGraphRoutePattern
import com.solanamobile.mintyfresh.navigation.viewingGraph
import com.solanamobile.mintyfresh.navigation.viewingGraphRoutePattern
import com.solanamobile.mintyfresh.nftmint.MintConfirmLayout
import com.solanamobile.mintyfresh.nftmint.mintDetailsScreen
import com.solanamobile.mintyfresh.nftmint.navigateToMintDetailsScreen
import com.solanamobile.mintyfresh.rememberMintyFreshAppState
import com.solanamobile.mintyfresh.walletconnectbutton.composables.ConnectWalletButton
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @OptIn(
        ExperimentalAnimationApi::class,
        ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        val activityResultSender = ActivityResultSender(this)

        setContent {
            val appState = rememberMintyFreshAppState()
            val useDarkIcons = appState.useDarkIcons

            SideEffect {
                appState.systemUiController.setSystemBarsColor(
                    Color.Transparent,
                    darkIcons = useDarkIcons
                )
            }

            AppTheme {
                val navigateUp = { appState.navController.navigateUp() }

                val bottomSheetState =
                    rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)

                ModalBottomSheetLayout(
                    sheetShape = RoundedCornerShape(
                        topStart = 24.dp,
                        topEnd = 24.dp
                    ),
                    sheetContent = {
                        MintConfirmLayout(
                            onDoneClick = {
                                appState.coroutineScope.launch {
                                    bottomSheetState.hide()
                                }
                            }
                        )
                    },
                    sheetState = bottomSheetState
                ) {
                    Scaffold(
                        topBar = {
                            if (appState.shouldShowAppBar) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .statusBarsPadding()
                                        .padding(horizontal = 16.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    ConnectWalletButton(
                                        identityUri = Uri.parse(application.getString((R.string.id_url))),
                                        iconUri = Uri.parse(application.getString(R.string.id_favico)),
                                        identityName = application.getString(R.string.app_name),
                                        activityResultSender = activityResultSender
                                    )
                                }
                            }
                        },
                        floatingActionButton = {
                            if (appState.shouldShowFAB) {
                                FloatingActionButton(
                                    shape = RoundedCornerShape(corner = CornerSize(16.dp)),
                                    backgroundColor = MaterialTheme.colorScheme.onBackground,
                                    onClick = appState.navController::navigateToCamera
                                ) {
                                    Icon(
                                        modifier = Modifier.size(24.dp),
                                        imageVector = Icons.Outlined.AddAPhoto,
                                        contentDescription = stringResource(R.string.take_pic_content_desc),
                                        tint = MaterialTheme.colorScheme.background
                                    )
                                }
                            }
                        },
                        bottomBar = {
                            if (appState.shouldShowAppBar) {
                                BottomNavigationBar(
                                    navController = appState.navController,
                                    navigationItems = listOf(
                                        NavigationItem(
                                            creatingGraphRoutePattern,
                                            Icons.Outlined.Image,
                                            R.string.photos
                                        ),
                                        NavigationItem(
                                            viewingGraphRoutePattern,
                                            Icons.Outlined.AutoAwesome,
                                            R.string.my_mints
                                        )
                                    ),
                                )
                            }
                        },
                        content = {
                            Box(
                                modifier = Modifier.padding(it)
                            ) {
                                AnimatedNavHost(
                                    navController = appState.navController,
                                    startDestination = creatingGraphRoutePattern,
                                ) {
                                    creatingGraph(
                                        startDestination = galleryRoute,
                                        nestedGraphs = {
                                            galleryScreen(
                                                navigateToDetails = appState.navController::navigateToMintDetailsScreen,
                                            )

                                            cameraScreen(navigateToDetails = {
                                                appState.navController.navigateToMintDetailsScreen(imagePath = it)
                                            })

                                            mintDetailsScreen(
                                                navigateUp = navigateUp,
                                                onMintCompleted = {
                                                    // Remove the MintDetails page from stack first.
                                                    appState.navController.popBackStack()
                                                    appState.navController.navigateToMyMints(
                                                        forceRefresh = true,
                                                        navOptions = NavOptions.Builder().setPopUpTo(
                                                            appState.navController.graph.findStartDestination().id,
                                                            inclusive = false,
                                                            saveState = true
                                                        ).setRestoreState(true).setLaunchSingleTop(true).build()
                                                    )

                                                    appState.coroutineScope.launch {
                                                        bottomSheetState.show()
                                                    }
                                                },
                                                activityResultSender = activityResultSender,
                                                contentResolver = contentResolver,
                                                cacheDir = cacheDir,
                                                identityUri = Uri.parse(application.getString((R.string.id_url))),
                                                iconUri = Uri.parse(application.getString(R.string.id_favico)),
                                                appName = application.getString(R.string.app_name),
                                            )
                                        }
                                    )

                                    viewingGraph(
                                        startDestination = "$myMintsRoute?forceRefresh={forceRefresh}",
                                        nestedGraphs = {
                                            myMintsScreen(
                                                navigateToDetails = appState.navController::navigateToMyMintsDetails,
                                            )

                                            myMintsDetailsScreen(navigateUp = navigateUp)
                                        }
                                    )
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}
