package com.nft.gallery

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import coil.decode.VideoFrameDecoder
import coil.request.ImageRequest
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()
            AppTheme {
                Scaffold(
                    bottomBar = { BottomNavigationBar(navController) },
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
            composable(NavigationItem.Photos.route) {
                Gallery()
            }
            composable(NavigationItem.Videos.route) {
                Videos()
            }
        }
    }

    @OptIn(ExperimentalPermissionsApi::class)
    @Composable
    fun Gallery(
        imageViewModel: ImageViewModel = hiltViewModel()
    ) {
        val permissionsRequired = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            listOf(
                Manifest.permission.READ_MEDIA_IMAGES,
            )
        } else {
            listOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
            )
        }

        PermissionView(
            permissionsRequired,
            content = {
                val uiState = imageViewModel.getImageList().collectAsState().value

                LaunchedEffect(
                    key1 = Unit,
                    block = {
                        imageViewModel.loadAllImages()
                    }
                )
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 110.dp)
                ) {
                    itemsIndexed(items = uiState) { index, _ ->
                        AsyncImage(
                            modifier = Modifier
                                .width(110.dp)
                                .height(110.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(uiState[index])
                                .size(110)
                                .crossfade(false)
                                .build(),
                            contentDescription = null,
                            placeholder = ColorPainter(MaterialTheme.colorScheme.surfaceVariant),
                            contentScale = ContentScale.Crop,
                        )
                    }
                }
            },
            emptyView = {
                EmptyView(it)
            }
        )
    }

    @OptIn(ExperimentalPermissionsApi::class)
    @Composable
    fun Videos(
        imageViewModel: ImageViewModel = hiltViewModel()
    ) {
        val permissionsRequired = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            listOf(
                Manifest.permission.READ_MEDIA_VIDEO,
            )
        } else {
            listOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
            )
        }

        PermissionView(
            permissionsRequired,
            content = {
                val uiState = imageViewModel.getVideoList().collectAsState().value

                LaunchedEffect(
                    key1 = Unit,
                    block = {
                        imageViewModel.loadAllVideos()
                    }
                )
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 110.dp)
                ) {
                    itemsIndexed(items = uiState) { index, _ ->

                        val context = LocalContext.current

                        val imageLoader = ImageLoader.Builder(context)
                            .components {
                                add(VideoFrameDecoder.Factory())
                            }.crossfade(true)
                            .build()

                        val painter = rememberAsyncImagePainter(
                            model = uiState[index],
                            imageLoader = imageLoader,
                        )

                        Image(
                            painter = painter,
                            contentDescription = "",
                            contentScale = ContentScale.Crop,
                            alignment = Alignment.Center,
                            modifier = Modifier
                                .width(110.dp)
                                .height(110.dp)
                        )
                    }
                }
            },
            emptyView = {
                EmptyView(it)
            }
        )
    }

    @OptIn(ExperimentalPermissionsApi::class)
    @Composable
    fun PermissionView(
        permissions: List<String>,
        content: @Composable () -> Unit,
        emptyView: @Composable (permissionState: MultiplePermissionsState) -> Unit
    ) {
        val permissionState = rememberMultiplePermissionsState(permissions)

        if (permissionState.allPermissionsGranted) {
            content()
        } else {
            emptyView(permissionState)
        }
    }

    @OptIn(ExperimentalPermissionsApi::class)
    @Composable
    fun EmptyView(permissionState: MultiplePermissionsState) {
        Column(modifier = Modifier.fillMaxSize()) {
            val textToShow = if (permissionState.shouldShowRationale) {
                "The camera is important for this app. Please grant the permission."
            } else {
                "Camera permission required for this feature to be available. " +
                        "Please grant the permission"
            }
            Text(textToShow)

            Button(
                onClick = {
                    permissionState.launchMultiplePermissionRequest()
                },
                content = {
                    Text("Grant")
                }
            )
        }
    }


    @Composable
    fun BottomNavigationBar(navController: NavHostController) {
        val items = listOf(
            NavigationItem.Photos,
            NavigationItem.Videos,
        )

        BottomNavigation(
            backgroundColor = MaterialTheme.colorScheme.surface,
        ) {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route

            items.forEach { item ->
                BottomNavigationItem(
                    icon = {
                        Icon(
                            modifier = Modifier.size(24.dp),
                            painter = painterResource(id = item.icon),
                            contentDescription = item.title
                        )
                    },
                    label = {
                        Text(text = item.title)
                    },
                    selectedContentColor = MaterialTheme.colorScheme.onSurface,
                    unselectedContentColor = MaterialTheme.colorScheme.onSurface,
                    alwaysShowLabel = true,
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
    }
}