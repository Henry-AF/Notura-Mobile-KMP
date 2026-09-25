package com.notura.mobile

import android.app.Application
import io.ktor.client.engine.okhttp.OkHttp

class NoturaApplication : Application() {
    /** Created once per process so the Supabase session survives activity recreation. */
    val graph: AppGraph by lazy {
        AppGraph(
            config = AppConfig(
                apiBaseUrl = BuildConfig.NOTURA_API_BASE_URL,
                supabaseUrl = BuildConfig.SUPABASE_URL,
                supabaseAnonKey = BuildConfig.SUPABASE_ANON_KEY,
            ),
            httpEngine = OkHttp.create(),
        )
    }
}
