package com.notura.mobile

import androidx.compose.ui.window.ComposeUIViewController
import io.ktor.client.engine.darwin.Darwin
import platform.UIKit.UIViewController

private var graph: AppGraph? = null

/** Entry point called from Swift with the values of Info.plist (see iosApp). */
fun MainViewController(apiBaseUrl: String, supabaseUrl: String, supabaseAnonKey: String): UIViewController {
    val appGraph = graph ?: AppGraph(
        config = AppConfig(apiBaseUrl = apiBaseUrl, supabaseUrl = supabaseUrl, supabaseAnonKey = supabaseAnonKey),
        httpEngine = Darwin.create(),
    ).also { graph = it }
    return ComposeUIViewController {
        // Native Google sign-in is not wired yet; see PROGRESS.md.
        NoturaApp(authRepository = appGraph.authRepository, googleSignIn = null)
    }
}
