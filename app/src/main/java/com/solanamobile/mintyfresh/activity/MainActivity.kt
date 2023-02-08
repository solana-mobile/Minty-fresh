package com.solanamobile.mintyfresh.activity

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.solana.mobilewalletadapter.clientlib.ActivityResultSender
import com.solanamobile.mintyfresh.R
import com.solanamobile.mintyfresh.composable.theme.AppTheme
import com.solanamobile.mintyfresh.composables.ScaffoldScreen
import com.solanamobile.mintyfresh.gallery.Gallery
import com.solanamobile.mintyfresh.gallery.cameraScreen
import com.solanamobile.mintyfresh.mymints.composables.MyMintPage
import com.solanamobile.mintyfresh.mymints.composables.myMintsDetailsScreen
import com.solanamobile.mintyfresh.mymints.composables.navigateToMyMintsDetails
import com.solanamobile.mintyfresh.navigation.NavigationItem
import com.solanamobile.mintyfresh.nftmint.MintConfirmLayout
import com.solanamobile.mintyfresh.nftmint.mintDetailsScreen
import com.solanamobile.mintyfresh.nftmint.navigateToMintDetailsScreen
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @OptIn(
        ExperimentalAnimationApi::class,
        ExperimentalMaterialApi::class
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        val activityResultSender = ActivityResultSender(this)

        setContent {
            val animNavController = rememberAnimatedNavController()
            val systemUiController = rememberSystemUiController()
            val useDarkIcons = !isSystemInDarkTheme()
            val scope = rememberCoroutineScope()

            SideEffect {
                systemUiController.setSystemBarsColor(
                    Color.Transparent,
                    darkIcons = useDarkIcons
                )
            }

            AppTheme {
                val navigateUp = { animNavController.navigateUp() }

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
                                scope.launch {
                                    bottomSheetState.hide()
                                }
                            }
                        )
                    },
                    sheetState = bottomSheetState
                ) {
                    AnimatedNavHost(
                        navController = animNavController,
                        startDestination = NavigationItem.Photos.route,
                    ) {
                        composable(NavigationItem.Photos.route) {
                            ScaffoldScreen(
                                activityResultSender = activityResultSender,
                                navController = animNavController
                            ) {
                                Gallery(
                                    navigateToDetails = {
                                        animNavController.navigateToMintDetailsScreen(imagePath = it)
                                    }
                                )
                            }
                        }
                        composable(
                            route = "${NavigationItem.MyMints.route}?forceRefresh={forceRefresh}",
                            arguments = listOf(navArgument("forceRefresh") {
                                type = NavType.BoolType
                                defaultValue = false
                            }),
                        ) { backStackEntry ->
                            val forceRefresh = backStackEntry.arguments?.getBoolean("forceRefresh")

                            ScaffoldScreen(
                                activityResultSender = activityResultSender,
                                navController = animNavController
                            ) {
                                MyMintPage(
                                    forceRefresh = forceRefresh
                                        ?: throw IllegalStateException("Argument required")
                                ) {
                                    animNavController.navigateToMyMintsDetails(index = it)
                                }
                            }
                        }

                        cameraScreen(navigateToDetails = {
                            animNavController.navigateToMintDetailsScreen(imagePath = it)
                        })

                        mintDetailsScreen(
                            navigateUp = navigateUp,
                            onMintCompleted = {
                                animNavController.navigate("${NavigationItem.MyMints.route}?forceRefresh=true") {
                                    popUpTo(NavigationItem.Photos.route)
                                }

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
                        myMintsDetailsScreen(navigateUp = navigateUp)
                    }
                }
            }
        }
    }
}
