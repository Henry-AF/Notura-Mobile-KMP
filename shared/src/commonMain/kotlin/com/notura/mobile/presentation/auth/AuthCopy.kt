package com.notura.mobile.presentation.auth

import com.notura.mobile.domain.auth.AuthFailure

/**
 * Auth screen copy. Every string comes from the Figma file (node noted) or from the web auth
 * pages in Notura-App `src/app/(auth)`. States with no existing copy are listed as gaps in
 * PROGRESS.md and return null here instead of inventing text.
 */
object AuthCopy {
    // Intro — Figma "intro" 2270:148.
    const val INTRO_TITLE = "Transforme reuniões em decisões acionáveis"

    /** The Figma body is Lorem ipsum; this is the web login page's text under the same title. */
    const val INTRO_BODY =
        "Notura organiza tudo para você com IA, mantendo contexto, tarefas e próximos passos sempre centralizados."
    const val INTRO_CTA = "Let's get started"

    // Shared by sign in and sign up — Figma 2270:160 / 2270:286.
    const val BRAND = "Notura"
    const val OR = "ou"
    const val GOOGLE = "Continue com o Google"
    const val TERMS = "Termos de Uso"
    const val PRIVACY = "Privacidade"

    // Sign in — Figma 2270:160.
    const val SIGN_IN_TITLE = "Bem-vindo de volta!"
    const val SIGN_IN_SUBTITLE = "Entre na sua conta para continuar organizando suas reuniões"
    const val EMAIL_PLACEHOLDER = "Email"
    const val PASSWORD_PLACEHOLDER = "Senha"
    const val FORGOT_PASSWORD = "Esqueceu a senha?"
    const val SIGN_IN_BUTTON = "Sign In"
    const val SIGN_UP_PROMPT = "Novo por aqui? Crie um "
    const val SIGN_UP_LINK = "Cadastro"

    // Sign up — Figma 2270:286.
    const val SIGN_UP_TITLE = "Crie sua conta"
    const val SIGN_UP_SUBTITLE = "Comece a organizar reuniões e tarefas com o padrão Notura"
    const val NAME_PLACEHOLDER = "Como podemos chamar você?"
    const val SIGN_UP_EMAIL_PLACEHOLDER = "Informe seu melhor e-mail"
    const val SIGN_UP_PASSWORD_PLACEHOLDER = "Crie sua senha  (mín. 8 caracteres)"
    const val TERMS_PREFIX = "Ao se inscrever, você concorda com nossos "
    const val TERMS_JOIN = " e "
    const val TERMS_SUFFIX = "."
    const val SIGN_UP_BUTTON = "Sign Up"

    // Loading labels — web login/signup buttons.
    const val SIGNING_IN = "Entrando..."
    const val SIGNING_UP = "Criando conta..."
    const val REDIRECTING_TO_GOOGLE = "Redirecionando..."

    // Errors — web login/signup pages.
    const val TERMS_REQUIRED = "Você precisa aceitar os Termos de Uso e Privacidade."
    const val TERMS_REQUIRED_FOR_GOOGLE = "Você precisa aceitar os Termos de Uso e Privacidade para continuar."
    const val UNEXPECTED_ERROR = "Ocorreu um erro inesperado. Tente novamente."
    const val GOOGLE_CONNECTION_ERROR = "Não foi possível conectar com o Google. Tente novamente."

    /**
     * Text shown under a field. Only the terms checkbox has copy on the web; the other fields rely
     * on the browser's native validation there, so they have no text yet (gap).
     */
    fun fieldErrorMessage(error: FieldError): String? = when (error) {
        FieldError.TermsNotAccepted -> TERMS_REQUIRED
        FieldError.TermsNotAcceptedForGoogle -> TERMS_REQUIRED_FOR_GOOGLE
        FieldError.NameRequired,
        FieldError.EmailRequired,
        FieldError.EmailInvalid,
        FieldError.PasswordRequired,
        FieldError.PasswordTooShort,
        -> null
    }

    /**
     * Mirrors the web: Supabase errors show Supabase's own message; failures without a server
     * response show the web's fallback for the button that was pressed.
     */
    fun authFailureMessage(failure: AuthFailure, action: AuthAction?): String =
        failure.serverMessage?.takeIf { it.isNotBlank() } ?: when (action) {
            AuthAction.Google -> GOOGLE_CONNECTION_ERROR
            AuthAction.Email, null -> UNEXPECTED_ERROR
        }
}
