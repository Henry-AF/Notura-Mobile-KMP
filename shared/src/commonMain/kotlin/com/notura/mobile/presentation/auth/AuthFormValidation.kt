package com.notura.mobile.presentation.auth

/** Minimum password length shown in the sign-up field placeholder (Figma "sign up", 2270:286). */
const val MIN_PASSWORD_LENGTH = 8

private val EMAIL_PATTERN = Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")

/** Client-side validation errors, checked before any request is sent. */
enum class FieldError {
    NameRequired,
    EmailRequired,
    EmailInvalid,
    PasswordRequired,
    PasswordTooShort,
    TermsNotAccepted,

    /** Same as [TermsNotAccepted], raised by the Google button; the web words it differently. */
    TermsNotAcceptedForGoogle,
}

/** Which button started the request that failed; the web words network failures differently for each. */
enum class AuthAction {
    Email,
    Google,
}

internal fun validateEmail(email: String): FieldError? {
    val trimmed = email.trim()
    return when {
        trimmed.isEmpty() -> FieldError.EmailRequired
        !EMAIL_PATTERN.matches(trimmed) -> FieldError.EmailInvalid
        else -> null
    }
}

internal fun validateSignInPassword(password: String): FieldError? =
    if (password.isEmpty()) FieldError.PasswordRequired else null

internal fun validateNewPassword(password: String): FieldError? = when {
    password.isEmpty() -> FieldError.PasswordRequired
    password.length < MIN_PASSWORD_LENGTH -> FieldError.PasswordTooShort
    else -> null
}

internal fun validateName(name: String): FieldError? =
    if (name.isBlank()) FieldError.NameRequired else null
