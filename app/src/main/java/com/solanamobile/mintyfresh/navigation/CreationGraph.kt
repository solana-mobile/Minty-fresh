package com.solanamobile.mintyfresh.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.navigation

const val creatingGraphRoutePattern = "createGraph"

fun NavController.navigateToCreatingGraph(navOptions: NavOptions? = null) {
    this.navigate(creatingGraphRoutePattern, navOptions)
}

fun NavGraphBuilder.creatingGraph(
    startDestination: String,
    nestedGraphs: NavGraphBuilder.() -> Unit,
) {
    navigation(
        route = creatingGraphRoutePattern,
        startDestination = startDestination,
    ) {
        nestedGraphs()
    }
}
