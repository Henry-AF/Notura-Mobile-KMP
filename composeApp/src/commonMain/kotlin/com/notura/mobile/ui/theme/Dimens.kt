package com.notura.mobile.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Corner radii from the Figma auth frames. */
@Immutable
data class NoturaShapes(
    /** Text fields (2270:182). */
    val field: RoundedCornerShape = RoundedCornerShape(5.dp),
    /** Primary and Google buttons (2270:191, 2270:197). */
    val button: RoundedCornerShape = RoundedCornerShape(7.dp),
)

/** Spacing from the Figma auth frames (node noted where the value comes from). */
@Immutable
data class NoturaSpacing(
    /** Horizontal screen padding ("main" 2270:163). */
    val screenHorizontal: Dp = 23.dp,
    /** Top padding under the status bar ("main" 2270:163). */
    val screenTop: Dp = 20.dp,
    /** Bottom padding on sign in / sign up ("main" 2270:163 / 2270:289). */
    val signInBottom: Dp = 13.dp,
    val signUpBottom: Dp = 40.dp,
    /** Logo icon to "Notura" (2270:170). */
    val logoGap: Dp = 12.dp,
    /** Title to subtitle (2270:178). */
    val titleToSubtitle: Dp = 18.dp,
    /** Logo to titles: sign in 46, sign up 40 (2270:169 / 2270:295). */
    val signInLogoToTitle: Dp = 46.dp,
    val signUpLogoToTitle: Dp = 40.dp,
    /** Titles to fields: sign in 47, sign up 45 (2270:168 / 2270:294). */
    val signInHeaderToFields: Dp = 47.dp,
    val signUpHeaderToFields: Dp = 45.dp,
    /** Between fields (2270:181). */
    val fieldGap: Dp = 23.dp,
    /** Password field to "Esqueceu a senha?" (2270:184). */
    val passwordToForgot: Dp = 15.dp,
    /** Fields to terms row on sign up (2270:307). */
    val fieldsToTerms: Dp = 20.dp,
    /** Form to primary button (2270:167). */
    val formToButton: Dp = 31.dp,
    /** Between primary button, "ou" and Google button (2270:166). */
    val buttonGroupGap: Dp = 23.dp,
    /** Button group to "Novo por aqui?" (2270:165). */
    val buttonsToPrompt: Dp = 40.dp,
    /** Field inner padding (2270:182). */
    val fieldHorizontalPadding: Dp = 22.dp,
    val fieldVerticalPadding: Dp = 18.dp,
    /** Button vertical padding (2270:191). */
    val buttonVerticalPadding: Dp = 17.dp,
    /** Google icon to label (2270:198). */
    val googleIconGap: Dp = 7.dp,
    /** Divider line width each side of "ou" (2270:194). */
    val dividerLineWidth: Dp = 138.dp,
    /** Sign-in welcome block width (2270:169) and sign-up subtitle width (2270:306). */
    val signInHeaderWidth: Dp = 287.dp,
    val signUpSubtitleWidth: Dp = 311.dp,
    /** Terms label width (2270:321). */
    val termsLabelWidth: Dp = 320.dp,
    /** Terms checkbox diameter (2270:320). */
    val checkboxSize: Dp = 15.dp,
    /** Sign-up primary button height (2270:322). */
    val signUpButtonHeight: Dp = 47.dp,
    /** Logo icon (2270:171) and eye icon (2270:187) sizes. */
    val logoIconWidth: Dp = 42.dp,
    val logoIconHeight: Dp = 45.dp,
    val fieldIconWidth: Dp = 21.dp,
    /** Google icon (2270:199). */
    val googleIconSize: Dp = 16.dp,
    /** Back chevron (2270:164). */
    val backIconWidth: Dp = 8.dp,
    val backIconHeight: Dp = 16.dp,
)
