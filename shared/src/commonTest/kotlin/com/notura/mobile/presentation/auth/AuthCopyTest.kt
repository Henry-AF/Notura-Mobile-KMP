package com.notura.mobile.presentation.auth

import com.notura.mobile.domain.auth.AuthFailure
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AuthCopyTest {

    @Test
    fun supabaseErrorsShowTheServerMessageLikeTheWeb() {
        assertEquals(
            "Invalid login credentials",
            AuthCopy.authFailureMessage(AuthFailure.InvalidCredentials("Invalid login credentials"), AuthAction.Email),
        )
        assertEquals(
            "Password should be at least 8 characters.",
            AuthCopy.authFailureMessage(
                AuthFailure.WeakPassword(listOf("length"), "Password should be at least 8 characters."),
                AuthAction.Email,
            ),
        )
    }

    @Test
    fun networkFailureOnEmailShowsTheWebFallback() {
        assertEquals(
            "Ocorreu um erro inesperado. Tente novamente.",
            AuthCopy.authFailureMessage(AuthFailure.Network, AuthAction.Email),
        )
    }

    @Test
    fun networkFailureOnGoogleShowsTheWebGoogleFallback() {
        assertEquals(
            "Não foi possível conectar com o Google. Tente novamente.",
            AuthCopy.authFailureMessage(AuthFailure.Network, AuthAction.Google),
        )
    }

    @Test
    fun blankServerMessageFallsBack() {
        assertEquals(AuthCopy.UNEXPECTED_ERROR, AuthCopy.authFailureMessage(AuthFailure.Unknown(" "), AuthAction.Email))
    }

    @Test
    fun onlyTermsErrorsHaveFieldCopy() {
        assertEquals("Você precisa aceitar os Termos de Uso e Privacidade.", AuthCopy.fieldErrorMessage(FieldError.TermsNotAccepted))
        assertEquals(
            "Você precisa aceitar os Termos de Uso e Privacidade para continuar.",
            AuthCopy.fieldErrorMessage(FieldError.TermsNotAcceptedForGoogle),
        )
        listOf(
            FieldError.NameRequired,
            FieldError.EmailRequired,
            FieldError.EmailInvalid,
            FieldError.PasswordRequired,
            FieldError.PasswordTooShort,
        ).forEach { assertNull(AuthCopy.fieldErrorMessage(it), it.name) }
    }
}
