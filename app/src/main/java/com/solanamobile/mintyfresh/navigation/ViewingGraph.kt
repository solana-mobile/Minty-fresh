package com.solanamobile.mintyfresh.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.navigation

const val viewingGraphRoutePattern = "viewingGraph"

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
