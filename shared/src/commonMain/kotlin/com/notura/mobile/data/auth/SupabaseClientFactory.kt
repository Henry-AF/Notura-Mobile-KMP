package com.notura.mobile.data.auth

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.SessionManager
import io.github.jan.supabase.createSupabaseClient
import io.ktor.client.engine.HttpClientEngine

/** Public Supabase project settings. The anon key is a publishable key, not a secret. */
data class SupabaseConfig(val url: String, val anonKey: String)

/**
 * Supabase client with only the Auth plugin installed: the app reads and writes data exclusively
 * through the Notura API, never through Supabase directly.
 *
 * [sessionManager] and [httpEngine] default to supabase-kt's platform implementations (secure
 * settings storage and the platform Ktor engine); tests pass in-memory and mock versions.
 */
fun createNoturaSupabaseClient(
    config: SupabaseConfig,
    sessionManager: SessionManager? = null,
    httpEngine: HttpClientEngine? = null,
): SupabaseClient = createSupabaseClient(config.url, config.anonKey) {
    httpEngine?.let { this.httpEngine = it }
    install(Auth) {
        sessionManager?.let { this.sessionManager = it }
    }
}
