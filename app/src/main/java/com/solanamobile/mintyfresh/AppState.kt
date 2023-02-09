package com.solanamobile.mintyfresh

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.google.accompanist.systemuicontroller.SystemUiController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.solanamobile.mintyfresh.gallery.galleryRoute
import com.solanamobile.mintyfresh.mymints.composables.myMintsRoute
import kotlinx.coroutines.CoroutineScope

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun rememberMintyFreshAppState(
    navController: NavHostController = rememberAnimatedNavController(),
    systemUiController: SystemUiController = rememberSystemUiController(),
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
): MintyFreshAppState {
    return remember(navController, systemUiController, coroutineScope) {
        MintyFreshAppState(navController, systemUiController, coroutineScope)
    }
}

class MintyFreshAppState(
    val navController: NavHostController,
    val systemUiController: SystemUiController,
    val coroutineScope: CoroutineScope
) {

    val currentDestination: NavDestination?
        @Composable get() = navController
            .currentBackStackEntryAsState().value?.destination

    val shouldShowAppBar: Boolean
        @Composable get() {
            val route = currentDestination?.route?.split("?")?.firstOrNull()
            return route == galleryRoute || route == myMintsRoute
        }

    val shouldShowFAB: Boolean
        @Composable get() {
            val route = currentDestination?.route?.split("?")?.firstOrNull()
            return route == galleryRoute
        }

    val useDarkIcons: Boolean
        @Composable get() = !isSystemInDarkTheme()
}
