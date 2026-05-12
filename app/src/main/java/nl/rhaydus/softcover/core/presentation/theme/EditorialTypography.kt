package nl.rhaydus.softcover.core.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private const val TABULAR_NUMS = "tnum"

@Immutable
data class EditorialTypography(
    val eyebrow: TextStyle,
    val eyebrowSmall: TextStyle,

    val pageTitle: TextStyle,
    val display: TextStyle,
    val headlineMedium: TextStyle,
    val headlineSmall: TextStyle,

    val titleLarge: TextStyle,
    val titleMedium: TextStyle,
    val titleSmall: TextStyle,

    val bodyLarge: TextStyle,
    val body: TextStyle,
    val bodySmall: TextStyle,

    val statHero: TextStyle,
    val statLarge: TextStyle,

    val quoteGlyph: TextStyle,
)

val DefaultEditorialTypography: EditorialTypography = EditorialTypography(
    eyebrow = AppTypography.labelMedium.copy(
        letterSpacing = 2.5.sp,
        fontWeight = FontWeight.SemiBold,
    ),
    eyebrowSmall = AppTypography.labelSmall.copy(
        letterSpacing = 1.2.sp,
        fontWeight = FontWeight.SemiBold,
    ),

    pageTitle = AppTypography.displaySmall.copy(
        fontWeight = FontWeight.SemiBold,
        fontSize = 30.sp,
        lineHeight = 36.sp,
    ),
    display = AppTypography.displayMedium.copy(
        fontStyle = FontStyle.Italic,
        fontWeight = FontWeight.SemiBold,
        lineHeight = 44.sp,
    ),
    headlineMedium = AppTypography.headlineMedium.copy(
        fontStyle = FontStyle.Italic,
        fontWeight = FontWeight.SemiBold,
    ),
    headlineSmall = AppTypography.headlineSmall.copy(
        fontStyle = FontStyle.Italic,
        fontWeight = FontWeight.SemiBold,
    ),

    titleLarge = AppTypography.titleLarge.copy(
        fontWeight = FontWeight.SemiBold,
    ),
    titleMedium = AppTypography.titleMedium.copy(
        fontWeight = FontWeight.SemiBold,
    ),
    titleSmall = AppTypography.titleSmall.copy(
        fontWeight = FontWeight.SemiBold,
    ),

    bodyLarge = AppTypography.bodyLarge.copy(
        fontFamily = displayFontFamily,
        fontStyle = FontStyle.Italic,
        lineHeight = 24.sp,
    ),
    body = AppTypography.bodyMedium.copy(
        fontFamily = displayFontFamily,
        fontStyle = FontStyle.Italic,
    ),
    bodySmall = AppTypography.bodySmall.copy(
        fontFamily = displayFontFamily,
        fontStyle = FontStyle.Italic,
    ),

    statHero = AppTypography.displayLarge.copy(
        fontStyle = FontStyle.Italic,
        fontWeight = FontWeight.SemiBold,
        fontSize = 72.sp,
        lineHeight = 76.sp,
        fontFeatureSettings = TABULAR_NUMS,
    ),
    statLarge = AppTypography.displayMedium.copy(
        fontStyle = FontStyle.Italic,
        fontWeight = FontWeight.SemiBold,
        lineHeight = 52.sp,
        fontFeatureSettings = TABULAR_NUMS,
    ),

    quoteGlyph = AppTypography.displayLarge.copy(
        fontSize = 120.sp,
    ),
)

val LocalEditorialTypography = staticCompositionLocalOf { DefaultEditorialTypography }

val MaterialTheme.editorialTypography: EditorialTypography
    @Composable
    @ReadOnlyComposable
    get() = LocalEditorialTypography.current
