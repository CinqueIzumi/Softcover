package nl.rhaydus.softcover.core.designsystem.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import nl.rhaydus.designsystem.editorial.EditorialTheme
import nl.rhaydus.designsystem.theme.RhaydusTheme
import nl.rhaydus.softcover.core.domain.model.ThemeMode

private val lightScheme = lightColorScheme(
    primary = primaryLight,
    onPrimary = onPrimaryLight,
    primaryContainer = primaryContainerLight,
    onPrimaryContainer = onPrimaryContainerLight,
    secondary = secondaryLight,
    onSecondary = onSecondaryLight,
    secondaryContainer = secondaryContainerLight,
    onSecondaryContainer = onSecondaryContainerLight,
    tertiary = tertiaryLight,
    onTertiary = onTertiaryLight,
    tertiaryContainer = tertiaryContainerLight,
    onTertiaryContainer = onTertiaryContainerLight,
    error = errorLight,
    onError = onErrorLight,
    errorContainer = errorContainerLight,
    onErrorContainer = onErrorContainerLight,
    background = backgroundLight,
    onBackground = onBackgroundLight,
    surface = surfaceLight,
    onSurface = onSurfaceLight,
    surfaceVariant = surfaceVariantLight,
    onSurfaceVariant = onSurfaceVariantLight,
    outline = outlineLight,
    outlineVariant = outlineVariantLight,
    scrim = scrimLight,
    inverseSurface = inverseSurfaceLight,
    inverseOnSurface = inverseOnSurfaceLight,
    inversePrimary = inversePrimaryLight,
    surfaceDim = surfaceDimLight,
    surfaceBright = surfaceBrightLight,
    surfaceContainerLowest = surfaceContainerLowestLight,
    surfaceContainerLow = surfaceContainerLowLight,
    surfaceContainer = surfaceContainerLight,
    surfaceContainerHigh = surfaceContainerHighLight,
    surfaceContainerHighest = surfaceContainerHighestLight,
)

private val darkScheme = darkColorScheme(
    primary = primaryDark,
    onPrimary = onPrimaryDark,
    primaryContainer = primaryContainerDark,
    onPrimaryContainer = onPrimaryContainerDark,
    secondary = secondaryDark,
    onSecondary = onSecondaryDark,
    secondaryContainer = secondaryContainerDark,
    onSecondaryContainer = onSecondaryContainerDark,
    tertiary = tertiaryDark,
    onTertiary = onTertiaryDark,
    tertiaryContainer = tertiaryContainerDark,
    onTertiaryContainer = onTertiaryContainerDark,
    error = errorDark,
    onError = onErrorDark,
    errorContainer = errorContainerDark,
    onErrorContainer = onErrorContainerDark,
    background = backgroundDark,
    onBackground = onBackgroundDark,
    surface = surfaceDark,
    onSurface = onSurfaceDark,
    surfaceVariant = surfaceVariantDark,
    onSurfaceVariant = onSurfaceVariantDark,
    outline = outlineDark,
    outlineVariant = outlineVariantDark,
    scrim = scrimDark,
    inverseSurface = inverseSurfaceDark,
    inverseOnSurface = inverseOnSurfaceDark,
    inversePrimary = inversePrimaryDark,
    surfaceDim = surfaceDimDark,
    surfaceBright = surfaceBrightDark,
    surfaceContainerLowest = surfaceContainerLowestDark,
    surfaceContainerLow = surfaceContainerLowDark,
    surfaceContainer = surfaceContainerDark,
    surfaceContainerHigh = surfaceContainerHighDark,
    surfaceContainerHighest = surfaceContainerHighestDark,
)

/**
 * The brand color scheme for one side of the light/dark pair, independent of what the app is
 * currently painting in. [SoftcoverTheme] resolves its own scheme through this, and the Appearance
 * screen's theme preview tiles paint *both* through it — which is why the two schemes are reachable
 * from outside this file rather than being read off `MaterialTheme` at the call site (a tile has to
 * show the theme the reader has *not* picked).
 */
fun softcoverColorScheme(darkTheme: Boolean): ColorScheme = if (darkTheme) darkScheme else lightScheme

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
    // Dynamic color is available on Android 12+; iOS has no equivalent and falls back to the brand scheme.
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val darkTheme = themeMode.isDark()

    val colorScheme = dynamicColorSchemeOrNull(
        useDynamicColor = dynamicColor,
        darkTheme = darkTheme,
    )
        ?: softcoverColorScheme(darkTheme = darkTheme)

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
