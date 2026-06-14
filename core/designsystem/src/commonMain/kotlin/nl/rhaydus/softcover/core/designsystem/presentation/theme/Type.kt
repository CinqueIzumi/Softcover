package nl.rhaydus.softcover.core.designsystem.presentation.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import org.jetbrains.compose.resources.Font
import nl.rhaydus.softcover.core.designsystem.generated.resources.Res
import nl.rhaydus.softcover.core.designsystem.generated.resources.fraunces_bold
import nl.rhaydus.softcover.core.designsystem.generated.resources.fraunces_bold_italic
import nl.rhaydus.softcover.core.designsystem.generated.resources.fraunces_italic
import nl.rhaydus.softcover.core.designsystem.generated.resources.fraunces_medium
import nl.rhaydus.softcover.core.designsystem.generated.resources.fraunces_medium_italic
import nl.rhaydus.softcover.core.designsystem.generated.resources.fraunces_regular
import nl.rhaydus.softcover.core.designsystem.generated.resources.fraunces_semibold
import nl.rhaydus.softcover.core.designsystem.generated.resources.fraunces_semibold_italic
import nl.rhaydus.softcover.core.designsystem.generated.resources.inter_bold
import nl.rhaydus.softcover.core.designsystem.generated.resources.inter_bold_italic
import nl.rhaydus.softcover.core.designsystem.generated.resources.inter_italic
import nl.rhaydus.softcover.core.designsystem.generated.resources.inter_medium
import nl.rhaydus.softcover.core.designsystem.generated.resources.inter_medium_italic
import nl.rhaydus.softcover.core.designsystem.generated.resources.inter_regular
import nl.rhaydus.softcover.core.designsystem.generated.resources.inter_semibold
import nl.rhaydus.softcover.core.designsystem.generated.resources.inter_semibold_italic

/**
 * The editorial display family (Fraunces) and the body family (Inter), bundled as Compose
 * Multiplatform resources so every platform renders the same branded type. Because the CMP resource
 * [Font] loader is `@Composable`, these — and the typography built from them — are composable rather
 * than top-level values; call them from within a theme/composition. The built `FontFamily` is
 * `remember`ed so repeated calls (e.g. from leaf composables) don't re-allocate it on every recompose.
 */
@Composable
fun displayFontFamily(): FontFamily {
    val fonts = listOf(
        Font(
            Res.font.fraunces_regular,
            FontWeight.Normal,
            FontStyle.Normal,
        ),
        Font(
            Res.font.fraunces_italic,
            FontWeight.Normal,
            FontStyle.Italic,
        ),
        Font(
            Res.font.fraunces_medium,
            FontWeight.Medium,
            FontStyle.Normal,
        ),
        Font(
            Res.font.fraunces_medium_italic,
            FontWeight.Medium,
            FontStyle.Italic,
        ),
        Font(
            Res.font.fraunces_semibold,
            FontWeight.SemiBold,
            FontStyle.Normal,
        ),
        Font(
            Res.font.fraunces_semibold_italic,
            FontWeight.SemiBold,
            FontStyle.Italic,
        ),
        Font(
            Res.font.fraunces_bold,
            FontWeight.Bold,
            FontStyle.Normal,
        ),
        Font(
            Res.font.fraunces_bold_italic,
            FontWeight.Bold,
            FontStyle.Italic,
        ),
    )

    return remember(fonts) { FontFamily(fonts) }
}

@Composable
fun bodyFontFamily(): FontFamily {
    val fonts = listOf(
        Font(
            Res.font.inter_regular,
            FontWeight.Normal,
            FontStyle.Normal,
        ),
        Font(
            Res.font.inter_italic,
            FontWeight.Normal,
            FontStyle.Italic,
        ),
        Font(
            Res.font.inter_medium,
            FontWeight.Medium,
            FontStyle.Normal,
        ),
        Font(
            Res.font.inter_medium_italic,
            FontWeight.Medium,
            FontStyle.Italic,
        ),
        Font(
            Res.font.inter_semibold,
            FontWeight.SemiBold,
            FontStyle.Normal,
        ),
        Font(
            Res.font.inter_semibold_italic,
            FontWeight.SemiBold,
            FontStyle.Italic,
        ),
        Font(
            Res.font.inter_bold,
            FontWeight.Bold,
            FontStyle.Normal,
        ),
        Font(
            Res.font.inter_bold_italic,
            FontWeight.Bold,
            FontStyle.Italic,
        ),
    )

    return remember(fonts) { FontFamily(fonts) }
}

/**
 * The app's Material 3 typography — the baseline scale with the display family on the large/headline
 * roles and the body family on the rest. Composable because the families resolve CMP font resources.
 */
@Composable
internal fun appTypography(): Typography {
    val display = displayFontFamily()
    val body = bodyFontFamily()
    val baseline = Typography()

    return Typography(
        displayLarge = baseline.displayLarge.copy(fontFamily = display),
        displayMedium = baseline.displayMedium.copy(fontFamily = display),
        displaySmall = baseline.displaySmall.copy(fontFamily = display),
        headlineLarge = baseline.headlineLarge.copy(fontFamily = display),
        headlineMedium = baseline.headlineMedium.copy(fontFamily = display),
        headlineSmall = baseline.headlineSmall.copy(fontFamily = display),
        titleLarge = baseline.titleLarge.copy(fontFamily = body),
        titleMedium = baseline.titleMedium.copy(fontFamily = body),
        titleSmall = baseline.titleSmall.copy(fontFamily = body),
        bodyLarge = baseline.bodyLarge.copy(fontFamily = body),
        bodyMedium = baseline.bodyMedium.copy(fontFamily = body),
        bodySmall = baseline.bodySmall.copy(fontFamily = body),
        labelLarge = baseline.labelLarge.copy(fontFamily = body),
        labelMedium = baseline.labelMedium.copy(fontFamily = body),
        labelSmall = baseline.labelSmall.copy(fontFamily = body),
    )
}
