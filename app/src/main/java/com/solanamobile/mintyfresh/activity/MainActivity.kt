package com.solanamobile.mintyfresh.activity

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.NavHost
import com.solana.mobilewalletadapter.clientlib.ActivityResultSender
import com.solanamobile.mintyfresh.R
import com.solanamobile.mintyfresh.composable.simplecomposables.NavigationItem
import com.solanamobile.mintyfresh.composable.theme.AppTheme
import com.solanamobile.mintyfresh.gallery.cameraScreen
import com.solanamobile.mintyfresh.gallery.galleryRoute
import com.solanamobile.mintyfresh.gallery.galleryScreen
import com.solanamobile.mintyfresh.gallery.navigateToCamera
import com.solanamobile.mintyfresh.mymints.composables.*
import com.solanamobile.mintyfresh.navigation.*
import com.solanamobile.mintyfresh.nftmint.MintConfirmLayout
import com.solanamobile.mintyfresh.nftmint.mintDetailsScreen
import com.solanamobile.mintyfresh.nftmint.navigateToMintDetailsScreen
import com.solanamobile.mintyfresh.settings.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterialApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        val activityResultSender = ActivityResultSender(this)

        setContent {
            val appState = rememberMintyFreshAppState()
            val useDarkIcons = appState.useDarkIcons
            val scope = rememberCoroutineScope()

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
                    NavHost(
                        navController = appState.navController,
                        startDestination = creatingGraphRoutePattern,
                    ) {
                        creatingGraph(
                            startDestination = galleryRoute,
                            nestedGraphs = {
                                galleryScreen(
                                    navigateToDetails = appState.navController::navigateToMintDetailsScreen,
                                    navigateToCamera = appState.navController::navigateToCamera,
                                    navController = appState.navController,
                                    activityResultSender = activityResultSender,
                                    navigationItems = listOf(
                                        NavigationItem(creatingGraphRoutePattern, Icons.Outlined.Image, R.string.photos),
                                        NavigationItem(viewingGraphRoutePattern, Icons.Outlined.AutoAwesome, R.string.my_mints)
                                    ),
                                    identityUri = Uri.parse(application.getString((R.string.id_url))),
                                    iconUri = Uri.parse(application.getString(R.string.id_favico)),
                                    appName = application.getString(R.string.app_name),
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

                                        scope.launch {
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
                                    navController = appState.navController,
                                    activityResultSender = activityResultSender,
                                    navigationItems = listOf(
                                        NavigationItem(creatingGraphRoutePattern, Icons.Outlined.Image, R.string.photos),
                                        NavigationItem(viewingGraphRoutePattern, Icons.Outlined.AutoAwesome, R.string.my_mints)
                                    ),
                                    identityUri = Uri.parse(application.getString((R.string.id_url))),
                                    iconUri = Uri.parse(application.getString(R.string.id_favico)),
                                    appName = application.getString(R.string.app_name),
                                )

                                myMintsDetailsScreen(navigateUp = navigateUp)
                            }
                        )

                        settingsGraph(
                            startDestination = settingsRoute,
                            nestedGraphs = {
                                settingsScreen(
                                    onNavigateToUrl = { title, url -> appState.navController.navigateToWebView(title = title, url = url) },
                                    navigateUp = navigateUp,
                                )

                                webViewScreen(
                                    navigateUp = navigateUp,
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}
