package com.notura.mobile.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/** Tolerant decoding: the API may add fields without breaking older app versions. */
val NoturaJson: Json = Json {
    ignoreUnknownKeys = true
    explicitNulls = false
}

/** HTTP client for the `/api` routes. The engine is platform-specific (OkHttp/Darwin), or a MockEngine in tests. */
fun createNoturaHttpClient(engine: HttpClientEngine): HttpClient = HttpClient(engine) {
    expectSuccess = false
    install(ContentNegotiation) {
        json(NoturaJson)
    }
}
