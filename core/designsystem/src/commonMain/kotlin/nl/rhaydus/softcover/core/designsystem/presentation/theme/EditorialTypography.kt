package nl.rhaydus.softcover.core.designsystem.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import nl.rhaydus.designsystem.editorial.EditorialTypography as FoundationEditorialTypography

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

/**
 * Derives the editorial type roles from a base Material [typography] and the [displayFamily] used for
 * the italic editorial body voice. Pure (non-composable) so it can build both the branded scale (from
 * [appTypography] + [displayFontFamily]) and the non-branded fallback below.
 */
internal fun buildEditorialTypography(
    typography: Typography,
    displayFamily: FontFamily,
): EditorialTypography = EditorialTypography(
    eyebrow = typography.labelMedium.copy(
        letterSpacing = 2.5.sp,
        fontWeight = FontWeight.SemiBold,
    ),
    eyebrowSmall = typography.labelSmall.copy(
        letterSpacing = 1.2.sp,
        fontWeight = FontWeight.SemiBold,
    ),

    pageTitle = typography.displaySmall.copy(
        fontWeight = FontWeight.SemiBold,
        fontSize = 30.sp,
        lineHeight = 36.sp,
    ),
    display = typography.displayMedium.copy(
        fontStyle = FontStyle.Italic,
        fontWeight = FontWeight.SemiBold,
        lineHeight = 44.sp,
    ),
    headlineMedium = typography.headlineMedium.copy(
        fontStyle = FontStyle.Italic,
        fontWeight = FontWeight.SemiBold,
    ),
    headlineSmall = typography.headlineSmall.copy(
        fontStyle = FontStyle.Italic,
        fontWeight = FontWeight.SemiBold,
    ),

    titleLarge = typography.titleLarge.copy(
        fontWeight = FontWeight.SemiBold,
    ),
    titleMedium = typography.titleMedium.copy(
        fontWeight = FontWeight.SemiBold,
    ),
    titleSmall = typography.titleSmall.copy(
        fontWeight = FontWeight.SemiBold,
    ),

    bodyLarge = typography.bodyLarge.copy(
        fontFamily = displayFamily,
        fontStyle = FontStyle.Italic,
        lineHeight = 24.sp,
    ),
    body = typography.bodyMedium.copy(
        fontFamily = displayFamily,
        fontStyle = FontStyle.Italic,
    ),
    bodySmall = typography.bodySmall.copy(
        fontFamily = displayFamily,
        fontStyle = FontStyle.Italic,
    ),

    statHero = typography.displayLarge.copy(
        fontStyle = FontStyle.Italic,
        fontWeight = FontWeight.SemiBold,
        fontSize = 72.sp,
        lineHeight = 76.sp,
        fontFeatureSettings = TABULAR_NUMS,
    ),
    statLarge = typography.displayMedium.copy(
        fontStyle = FontStyle.Italic,
        fontWeight = FontWeight.SemiBold,
        lineHeight = 52.sp,
        fontFeatureSettings = TABULAR_NUMS,
    ),

    quoteGlyph = typography.displayLarge.copy(
        fontSize = 120.sp,
    ),
)

/**
 * The branded editorial type scale, resolving the bundled font resources. Provided by `SoftcoverTheme`.
 */
@Composable
internal fun defaultEditorialTypography(): EditorialTypography = buildEditorialTypography(
    typography = appTypography(),
    displayFamily = displayFontFamily(),
)

// Non-branded fallback for the composition-local default — used only outside SoftcoverTheme (previews
// / tests), which always provides the branded scale. Built from the system family so it needs no
// composable font resolution.
private val fallbackEditorialTypography: EditorialTypography = buildEditorialTypography(
    typography = Typography(),
    displayFamily = FontFamily.Default,
)

internal val LocalEditorialTypography = staticCompositionLocalOf { fallbackEditorialTypography }

val MaterialTheme.editorialTypography: EditorialTypography
    @Composable
    @ReadOnlyComposable
    get() = LocalEditorialTypography.current

/**
 * Maps Softcover's 15-role [EditorialTypography] onto the 9-role
 * [nl.rhaydus.designsystem.editorial.EditorialTypography] consumed by the foundation editorial
 * components. Wired once in [SoftcoverTheme] via [nl.rhaydus.designsystem.editorial.EditorialTheme];
 * the richer Softcover scale remains separately available through [MaterialTheme.editorialTypography].
 *
 * Role mapping:
 * - foundation.eyebrow       ← Softcover.eyebrow
 * - foundation.eyebrowSmall  ← Softcover.eyebrowSmall
 * - foundation.pageTitle     ← Softcover.pageTitle
 * - foundation.headline      ← Softcover.headlineMedium  (what EditorialSectionHeader rendered before)
 * - foundation.title         ← Softcover.titleLarge
 * - foundation.body          ← Softcover.body
 * - foundation.bodySmall     ← Softcover.bodySmall
 * - foundation.statLarge     ← Softcover.statLarge
 * - foundation.statHero      ← Softcover.statHero
 */
@Composable
internal fun softcoverFoundationEditorialTypography(): FoundationEditorialTypography {
    val sc = defaultEditorialTypography()

    return FoundationEditorialTypography(
        eyebrow = sc.eyebrow,
        eyebrowSmall = sc.eyebrowSmall,
        pageTitle = sc.pageTitle,
        headline = sc.headlineMedium,
        title = sc.titleLarge,
        body = sc.body,
        bodySmall = sc.bodySmall,
        statLarge = sc.statLarge,
        statHero = sc.statHero,
    )
}
