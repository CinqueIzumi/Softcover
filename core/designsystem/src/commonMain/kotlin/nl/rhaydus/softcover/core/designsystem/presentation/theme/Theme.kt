package nl.rhaydus.softcover.core.designsystem.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import nl.rhaydus.designsystem.editorial.EditorialTheme
import nl.rhaydus.designsystem.haptics.LocalHaptics
import nl.rhaydus.designsystem.haptics.rememberHaptics
import nl.rhaydus.designsystem.motion.playDecorativeMotion
import nl.rhaydus.designsystem.theme.RhaydusTheme

/**
 * The light half of one [SpinePalette]: the palette's three accent families over the palette's own
 * paper. Every slot but the scrim comes from the palette, which is why swapping one repaints the
 * page and not only its accents — see [NeutralFamily] for the tone-to-slot map.
 */
private fun lightSchemeFor(palette: PaletteColors) = lightColorScheme(
    primary = palette.primary.tone40,
    onPrimary = Color.White,
    primaryContainer = palette.primary.tone90,
    onPrimaryContainer = palette.primary.tone30,
    secondary = palette.secondary.tone40,
    onSecondary = Color.White,
    secondaryContainer = palette.secondary.tone90,
    onSecondaryContainer = palette.secondary.tone30,
    tertiary = palette.tertiary.tone40,
    onTertiary = Color.White,
    tertiaryContainer = palette.tertiary.tone90,
    onTertiaryContainer = palette.tertiary.tone30,
    inversePrimary = palette.primary.tone80,
    error = palette.error.tone40,
    onError = palette.neutral.tone100,
    errorContainer = palette.error.tone86,
    onErrorContainer = palette.error.tone30,
    scrim = scrimLight,
    background = palette.neutral.tone98,
    onBackground = palette.neutral.tone10,
    surface = palette.neutral.tone98,
    onSurface = palette.neutral.tone10,
    surfaceVariant = palette.neutralVariant.tone90,
    onSurfaceVariant = palette.neutralVariant.tone30,
    outline = palette.neutralVariant.tone50,
    outlineVariant = palette.neutralVariant.tone80,
    inverseSurface = palette.neutral.tone20,
    inverseOnSurface = palette.neutral.tone95,
    surfaceDim = palette.neutral.tone87,
    surfaceBright = palette.neutral.tone98,
    surfaceContainerLowest = palette.neutral.tone100,
    surfaceContainerLow = palette.neutral.tone96,
    surfaceContainer = palette.neutral.tone94,
    surfaceContainerHigh = palette.neutral.tone92,
    surfaceContainerHighest = palette.neutral.tone90,
)

/** The dark half of [lightSchemeFor] — same palette, every ramp stepped the other way. */
private fun darkSchemeFor(palette: PaletteColors) = darkColorScheme(
    primary = palette.primary.tone80,
    onPrimary = palette.primary.tone20,
    primaryContainer = palette.primary.tone30,
    onPrimaryContainer = palette.primary.tone90,
    secondary = palette.secondary.tone80,
    onSecondary = palette.secondary.tone20,
    secondaryContainer = palette.secondary.tone30,
    onSecondaryContainer = palette.secondary.tone90,
    tertiary = palette.tertiary.tone80,
    onTertiary = palette.tertiary.tone20,
    tertiaryContainer = palette.tertiary.tone30,
    onTertiaryContainer = palette.tertiary.tone90,
    inversePrimary = palette.primary.tone40,
    error = palette.error.tone70,
    onError = palette.error.tone20,
    errorContainer = palette.error.tone30,
    onErrorContainer = palette.error.tone86,
    scrim = scrimDark,
    background = palette.neutral.tone6,
    onBackground = palette.neutral.tone90,
    surface = palette.neutral.tone6,
    onSurface = palette.neutral.tone90,
    surfaceVariant = palette.neutralVariant.tone30,
    onSurfaceVariant = palette.neutralVariant.tone80,
    outline = palette.neutralVariant.tone60,
    outlineVariant = palette.neutralVariant.tone30,
    inverseSurface = palette.neutral.tone90,
    inverseOnSurface = palette.neutral.tone20,
    surfaceDim = palette.neutral.tone6,
    surfaceBright = palette.neutral.tone24,
    surfaceContainerLowest = palette.neutral.tone4,
    surfaceContainerLow = palette.neutral.tone10,
    surfaceContainer = palette.neutral.tone12,
    surfaceContainerHigh = palette.neutral.tone17,
    surfaceContainerHighest = palette.neutral.tone22,
)

// Every scheme is built once, at class-init, rather than per call: the schemes are read during
// composition (by the theme itself and by the Appearance preview tiles), and a freshly-built
// ColorScheme each time would hand the theme a new identity on every recomposition.
private val lightSchemes: Map<SpinePalette, ColorScheme> = SpinePalette.entries
    .associateWith { lightSchemeFor(palette = it.colors) }

private val darkSchemes: Map<SpinePalette, ColorScheme> = SpinePalette.entries
    .associateWith { darkSchemeFor(palette = it.colors) }

private val dimScheme: ColorScheme = darkColorScheme(
    primary = dimPrimary,
    onPrimary = dimOnPrimary,
    primaryContainer = dimPrimaryContainer,
    onPrimaryContainer = dimOnPrimaryContainer,
    secondary = dimSecondary,
    onSecondary = dimOnSecondary,
    secondaryContainer = dimSecondaryContainer,
    onSecondaryContainer = dimOnSecondaryContainer,
    tertiary = dimTertiary,
    onTertiary = dimOnTertiary,
    tertiaryContainer = dimTertiaryContainer,
    onTertiaryContainer = dimOnTertiaryContainer,
    inversePrimary = dimInversePrimary,
    error = dimError,
    onError = dimOnError,
    errorContainer = dimErrorContainer,
    onErrorContainer = dimOnErrorContainer,
    background = dimBackground,
    onBackground = dimOnBackground,
    surface = dimSurface,
    onSurface = dimOnSurface,
    surfaceVariant = dimSurfaceVariant,
    onSurfaceVariant = dimOnSurfaceVariant,
    outline = dimOutline,
    outlineVariant = dimOutlineVariant,
    inverseSurface = dimInverseSurface,
    inverseOnSurface = dimInverseOnSurface,
    surfaceDim = dimSurfaceDim,
    surfaceBright = dimSurfaceBright,
    surfaceContainerLowest = dimSurfaceContainerLowest,
    surfaceContainerLow = dimSurfaceContainerLow,
    surfaceContainer = dimSurfaceContainer,
    surfaceContainerHigh = dimSurfaceContainerHigh,
    surfaceContainerHighest = dimSurfaceContainerHighest,
    scrim = scrimDark,
)

/**
 * The brand color scheme for one [brightness] in one [palette], independent of what the app is
 * currently painting in. [SoftcoverTheme] resolves its own scheme through this, and the Appearance
 * screen's preview tiles paint *other* palettes and *the other* brightness through it — which is why
 * the schemes are reachable from outside this file rather than being read off `MaterialTheme` at
 * the call site (a tile has to show the look the reader has *not* picked). [SchemeBrightness.DIM]
 * ignores [palette].
 */
fun softcoverColorScheme(
    brightness: SchemeBrightness,
    palette: SpinePalette = SpinePalette.DEFAULT,
): ColorScheme = when (brightness) {
    SchemeBrightness.LIGHT -> lightSchemes.getValue(palette)
    SchemeBrightness.DARK -> darkSchemes.getValue(palette)
    SchemeBrightness.DIM -> dimScheme
}

/**
 * The app's theme, resolved from a [palette] token and an already-decided [brightness].
 *
 * "Follow the device" is a *reader preference*, not a token: resolving it needs the persisted
 * setting, so it belongs one layer up (`:core:uibinding`'s `ThemeMode.toSchemeBrightness`).
 * Defaulting to the host's light or dark keeps a bare `SoftcoverTheme { }` — the shape every
 * `@Preview` uses — painting the way the host does. [SchemeBrightness.DIM] ignores both [palette]
 * and [dynamicColor].
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SoftcoverTheme(
    palette: SpinePalette = SpinePalette.DEFAULT,
    brightness: SchemeBrightness = if (isSystemInDarkTheme()) {
        SchemeBrightness.DARK
    } else {
        SchemeBrightness.LIGHT
    },
    // Dynamic color is available on Android 12+; iOS has no equivalent and falls back to the brand scheme.
    // While it is on it *replaces* the chosen palette outright — the wallpaper's own scheme is the whole
    // point of it — which is why picking a palette turns it back off (SetColorPaletteUseCase).
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val dynamicScheme = if (brightness == SchemeBrightness.DIM) {
        null
    } else {
        dynamicColorSchemeOrNull(
            useDynamicColor = dynamicColor,
            darkTheme = brightness.isDark,
        )
    }
    val colorScheme = dynamicScheme
        ?: softcoverColorScheme(
            brightness = brightness,
            palette = palette,
        )

    // EditorialTheme (designsystem-editorial) nests inside RhaydusTheme so that foundation editorial
    // components resolve the shared 9-role contract; Softcover's richer 15-role scale is provided in
    // parallel so app-specific screens that read editorialTypography directly are unaffected.
    val playMotion = playDecorativeMotion()

    RhaydusTheme(
        colorScheme = colorScheme,
        typography = appTypography(),
        motionScheme = if (playMotion) MotionScheme.expressive() else ReducedMotionScheme,
    ) {
        EditorialTheme(editorialTypography = softcoverFoundationEditorialTypography()) {
            CompositionLocalProvider(
                LocalEditorialTypography provides defaultEditorialTypography(),
                LocalSchemeBrightness provides brightness,
                LocalHaptics provides rememberHaptics(),
                LocalMotion provides if (playMotion) Motion.Full else Motion.Reduced,
                content = content,
            )
        }
    }
}

/**
 * The platform's Material You / dynamic color scheme, or `null` when dynamic color is unavailable or
 * unrequested (Android < 12, [useDynamicColor] off, or iOS — which has no dynamic-color equivalent).
 * Callers fall back to the brand [softcoverColorScheme].
 */
@Composable
expect fun dynamicColorSchemeOrNull(
    useDynamicColor: Boolean,
    darkTheme: Boolean,
): ColorScheme?
