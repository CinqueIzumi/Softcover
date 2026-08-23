package nl.rhaydus.softcover.feature.settings.presentation.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.editorial.component.EditorialSectionHeader
import nl.rhaydus.softcover.core.component.gallery.GalleryEntry
import nl.rhaydus.softcover.core.component.gallery.GalleryFamily
import nl.rhaydus.softcover.core.component.gallery.GalleryFixture
import nl.rhaydus.softcover.core.component.gallery.GalleryRegistry
import nl.rhaydus.softcover.core.designsystem.presentation.component.PillChip
import nl.rhaydus.softcover.core.designsystem.presentation.modifier.quoteGlyphSway
import nl.rhaydus.softcover.core.designsystem.presentation.theme.LocalDarkTheme
import nl.rhaydus.softcover.core.designsystem.presentation.theme.LocalThemeConfiguration
import nl.rhaydus.softcover.core.designsystem.presentation.theme.SoftcoverTheme
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.core.domain.model.ColorPalette
import nl.rhaydus.softcover.core.domain.model.ThemeMode
import nl.rhaydus.softcover.feature.settings.presentation.action.ComponentGalleryAction
import nl.rhaydus.softcover.feature.settings.presentation.action.OnGalleryFamilySelectedAction
import nl.rhaydus.softcover.feature.settings.presentation.action.OnGalleryPaletteSelectedAction
import nl.rhaydus.softcover.feature.settings.presentation.action.OnGalleryThemeModeSelectedAction
import nl.rhaydus.softcover.feature.settings.presentation.state.ComponentGalleryUiState

private val THEMED_REGION_SHAPE = RoundedCornerShape(20.dp)
private val FIXTURE_FRAME_SHAPE = RoundedCornerShape(12.dp)
// region Gallery content
/**
 * The Component Gallery screen body, shared by the mobile [ComponentGalleryScreen] page and the
 * desktop standalone fallback (`component-contract.md` § 7.5). Opens with the two override chip rows
 * (brightness, spine colour — always rendered, since they retint the themed region below even with an
 * empty registry) and, once [GalleryRegistry.families] is non-empty, a family filter row — all three
 * stay in the app's own current look, since they are chrome, not previewed content. Everything from
 * [GalleryThemedRegion] down renders inside a [SoftcoverTheme] resolved from [state]'s overrides, so
 * only the previewed material steps into the chosen brightness/palette.
 */
@Composable
internal fun ComponentGalleryContent(
    state: ComponentGalleryUiState,
    runAction: (ComponentGalleryAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Every component in the library, rendered from its own preview fixtures — in any " +
                "brightness, and any spine colour.",
            style = MaterialTheme.editorialTypography.body,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(32.dp))

        EditorialSectionHeader(
            eyebrow = "Preview controls",
            headline = "See it in every light",
        )

        Spacer(modifier = Modifier.height(20.dp))

        GalleryOverrideControls(
            state = state,
            runAction = runAction,
        )

        if (GalleryRegistry.families.isNotEmpty()) {
            Spacer(modifier = Modifier.height(24.dp))

            GalleryFamilyFilterRow(
                selectedFamily = state.selectedFamily,
                runAction = runAction,
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        GalleryThemedRegion(state = state)
    }
}
// endregion
// region Controls
@Composable
private fun GalleryOverrideControls(
    state: ComponentGalleryUiState,
    runAction: (ComponentGalleryAction) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        GalleryControlLabel(text = "Brightness")

        Spacer(modifier = Modifier.height(10.dp))

        GalleryChipRow(
            options = ThemeMode.entries,
            label = { it.label },
            isSelected = { it == state.themeModeOverride },
            onSelect = { mode -> runAction(OnGalleryThemeModeSelectedAction(mode = mode)) },
        )

        Spacer(modifier = Modifier.height(20.dp))

        GalleryControlLabel(text = "Spine colour")

        Spacer(modifier = Modifier.height(10.dp))

        GalleryChipRow(
            options = ColorPalette.entries,
            label = { it.label },
            isSelected = { it == state.paletteOverride },
            onSelect = { palette -> runAction(OnGalleryPaletteSelectedAction(palette = palette)) },
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "A highlighted chip is the override in effect — tap it again to hand the gallery " +
                "back to the app's own current setting.",
            style = MaterialTheme.editorialTypography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun GalleryFamilyFilterRow(
    selectedFamily: GalleryFamily?,
    runAction: (ComponentGalleryAction) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        GalleryControlLabel(text = "Family")

        Spacer(modifier = Modifier.height(10.dp))

        GalleryChipRow(
            options = GalleryRegistry.families,
            label = { it.label },
            isSelected = { it == selectedFamily },
            onSelect = { family -> runAction(OnGalleryFamilySelectedAction(family = family)) },
        )
    }
}

/**
 * A bar-less, in-flow sub-label (§2.3's third eyebrow register) for a control living inside the
 * "Preview controls" section [EditorialSectionHeader] already opened above — never paired with an
 * accent bar of its own.
 */
@Composable
private fun GalleryControlLabel(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.editorialTypography.eyebrowSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun <T> GalleryChipRow(
    options: List<T>,
    label: (T) -> String,
    isSelected: (T) -> Boolean,
    onSelect: (T) -> Unit,
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        options.forEach { option ->
            PillChip(
                label = label(option),
                selected = isSelected(option),
                onClick = { onSelect(option) },
            )
        }
    }
}
// endregion
// region Themed region
/**
 * Everything a reader previews sits inside its own framed paper rather than bleeding into the page:
 * a [SoftcoverTheme] resolved from [state]'s overrides (falling back to [LocalThemeConfiguration] —
 * the app's own current setting — wherever an override is `null`), painted onto a hairline-bordered
 * [Surface] filled with that theme's own `background`. The border and independent fill are what let the
 * reader see the previewed palette's paper as a distinct spread, not as a continuation of the chrome
 * above (which stays in the app's own look throughout).
 */
@Composable
private fun GalleryThemedRegion(state: ComponentGalleryUiState) {
    val configuration = LocalThemeConfiguration.current

    val themeMode = state.themeModeOverride ?: configuration.themeMode
    val colorPalette = state.paletteOverride ?: configuration.colorPalette

    // Dynamic colour replaces the chosen palette outright (see SoftcoverTheme's KDoc) — so once a
    // palette override is in effect here, dynamic colour must step aside, or the wallpaper scheme
    // would silently override the override right back.
    val dynamicColor = configuration.useDynamicColor && state.paletteOverride == null

    SoftcoverTheme(
        themeMode = themeMode,
        colorPalette = colorPalette,
        dynamicColor = dynamicColor,
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = THEMED_REGION_SHAPE,
            color = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.onBackground,
            border = BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant,
            ),
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                val families = GalleryRegistry.families

                if (families.isEmpty()) {
                    GalleryEmptyState()
                } else {
                    val visibleFamilies = families.filter {
                        state.selectedFamily == null || it == state.selectedFamily
                    }

                    visibleFamilies.forEachIndexed { index, family ->
                        if (index > 0) Spacer(modifier = Modifier.height(40.dp))

                        GalleryFamilySection(family = family)
                    }
                }
            }
        }
    }
}

@Composable
private fun GalleryFamilySection(family: GalleryFamily) {
    Column(modifier = Modifier.fillMaxWidth()) {
        EditorialSectionHeader(
            eyebrow = "Family",
            headline = family.label,
        )

        Spacer(modifier = Modifier.height(20.dp))

        val entries = GalleryRegistry.entriesIn(family)

        entries.forEachIndexed { index, entry ->
            if (index > 0) Spacer(modifier = Modifier.height(32.dp))

            GalleryEntrySection(entry = entry)
        }
    }
}

@Composable
private fun GalleryEntrySection(entry: GalleryEntry) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = entry.name,
            style = MaterialTheme.editorialTypography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = entry.blurb,
            style = MaterialTheme.editorialTypography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(16.dp))

        entry.fixtures.forEachIndexed { index, fixture ->
            if (index > 0) Spacer(modifier = Modifier.height(16.dp))

            GalleryFixtureFrame(fixture = fixture)
        }
    }
}

/**
 * One fixture's frame: its label, quiet and small, above a [Surface] tile that constrains the
 * fixture's *width* (via the [Modifier] handed to [GalleryFixture.render]) while leaving its height to
 * the fixture itself — a chip sizes to a line, a card sizes to its own body, and neither is stretched
 * or clipped to match its neighbours.
 */
@Composable
private fun GalleryFixtureFrame(fixture: GalleryFixture) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = fixture.label.uppercase(),
            style = MaterialTheme.editorialTypography.eyebrowSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = FIXTURE_FRAME_SHAPE,
            color = MaterialTheme.colorScheme.surfaceContainerLow,
        ) {
            fixture.render(
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            )
        }
    }
}
// endregion
// region Empty state
/**
 * The S2 reality: the registry is empty until the first migration stage appends to it. Reuses the
 * documented "editorial quote, empty-state variant" (design-system §5) — the same low-alpha, swaying
 * quote glyph the empty Reading and Hidden-suggestions screens use — with copy that explains what will
 * eventually fill this frame rather than reading as an unfinished placeholder.
 */
@Composable
private fun GalleryEmptyState() {
    val quoteAlpha = if (LocalDarkTheme.current) 0.15f else 0.3f

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "“",
            style = MaterialTheme.editorialTypography.quoteGlyph,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = quoteAlpha),
            modifier = Modifier.quoteGlyphSway(),
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "The shelf is being built",
            style = MaterialTheme.editorialTypography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Every component the app renders is moving into this library, one migration " +
                "stage at a time. A family's fixtures appear here the moment its stage lands — " +
                "rendered in whichever brightness and spine colour you've chosen above.",
            style = MaterialTheme.editorialTypography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}
// endregion
