package nl.rhaydus.softcover.core.designsystem.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import nl.rhaydus.designsystem.editorial.EditorialTheme
import nl.rhaydus.designsystem.theme.RhaydusTheme
import nl.rhaydus.softcover.core.domain.model.ColorPalette
import nl.rhaydus.softcover.core.domain.model.ThemeMode

/**
 * The light half of one [ColorPalette]: the palette's three accent families over the palette's own
 * paper. Every slot but the error family and the scrim comes from the palette, which is why swapping
 * one repaints the page and not only its accents — see [NeutralFamily] for the tone-to-slot map.
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
    error = errorLight,
    onError = onErrorLight,
    errorContainer = errorContainerLight,
    onErrorContainer = onErrorContainerLight,
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
    error = errorDark,
    onError = onErrorDark,
    errorContainer = errorContainerDark,
    onErrorContainer = onErrorContainerDark,
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

// Both halves of every palette are built once, at class-init, rather than per call: the schemes are
// read during composition (by the theme itself and by all eight Appearance preview tiles), and a
// freshly-built ColorScheme each time would hand the theme a new identity on every recomposition.
private val lightSchemes: Map<ColorPalette, ColorScheme> = ColorPalette.entries
    .associateWith { lightSchemeFor(palette = it.colors) }

private val darkSchemes: Map<ColorPalette, ColorScheme> = ColorPalette.entries
    .associateWith { darkSchemeFor(palette = it.colors) }

/**
 * The brand color scheme for one side of the light/dark pair in one [colorPalette], independent of
 * what the app is currently painting in. [SoftcoverTheme] resolves its own scheme through this, and
 * the Appearance screen's preview tiles paint *other* palettes and *the other* brightness through it
 * — which is why the schemes are reachable from outside this file rather than being read off
 * `MaterialTheme` at the call site (a tile has to show the look the reader has *not* picked).
 */
fun softcoverColorScheme(
    darkTheme: Boolean,
    colorPalette: ColorPalette = ColorPalette.DEFAULT,
): ColorScheme = if (darkTheme) {
    darkSchemes.getValue(colorPalette)
} else {
    lightSchemes.getValue(colorPalette)
}

/**
 * Whether this mode paints dark right now. [ThemeMode.SYSTEM] defers to the platform's own setting
 * and therefore re-resolves whenever the device flips; the two explicit modes ignore it.
 *
 * Read this — or [LocalDarkTheme] once inside the theme — rather than `isSystemInDarkTheme()`: with a
 * forced Light or Dark mode the system's answer is no longer the app's.
 */
@Composable
fun ThemeMode.isDark(): Boolean = when (this) {
    ThemeMode.LIGHT -> false
    ThemeMode.DARK -> true
    ThemeMode.SYSTEM -> isSystemInDarkTheme()
}

@Composable
fun SoftcoverTheme(
    themeMode: ThemeMode = ThemeMode.DEFAULT,
    colorPalette: ColorPalette = ColorPalette.DEFAULT,
    // Dynamic color is available on Android 12+; iOS has no equivalent and falls back to the brand scheme.
    // While it is on it *replaces* the chosen palette outright — the wallpaper's own scheme is the whole
    // point of it — which is why picking a palette turns it back off (SetColorPaletteUseCase).
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val darkTheme = themeMode.isDark()

    val colorScheme = dynamicColorSchemeOrNull(
        useDynamicColor = dynamicColor,
        darkTheme = darkTheme,
    )
        ?: softcoverColorScheme(
            darkTheme = darkTheme,
            colorPalette = colorPalette,
        )

    // Delegates the Material 3 Expressive scaffold to the foundation RhaydusTheme (designsystem-core),
    // supplying Softcover's brand color scheme + typography. The branded editorial scale stays an
    // app concern, provided here via Softcover's own LocalEditorialTypography.
    // EditorialTheme (designsystem-editorial) nests inside RhaydusTheme so that foundation editorial
    // components resolve the shared 9-role contract; Softcover's richer 15-role scale is provided in
    // parallel so app-specific screens that read editorialTypography directly are unaffected.
    RhaydusTheme(
        colorScheme = colorScheme,
        typography = appTypography(),
    ) {
        EditorialTheme(editorialTypography = softcoverFoundationEditorialTypography()) {
            CompositionLocalProvider(
                LocalEditorialTypography provides defaultEditorialTypography(),
                LocalDarkTheme provides darkTheme,
                content = content,
            )
        }
    }
}

/**
 * The platform's Material You / dynamic color scheme, or `null` when dynamic color is unavailable or
 * unrequested (Android < 12, [useDynamicColor] off, or iOS — which has no dynamic-color equivalent).
 * Callers fall back to the brand [lightScheme]/[darkScheme].
 */
@Composable
expect fun dynamicColorSchemeOrNull(
    useDynamicColor: Boolean,
    darkTheme: Boolean,
): ColorScheme?
