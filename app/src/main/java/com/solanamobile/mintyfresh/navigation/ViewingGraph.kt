package com.solanamobile.mintyfresh.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.navigation

const val viewingGraphRoutePattern = "viewingGraph"

fun NavController.navigateToViewingGraph(navOptions: NavOptions? = null) {
    this.navigate(viewingGraphRoutePattern, navOptions)
}

fun NavGraphBuilder.viewingGraph(
    startDestination: String,
    nestedGraphs: NavGraphBuilder.() -> Unit,
) {
    navigation(
        route = viewingGraphRoutePattern,
        startDestination = startDestination,
    ) {
        nestedGraphs()
    }
}
