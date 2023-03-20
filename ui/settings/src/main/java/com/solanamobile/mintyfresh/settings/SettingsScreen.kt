package com.solanamobile.mintyfresh.settings

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity

const val settingsRoute = "settings"

fun NavController.navigateToSettingsPage(navOptions: NavOptions? = null) {
    this.navigate(settingsRoute, navOptions)
}

@OptIn(ExperimentalMaterial3Api::class)
fun NavGraphBuilder.settingsScreen(
    onNavigateToUrl: (title: String, url: String) -> Unit = { _, _ -> }
) {
    composable(route = settingsRoute) {
        val context = LocalContext.current
        Scaffold(
            topBar = {
            },
            content = { padding ->
                LazyColumn(
                    modifier = Modifier.padding(padding)
                ) {
                    item {
                        val title = stringResource(R.string.privacy_policy_title)
                        val url = stringResource(R.string.privacy_policy_url)
                        UrlClickable(title = title) {
                            onNavigateToUrl(title, url)
                        }
                    }
                    item {
                        val title = stringResource(R.string.terms_of_service_title)
                        val url = stringResource(R.string.terms_of_service_url)
                        UrlClickable(title = title) {
                            onNavigateToUrl(title, url)
                        }
                    }
                    item {
                        Box(
                            modifier = Modifier
                                .clickable {
                                    context.startActivity(
                                        Intent(
                                            context,
                                            OssLicensesMenuActivity::class.java
                                        )
                                    )
                                }
                                .padding(horizontal = 16.dp)
                                .fillMaxWidth()
                                .defaultMinSize(minHeight = 64.dp),
                            contentAlignment = Alignment.CenterStart,
                        ) {
                            Text(
                                text = stringResource(R.string.open_source_licenses_title),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            },
            containerColor = MaterialTheme.colorScheme.background
        )
    }
}

@Composable
fun UrlClickable(
    title: String,
    onNavigateToUrl: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .clickable {
                onNavigateToUrl()
            }
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .defaultMinSize(minHeight = 64.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

