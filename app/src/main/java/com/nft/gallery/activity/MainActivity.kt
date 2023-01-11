package com.nft.gallery.activity

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.GuardedBy
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Surface
import androidx.compose.material.contentColorFor
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.navigation.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.nft.gallery.composables.Camera
import com.nft.gallery.composables.Gallery
import com.nft.gallery.composables.MintDetailsPage
import com.nft.gallery.composables.MyMintPage
import com.nft.gallery.theme.AppTheme
import com.nft.gallery.theme.NavigationItem
import com.nft.gallery.viewmodel.WalletConnectionViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route
            AppTheme {
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
                                    contentDescription = "Take Picture",
                                    tint = MaterialTheme.colorScheme.background
                                )
                            }
                        }
                    },
                    topBar = {
                        if (currentRoute == NavigationItem.Photos.route || currentRoute == NavigationItem.MyMints.route) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                Button(
                                    shape = RoundedCornerShape(corner = CornerSize(16.dp)),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                    onClick = {
                                        walletConnectionViewModel.authorize(intentSender)
                                    }
                                ) {
                                    val buttonText = walletConnectionViewModel.uiState.collectAsState().value.publicKey?.toString() ?: "Collect"
                                    Text(
                                        text = buttonText,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    },
                    bottomBar = {
                        if (currentRoute == NavigationItem.Photos.route || currentRoute == NavigationItem.MyMints.route) {
                            BottomNavigationBar(navController = navController)
                        }
                    },
                    content = { padding ->
                        Box(modifier = Modifier.padding(padding)) {
                            Navigation(navController = navController)
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.background
                )
            }
        }
    }

    @Composable
    fun Navigation(navController: NavHostController) {
        NavHost(navController, startDestination = NavigationItem.Photos.route) {
            composable(NavigationItem.Camera.route) {
                Camera(
                    navigateToDetails = {
                        navController.navigate("${NavigationItem.MintDetail.route}?imagePath=$it")
                    }
                )
            }
            composable(NavigationItem.Photos.route) {
                Gallery(
                    navigateToDetails = {
                        navController.navigate("${NavigationItem.MintDetail.route}?imagePath=$it")
                    }
                )
            }
            composable(
                route = "${NavigationItem.MintDetail.route}?imagePath={imagePath}",
                arguments = listOf(navArgument("imagePath") { type = NavType.StringType }),
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
                val clipDataUri = deepLinkIntent?.clipData?.getItemAt(0)?.uri?.toString()

                MintDetailsPage(
                    imagePath = imagePath ?: clipDataUri
                    ?: throw IllegalStateException("${NavigationItem.MintDetail.route} requires an \"imagePath\" argument to be launched"),
                    navigateUp = {
                        navController.navigateUp()
                    }
                )
            }
            composable(NavigationItem.MyMints.route) {
                MyMintPage()
            }
        }
    }

    @Composable
    fun BottomNavigationBar(navController: NavHostController) {
        val items = listOf(
            NavigationItem.Photos,
            NavigationItem.MyMints,
        )

        Surface(
            color = MaterialTheme.colorScheme.surface,
            contentColor = contentColorFor(MaterialTheme.colorScheme.surface),
            elevation = 8.dp,
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .selectableGroup(),
                horizontalArrangement = Arrangement.SpaceBetween,
                content = {
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentRoute = navBackStackEntry?.destination?.route

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
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.padding(bottom = 10.dp)
                                    )
                                }
                            },
                            selectedContentColor = MaterialTheme.colorScheme.onSurface,
                            unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
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
        }
    }

    private val walletConnectionViewModel: WalletConnectionViewModel by viewModels()

    private val activityResultLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            intentSender.onActivityComplete()
        }

    private val intentSender = object : WalletConnectionViewModel.StartActivityForResultSender {
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