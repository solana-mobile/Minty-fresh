package com.solanamobile.mintyfresh.composable.simplecomposables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController

data class NavigationItem(val route: String, val icon: ImageVector, val title: Int)

@Composable
fun BottomNavigationBar(
    navController: NavHostController,
    navigationItems: List<NavigationItem>,
) {
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
                    navigationItems.forEach { item ->
                        val currentDestination = navController.currentDestination
                        val selected = currentDestination?.hierarchy?.any {
                            it.route?.split("?")?.firstOrNull() == item.route.split("?").firstOrNull()
                        } == true
                        BottomNavigationItem(
                            icon = {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .then(
                                            if (selected) {
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
                                        contentDescription = stringResource(id = item.title)
                                    )
                                    Text(
                                        text = stringResource(id = item.title),
                                        style = MaterialTheme.typography.labelMedium,
                                        modifier = Modifier.padding(bottom = 10.dp)
                                    )
                                }
                            },
                            selected = selected,
                            onClick = {
                                navController.navigate(item.route) {
                                    // Pop up to the start destination of the graph to
                                    // avoid building up a large stack of destinations
                                    // on the back stack as users select items
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
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