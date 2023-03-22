package com.solanamobile.mintyfresh.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.navigation

const val settingsGraphRoutePattern = "settingsGraph"

fun NavGraphBuilder.settingsGraph(
    startDestination: String,
    nestedGraphs: NavGraphBuilder.() -> Unit,
) {
    navigation(
        route = settingsGraphRoutePattern,
        startDestination = startDestination,
    ) {
        nestedGraphs()
    }
}
