package com.solanamobile.mintyfresh.settings

import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.*
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.*
import androidx.navigation.compose.composable
import com.solanamobile.mintyfresh.composable.simplecomposables.BackButton


private const val WebViewRoute = "WebView"

fun NavController.navigateToWebView(title: String, url: String, navOptions: NavOptions? = null) {
    this.navigate("$WebViewRoute?url=$url&urlTitle=$title", navOptions)
}

fun NavGraphBuilder.webViewScreen(
    navigateUp: () -> Boolean = { true },
) {
    composable(
        route = "${WebViewRoute}?url={url}&urlTitle={urlTitle}",
        arguments = listOf(navArgument("url") { type = NavType.StringType }, navArgument("urlTitle") { type = NavType.StringType })
    ) { backStackEntry ->
        WebViewScreenContents(
            url = backStackEntry.arguments?.getString("url") ?: throw IllegalArgumentException("$WebViewRoute requires a \"url\" argument to be launched"),
            title = backStackEntry.arguments?.getString("urlTitle") ?: throw IllegalArgumentException("$WebViewRoute requires a \"title\" argument to be launched"),
            navigateUp = navigateUp,
        )
    }
}


@Composable
fun WebViewScreenContents(
    url: String,
    title: String,
    navigateUp: () -> Boolean,
) {
    Scaffold(
        modifier = Modifier
            .systemBarsPadding()
            .navigationBarsPadding(),
        topBar = {
            TopAppBar(
                backgroundColor = MaterialTheme.colorScheme.background,
                content = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BackButton(navigateUp)
                        Text(
                            modifier = Modifier.padding(end = 16.dp, start = 4.dp),
                            text = title,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            )
        },
        content = { innerPadding ->
            AndroidView(
                modifier = Modifier.padding(innerPadding),
                factory = {
                    WebView(it).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        settings.javaScriptEnabled = false
                        webViewClient = WebViewClient()
                        loadUrl(url)
                    }
                },
                update = {
                    it.loadUrl(url)
                }
            )
        }
    )
}
