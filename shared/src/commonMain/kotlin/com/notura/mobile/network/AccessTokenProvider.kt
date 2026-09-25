package com.notura.mobile.network

/** Source of the Supabase access token sent as `Authorization: Bearer` to the `/api` routes. */
interface AccessTokenProvider {
    suspend fun currentAccessToken(): String?

    /** Refreshes the session and returns the new token, or null when the session cannot be renewed. */
    suspend fun refreshAccessToken(): String?
}
