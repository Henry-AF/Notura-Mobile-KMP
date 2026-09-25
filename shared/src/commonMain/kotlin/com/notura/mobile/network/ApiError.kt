package com.notura.mobile.network

/**
 * Errors documented in `docs/api-codex-mobile.md` (Notura-App), section 2.
 * [message] is the server's `error` field when present, which is user-facing Portuguese copy.
 */
sealed interface ApiError {
    val message: String?

    /** 401: session missing or expired, even after one token refresh. */
    data class Unauthorized(override val message: String?) : ApiError

    /** 403: ownership, plan or quota. [code] and [quotaLimit] are set by the routes that send them. */
    data class Forbidden(
        override val message: String?,
        val code: String? = null,
        val quotaLimit: Int? = null,
    ) : ApiError

    data class NotFound(override val message: String?) : ApiError

    data class Conflict(override val message: String?) : ApiError

    /** 429: [retryAfterSeconds] comes from `Retry-After`. [code] is `rate_limited` for route rate limits. */
    data class RateLimited(
        override val message: String?,
        val retryAfterSeconds: Long?,
        val code: String? = null,
    ) : ApiError

    /** 400, 413, 415 and 422: request rejected by validation. */
    data class InvalidRequest(val status: Int, override val message: String?) : ApiError

    /** 5xx. */
    data class Server(val status: Int, override val message: String?) : ApiError

    /** The request never produced an HTTP response (offline, DNS, timeout). */
    data class Network(val cause: Throwable) : ApiError {
        override val message: String? get() = cause.message
    }

    /** A status the contract does not document, or a success body that does not match it. */
    data class Unexpected(val status: Int?, override val message: String?) : ApiError
}
