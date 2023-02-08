package com.solanamobile.mintyfresh.activity

import android.content.Intent
import android.net.Uri
import android.os.Build
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.solana.mobilewalletadapter.clientlib.ActivityResultSender
import com.solanamobile.mintyfresh.R
import com.solanamobile.mintyfresh.composable.theme.AppTheme
import com.solanamobile.mintyfresh.composables.ScaffoldScreen
import com.solanamobile.mintyfresh.gallery.Camera
import com.solanamobile.mintyfresh.gallery.Gallery
import com.solanamobile.mintyfresh.mymints.composables.MyMintPage
import com.solanamobile.mintyfresh.mymints.composables.myMintsDetailsScreen
import com.solanamobile.mintyfresh.mymints.composables.navigateToMyMintsDetails
import com.solanamobile.mintyfresh.navigation.NavigationItem
import com.solanamobile.mintyfresh.nftmint.MintConfirmLayout
import com.solanamobile.mintyfresh.nftmint.MintDetailsPage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.File

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
                                        animNavController.navigate("${NavigationItem.MintDetail.route}?imagePath=$it")
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

                        composable(NavigationItem.Camera.route) {
                            Camera(
                                navigateToDetails = {
                                    animNavController.navigate("${NavigationItem.MintDetail.route}?imagePath=$it")
                                }
                            )
                        }
                        composable(
                            route = "${NavigationItem.MintDetail.route}?imagePath={imagePath}",
                            arguments = listOf(navArgument("imagePath") {
                                type = NavType.StringType
                            }),
                            deepLinks = listOf(navDeepLink {
                                uriPattern = "{imagePath}"
                                action = Intent.ACTION_SEND
                                mimeType = "image/*"
                            })
                        ) { backStackEntry ->
                            val imagePath = backStackEntry.arguments?.getString("imagePath")
                            val deepLinkIntent: Intent? =
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    backStackEntry.arguments?.getParcelable(
                                        NavController.KEY_DEEP_LINK_INTENT,
                                        Intent::class.java
                                    )
                                } else {
                                    @Suppress("DEPRECATION")
                                    backStackEntry.arguments?.getParcelable(
                                        NavController.KEY_DEEP_LINK_INTENT
                                    )
                                }
                            val clipDataUri = deepLinkIntent?.clipData?.getItemAt(0)?.uri
                            val clipDataPath = clipDataUri?.let {
                                val input = contentResolver.openInputStream(clipDataUri)
                                val file = File.createTempFile("shared", ".image", cacheDir)

                                input?.let {
                                    file.writeBytes(input.readBytes())
                                    input.close()
                                    file.toPath()
                                }
                            }?.toString()

                            MintDetailsPage(
                                imagePath = imagePath ?: clipDataPath
                                ?: throw IllegalStateException("${NavigationItem.MintDetail.route} requires an \"imagePath\" argument to be launched"),
                                navigateUp = {
                                    animNavController.navigateUp()
                                },
                                onMintCompleted = {
                                    animNavController.navigate("${NavigationItem.MyMints.route}?forceRefresh=true") {
                                        popUpTo(NavigationItem.Photos.route)
                                    }

                                    scope.launch {
                                        bottomSheetState.show()
                                    }
                                },
                                identityUri = Uri.parse(stringResource(R.string.id_url)),
                                iconUri = Uri.parse(stringResource(R.string.id_favico)),
                                identityName = stringResource(R.string.app_name),
                                intentSender = activityResultSender
                            )
                        }
                        myMintsDetailsScreen(navigateUp = { onNavigateUp() })
                    }
                }
            }
        }
    }
}
