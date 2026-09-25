package com.notura.mobile.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/**
 * Colors from the Figma file. Names in quotes are Figma variables; the rest are fixed values
 * read from the frames (node noted), since the file defines only a handful of variables.
 */
@Immutable
data class NoturaColors(
    /** "gradient" — screen background. */
    val background: Color = Color(0xFFF5F4FC),
    /** "notura-main-color" — primary button. */
    val primary: Color = Color(0xFF6656E6),
    val onPrimary: Color = Color(0xFFFFFFFF),
    /** "title-notura" — brand name and emphasized links. */
    val brand: Color = Color(0xFF5A4DC5),
    /** "title" — titles and field text. */
    val title: Color = Color(0xFF3B3B3B),
    /** "subtitle" — subtitles, body and secondary buttons. */
    val subtitle: Color = Color(0xFF474747),
    /** "main-text". */
    val mainText: Color = Color(0xFF525252),
    /** "Esqueceu a senha?" link (2270:190). */
    val mutedLink: Color = Color(0xFF5D5D5D),
    /** Text field border (2270:182). */
    val fieldBorder: Color = Color(0xFFD5D5D5),
    /** Google button border (2270:197). */
    val outlinedButtonBorder: Color = Color(0xFFCFCFCF),
    /** Primary button shadow tint (2270:191). */
    val primaryShadow: Color = Color(0xFFA296FD),
    /** Secondary button shadow tint (2270:197). */
    val neutralShadow: Color = Color(0xFFA3A3A3),
    /** Not in Figma: the web's `destructive` token (rgb 239 68 68) used for auth errors. */
    val error: Color = Color(0xFFEF4444),
)
