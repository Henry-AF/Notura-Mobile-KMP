package com.notura.mobile.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.notura.mobile.resources.Res
import com.notura.mobile.resources.plus_jakarta_sans
import com.notura.mobile.resources.poppins_regular
import com.notura.mobile.resources.poppins_semibold
import org.jetbrains.compose.resources.Font

/** Text styles used by the Figma auth frames (sizes in the design's px, mapped 1:1 to sp). */
@Immutable
data class NoturaTypography(
    /** Plus Jakarta Sans Bold 24 — "Notura" logo and screen titles. */
    val titleLarge: TextStyle,
    /** Plus Jakarta Sans Regular 16 — screen subtitles. */
    val subtitle: TextStyle,
    /** Plus Jakarta Sans Regular 14 — the "ou" divider label. */
    val divider: TextStyle,
    /** Plus Jakarta Sans Regular 12 — text field content and placeholder. */
    val field: TextStyle,
    /** Plus Jakarta Sans Regular 11 — "Esqueceu a senha?". */
    val smallLink: TextStyle,
    /** Plus Jakarta Sans SemiBold 12 — primary button on sign in. */
    val buttonSmall: TextStyle,
    /** Plus Jakarta Sans SemiBold 14 — primary button on sign up. */
    val button: TextStyle,
    /** Plus Jakarta Sans Bold 12 — Google button. */
    val buttonBold: TextStyle,
    /** Poppins Regular 13 — sign-up prompt and legal footer. */
    val footer: TextStyle,
    /** Poppins Regular 12 — terms checkbox label. */
    val terms: TextStyle,
    /** Poppins SemiBold — emphasized spans inside [footer] and [terms]. */
    val emphasisFamily: FontFamily,
    /** Not in Figma: web error text is `text-sm` (14px). */
    val error: TextStyle,
)

@Composable
internal fun noturaTypography(): NoturaTypography {
    val jakarta = FontFamily(
        Font(Res.font.plus_jakarta_sans, FontWeight.Normal),
        Font(Res.font.plus_jakarta_sans, FontWeight.SemiBold),
        Font(Res.font.plus_jakarta_sans, FontWeight.Bold),
    )
    val poppins = FontFamily(
        Font(Res.font.poppins_regular, FontWeight.Normal),
        Font(Res.font.poppins_semibold, FontWeight.SemiBold),
    )
    val poppinsSemiBold = FontFamily(Font(Res.font.poppins_semibold, FontWeight.SemiBold))
    return NoturaTypography(
        titleLarge = TextStyle(fontFamily = jakarta, fontWeight = FontWeight.Bold, fontSize = 24.sp),
        subtitle = TextStyle(fontFamily = jakarta, fontWeight = FontWeight.Normal, fontSize = 16.sp),
        divider = TextStyle(fontFamily = jakarta, fontWeight = FontWeight.Normal, fontSize = 14.sp),
        field = TextStyle(fontFamily = jakarta, fontWeight = FontWeight.Normal, fontSize = 12.sp),
        smallLink = TextStyle(fontFamily = jakarta, fontWeight = FontWeight.Normal, fontSize = 11.sp),
        buttonSmall = TextStyle(fontFamily = jakarta, fontWeight = FontWeight.SemiBold, fontSize = 12.sp),
        button = TextStyle(fontFamily = jakarta, fontWeight = FontWeight.SemiBold, fontSize = 14.sp),
        buttonBold = TextStyle(fontFamily = jakarta, fontWeight = FontWeight.Bold, fontSize = 12.sp),
        footer = TextStyle(fontFamily = poppins, fontWeight = FontWeight.Normal, fontSize = 13.sp),
        terms = TextStyle(fontFamily = poppins, fontWeight = FontWeight.Normal, fontSize = 12.sp),
        emphasisFamily = poppinsSemiBold,
        error = TextStyle(fontFamily = jakarta, fontWeight = FontWeight.Normal, fontSize = 14.sp),
    )
}
