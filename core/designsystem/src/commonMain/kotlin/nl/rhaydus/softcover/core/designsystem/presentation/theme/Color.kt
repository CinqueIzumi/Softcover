package nl.rhaydus.softcover.core.designsystem.presentation.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color

internal val primaryLight = Color(0xFF8F4C38)
internal val onPrimaryLight = Color(0xFFFFFFFF)
internal val primaryContainerLight = Color(0xFFFFDBD1)
internal val onPrimaryContainerLight = Color(0xFF723523)
internal val secondaryLight = Color(0xFF77574E)
internal val onSecondaryLight = Color(0xFFFFFFFF)
internal val secondaryContainerLight = Color(0xFFFFDBD1)
internal val onSecondaryContainerLight = Color(0xFF5D4037)
internal val tertiaryLight = Color(0xFF6C5D2F)
internal val onTertiaryLight = Color(0xFFFFFFFF)
internal val tertiaryContainerLight = Color(0xFFF5E1A7)
internal val onTertiaryContainerLight = Color(0xFF534619)
internal val errorLight = Color(0xFFBA1A1A)
internal val onErrorLight = Color(0xFFFFFFFF)
internal val errorContainerLight = Color(0xFFFFDAD6)
internal val onErrorContainerLight = Color(0xFF93000A)
internal val backgroundLight = Color(0xFFFFF8F6)
internal val onBackgroundLight = Color(0xFF231917)
internal val surfaceLight = Color(0xFFFFF8F6)
internal val onSurfaceLight = Color(0xFF231917)
internal val surfaceVariantLight = Color(0xFFF5DED8)
internal val onSurfaceVariantLight = Color(0xFF53433F)
internal val outlineLight = Color(0xFF85736E)
internal val outlineVariantLight = Color(0xFFD8C2BC)
internal val scrimLight = Color(0xFF000000)
internal val inverseSurfaceLight = Color(0xFF392E2B)
internal val inverseOnSurfaceLight = Color(0xFFFFEDE8)
internal val inversePrimaryLight = Color(0xFFFFB5A0)
internal val surfaceDimLight = Color(0xFFE8D6D2)
internal val surfaceBrightLight = Color(0xFFFFF8F6)
internal val surfaceContainerLowestLight = Color(0xFFFFFFFF)
internal val surfaceContainerLowLight = Color(0xFFFFF1ED)
internal val surfaceContainerLight = Color(0xFFFCEAE5)
internal val surfaceContainerHighLight = Color(0xFFF7E4E0)
internal val surfaceContainerHighestLight = Color(0xFFF1DFDA)

internal val primaryDark = Color(0xFFFFB5A0)
internal val onPrimaryDark = Color(0xFF561F0F)
internal val primaryContainerDark = Color(0xFF723523)
internal val onPrimaryContainerDark = Color(0xFFFFDBD1)
internal val secondaryDark = Color(0xFFE7BDB2)
internal val onSecondaryDark = Color(0xFF442A22)
internal val secondaryContainerDark = Color(0xFF5D4037)
internal val onSecondaryContainerDark = Color(0xFFFFDBD1)
internal val tertiaryDark = Color(0xFFD8C58D)
internal val onTertiaryDark = Color(0xFF3B2F05)
internal val tertiaryContainerDark = Color(0xFF534619)
internal val onTertiaryContainerDark = Color(0xFFF5E1A7)
internal val errorDark = Color(0xFFFFB4AB)
internal val onErrorDark = Color(0xFF690005)
internal val errorContainerDark = Color(0xFF93000A)
internal val onErrorContainerDark = Color(0xFFFFDAD6)
internal val backgroundDark = Color(0xFF1A110F)
internal val onBackgroundDark = Color(0xFFF1DFDA)
internal val surfaceDark = Color(0xFF1A110F)
internal val onSurfaceDark = Color(0xFFF1DFDA)
internal val surfaceVariantDark = Color(0xFF53433F)
internal val onSurfaceVariantDark = Color(0xFFD8C2BC)
internal val outlineDark = Color(0xFFA08C87)
internal val outlineVariantDark = Color(0xFF53433F)
internal val scrimDark = Color(0xFF000000)
internal val inverseSurfaceDark = Color(0xFFF1DFDA)
internal val inverseOnSurfaceDark = Color(0xFF392E2B)
internal val inversePrimaryDark = Color(0xFF8F4C38)
internal val surfaceDimDark = Color(0xFF1A110F)
internal val surfaceBrightDark = Color(0xFF423734)
internal val surfaceContainerLowestDark = Color(0xFF140C0A)
internal val surfaceContainerLowDark = Color(0xFF231917)
internal val surfaceContainerDark = Color(0xFF271D1B)
internal val surfaceContainerHighDark = Color(0xFF322825)
internal val surfaceContainerHighestDark = Color(0xFF3D322F)

/**
 * Brand gold reserved for rating stars — both the read-only community/review rating glyphs and
 * the interactive personal rating control. Lives outside the Material scheme because the warm
 * gold reads identically in light and dark and is a deliberate "rating" signal, not a theme role.
 */
val RatingGold = Color(0xFFFBBF23)

/**
 * Spoiler redaction treatment, derived from `onSurfaceVariant` so it tracks light/dark. The two roles
 * are deliberately different: in the review editor the author must keep reading their own words, so a
 * spoiler run gets only a translucent [spoilerEditorHighlight] wash; on display the run is hidden under
 * a near-solid [spoilerCover] block until the reader taps to reveal it.
 */
val ColorScheme.spoilerEditorHighlight: Color
    get() = onSurfaceVariant.copy(alpha = 0.20f)

internal val ColorScheme.spoilerCover: Color
    get() = onSurfaceVariant.copy(alpha = 0.90f)

/**
 * Fixed warm inks for the Explore "Browse by mood" tiles and the unreleased-book monogram cover
 * (explore-3a). Like [RatingGold] these sit outside the Material scheme — the spec states they read
 * identically in both themes, as a deliberate signal (a printed-jacket ink) rather than a theme role.
 * Never re-declare one of these hexes at a call site; two of the four mood tiles stay on ordinary
 * theme roles instead (`surfaceContainerHigh` / `onSurface` / `primary` / `onSurfaceVariant`) because
 * only two of the spec's tiles are ink-tinted — see `docs/reference/design-system.md`.
 */
val MoodInkCosyBackground = Color(0xFF3A3016)
val MoodInkCosyForeground = Color(0xFFF5E1A7)

val MoodInkDreadBackground = Color(0xFF2B1A15)
val MoodInkDreadForeground = Color(0xFFFFEDE8)
val MoodInkDreadEyebrow = Color(0xFFFFDBD1)

val MoodInkHeartWrenchBackground = Color(0xFF3A1E22)
val MoodInkHeartWrenchForeground = Color(0xFFFFD9DE)

/**
 * The fixed dark ink behind the app-wide coverless-cover monogram - used whenever no cover art
 * resolves, independent of release status.
 */
val MonogramCoverInk = Color(0xFF1A110F)
val MonogramCoverForeground = Color(0xFFFFEDE8)
