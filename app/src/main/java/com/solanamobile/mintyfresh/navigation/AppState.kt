package com.solanamobile.mintyfresh.navigation

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.systemuicontroller.SystemUiController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.CoroutineScope

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun rememberMintyFreshAppState(
//    navController: NavHostController = rememberAnimatedNavController(),
    navController: NavHostController = rememberNavController(),
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

    val useDarkIcons: Boolean
        @Composable get() = !isSystemInDarkTheme()
}
