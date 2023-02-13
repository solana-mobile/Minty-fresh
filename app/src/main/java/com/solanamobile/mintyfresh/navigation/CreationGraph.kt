package com.solanamobile.mintyfresh.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.navigation

const val creatingGraphRoutePattern = "createGraph"

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
