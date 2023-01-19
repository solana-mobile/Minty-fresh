package com.nft.gallery.activity

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.nft.gallery.composables.*
import com.nft.gallery.theme.AppTheme
import com.nft.gallery.theme.NavigationItem
import com.nft.gallery.viewmodel.PerformMintViewModel
import com.solana.mobilewalletadapter.clientlib.ActivityResultSender
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import javax.annotation.concurrent.GuardedBy

@AndroidEntryPoint
class MainActivity : ComponentActivity(), ActivityResultSender {

    private val startForResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {}

    override fun launch(intent: Intent) {
        try {
            startForResult.launch(intent)
        } catch (exception: Exception) {
            lifecycleScope.launch(Dispatchers.Main) {
                Toast.makeText(
                    this@MainActivity,
                    "You need to install a Solana Wallet first (Solflare or Phantom)",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    @OptIn(
        ExperimentalAnimationApi::class
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            val animNavController = rememberAnimatedNavController()
            val systemUiController = rememberSystemUiController()
            val useDarkIcons = !isSystemInDarkTheme()

            SideEffect {
                systemUiController.setSystemBarsColor(
                    Color.Transparent,
                    darkIcons = useDarkIcons
                )
            }

            AppTheme {
                val navigateUp = { animNavController.navigateUp() }

                AnimatedNavHost(
                    navController = animNavController,
                    startDestination = NavigationItem.Photos.route,
                ) {
                    composable(NavigationItem.Camera.route) {
                        ScaffoldScreen(
                            currentRoute = NavigationItem.Camera.route,
                            activityResultSender = this@MainActivity,
                            navController = animNavController
                        ) {
                            Camera(
                                navigateToDetails = {
                                    animNavController.navigate("${NavigationItem.MintDetail.route}?imagePath=$it")
                                }
                            )
                        }
                    }
                    composable(NavigationItem.Photos.route) {
                        ScaffoldScreen(
                            currentRoute = NavigationItem.Photos.route,
                            activityResultSender = this@MainActivity,
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

                        ScaffoldScreen(
                            currentRoute = NavigationItem.MintDetail.route,
                            activityResultSender = this@MainActivity,
                            navController = animNavController
                        ) {
                            MintDetailsPage(
                                imagePath = imagePath ?: clipDataPath
                                ?: throw IllegalStateException("${NavigationItem.MintDetail.route} requires an \"imagePath\" argument to be launched"),
                                navigateUp = {
                                    animNavController.navigateUp()
                                },
                                intentSender = object : ActivityResultSender {
                                    override fun launch(intent: Intent) {
                                        intentSender.startActivityForResult(intent) {
                                            // ???
                                        }
                                    }
                                }
                            )
                        }
                    }
                    composable(NavigationItem.MyMints.route) {
                        ScaffoldScreen(
                            currentRoute = NavigationItem.MyMints.route,
                            activityResultSender = this@MainActivity,
                            navController = animNavController
                        ) {
                            MyMintPage {
                                animNavController.navigate("${NavigationItem.MyMintsDetails.route}?index=$it")
                            }
                        }
                    }
                    composable(
                        route = "${NavigationItem.MyMintsDetails.route}?index={index}",
                        arguments = listOf(navArgument("index") { type = NavType.IntType }),
                    ) { backStackEntry ->
                        ScaffoldScreen(
                            currentRoute = NavigationItem.MyMintsDetails.route,
                            activityResultSender = this@MainActivity,
                            navController = animNavController
                        ) {
                            MyMintsDetails(
                                index = backStackEntry.arguments?.getInt("index")
                                    ?: throw IllegalStateException("${NavigationItem.MyMintsDetails.route} requires an \"index\" argument to be launched"),
                                navigateUp = navigateUp,
                            )
                        }
                    }
                }
            }
        }
    }

    private val activityResultLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            intentSender.onActivityComplete()
        }

    private val intentSender = object : PerformMintViewModel.StartActivityForResultSender {
        @GuardedBy("this")
        private var callback: (() -> Unit)? = null

        override fun startActivityForResult(
            intent: Intent,
            onActivityCompleteCallback: () -> Unit
        ) {
            synchronized(this) {
                check(callback == null) { "Received an activity start request while another is pending" }
                callback = onActivityCompleteCallback
            }
            activityResultLauncher.launch(intent)
        }

        fun onActivityComplete() {
            synchronized(this) {
                callback?.let { it() }
                callback = null
            }
        }
    }
}
