package com.notura.mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val graph = (application as NoturaApplication).graph
        setContent {
            // Native Google sign-in (Credential Manager) is not wired yet; see PROGRESS.md.
            NoturaApp(authRepository = graph.authRepository, googleSignIn = null)
        }
    }
}
