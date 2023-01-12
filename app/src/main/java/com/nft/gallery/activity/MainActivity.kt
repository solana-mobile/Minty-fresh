package com.nft.gallery.activity

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.navigation.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.nft.gallery.composables.*
import com.nft.gallery.theme.AppTheme
import com.nft.gallery.theme.NavigationItem
import com.nft.gallery.viewmodel.WalletConnectionViewModel
import com.solana.mobilewalletadapter.clientlib.ActivityResultSender
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : ComponentActivity(), ActivityResultSender {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            val navController = rememberNavController()
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route

            val systemUiController = rememberSystemUiController()
            val useDarkIcons = !isSystemInDarkTheme()

            SideEffect {
                systemUiController.setSystemBarsColor(Color.Transparent, darkIcons = useDarkIcons)
            }

            val viewState = walletConnectionViewModel.viewState.collectAsState().value

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
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .statusBarsPadding()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.End
                            ) {
                                LaunchedEffect(
                                    key1 = Unit,
                                    block = {
                                        walletConnectionViewModel.loadConnection()
                                    }
                                )
                                Button(
                                    shape = RoundedCornerShape(corner = CornerSize(24.dp)),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                    contentPadding = PaddingValues(
                                        start = 8.dp, end = 16.dp, top = 8.dp, bottom = 8.dp
                                    ),
                                    onClick = {
                                        walletConnectionViewModel.connect(this@MainActivity)
                                    }
                                ) {
                                    val pubKey = viewState.userAddress
                                    val buttonText = if (pubKey.isEmpty()) {
                                        "Connect"
                                    } else {
                                        pubKey.take(4).plus("...").plus(pubKey.takeLast(4))
                                    }
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        if(pubKey.isNotEmpty()) {
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
                MyMintPage {
                    navController.navigate("${NavigationItem.MyMintsDetails.route}?index=$it")
                }
            }
            composable(
                route = "${NavigationItem.MyMintsDetails.route}?index={index}",
                arguments = listOf(navArgument("index") { type = NavType.IntType }),
            ) { backStackEntry ->
                MyMintsDetails(
                    backStackEntry.arguments?.getInt("index")
                        ?: throw IllegalStateException("${NavigationItem.MyMintsDetails.route} requires an \"index\" argument to be launched")
                )
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

    private val walletConnectionViewModel: WalletConnectionViewModel by viewModels()

    override fun launch(intent: Intent) {
        startActivityForResult(intent, 0)
    }
}
