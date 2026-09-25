package com.notura.mobile

import com.notura.mobile.data.auth.AuthRepository
import com.notura.mobile.data.auth.SupabaseAuthRepository
import com.notura.mobile.data.auth.SupabaseConfig
import com.notura.mobile.data.auth.createNoturaSupabaseClient
import com.notura.mobile.data.user.NoturaUserRepository
import com.notura.mobile.data.user.UserRepository
import com.notura.mobile.network.NoturaApiClient
import com.notura.mobile.network.createNoturaHttpClient
import io.github.jan.supabase.auth.auth
import io.ktor.client.engine.HttpClientEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/** Environment settings, provided by each platform (BuildConfig on Android, Info.plist on iOS). */
data class AppConfig(
    val apiBaseUrl: String,
    val supabaseUrl: String,
    val supabaseAnonKey: String,
)

/** Object graph shared by Android and iOS. The HTTP engine is the platform's (OkHttp / Darwin). */
class AppGraph(config: AppConfig, httpEngine: HttpClientEngine) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val supabase = createNoturaSupabaseClient(
        config = SupabaseConfig(config.supabaseUrl, config.supabaseAnonKey),
        httpEngine = httpEngine,
    )

    val authRepository: AuthRepository = SupabaseAuthRepository(supabase.auth, scope)

    private val apiClient = NoturaApiClient(
        httpClient = createNoturaHttpClient(httpEngine),
        baseUrl = config.apiBaseUrl,
        tokens = authRepository,
    )

    val userRepository: UserRepository = NoturaUserRepository(apiClient)
}
