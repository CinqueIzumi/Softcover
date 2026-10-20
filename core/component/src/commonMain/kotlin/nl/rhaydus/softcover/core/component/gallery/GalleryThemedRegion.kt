package nl.rhaydus.softcover.core.component.gallery

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableMap
import nl.rhaydus.softcover.core.component.header.SectionHeader
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.component.text.SoftcoverText
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Corners
import nl.rhaydus.softcover.core.designsystem.presentation.theme.SchemeBrightness
import nl.rhaydus.softcover.core.designsystem.presentation.theme.SoftcoverTheme
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import nl.rhaydus.softcover.core.designsystem.presentation.theme.SpinePalette
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography

/**
 * Every [GalleryRegistry] entry inside its own framed paper rather than bleeding into the page: a
 * [SoftcoverTheme] of [palette] and [brightness], painted onto a hairline-bordered [Surface] filled with
 * that theme's own `background`, so the previewed look reads as a distinct spread against any chrome
 * around it. [sectionHeaders] opens each family's section; [family] narrows the sections to one family; `null` shows all.
 *
 * [dynamicColor] is for a host that follows the device wallpaper; it replaces [palette] outright, so a
 * host that lets the reader pick a palette passes `false`.
 */
@Composable
fun GalleryThemedRegion(
    palette: SpinePalette,
    brightness: SchemeBrightness,
    sectionHeaders: ImmutableMap<GalleryFamily, SectionHeaderUiModel>,
    modifier: Modifier = Modifier,
    family: GalleryFamily? = null,
    dynamicColor: Boolean = false,
) {
    SoftcoverTheme(
        palette = palette,
        brightness = brightness,
        dynamicColor = dynamicColor,
    ) {
        Surface(
            modifier = modifier.fillMaxWidth(),
            shape = Corners.xl,
            color = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.onBackground,
            border = BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant,
            ),
        ) {
            Column(modifier = Modifier.padding(Spacing.xl)) {
                val visibleFamilies = GalleryFamily.entries.filter { family == null || it == family }

                visibleFamilies.forEachIndexed { index, visible ->
                    if (index > 0) Spacer(modifier = Modifier.height(Spacing.section))

                    GalleryFamilySection(
                        family = visible,
                        header = sectionHeaders.getValue(visible),
                    )
                }
            }
        }
    }
}

@Composable
private fun GalleryFamilySection(
    family: GalleryFamily,
    header: SectionHeaderUiModel,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        SectionHeader(model = header)

        Spacer(modifier = Modifier.height(Spacing.md))

        val entries = GalleryRegistry.entries.filter { it.family == family }

        entries.forEachIndexed { index, entry ->
            if (index > 0) Spacer(modifier = Modifier.height(Spacing.xxxl))

            GalleryEntrySection(entry = entry)
        }
    }
}

@Composable
private fun GalleryEntrySection(entry: GalleryEntry) {
    Column(modifier = Modifier.fillMaxWidth()) {
        SoftcoverText(
            text = entry.name,
            style = MaterialTheme.editorialTypography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Spacer(modifier = Modifier.height(Spacing.xxs))

        SoftcoverText(
            text = entry.blurb,
            style = MaterialTheme.editorialTypography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(Spacing.md))

        entry.fixtures.forEachIndexed { index, fixture ->
            if (index > 0) Spacer(modifier = Modifier.height(Spacing.md))

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
        SoftcoverText(
            text = fixture.label.uppercase(),
            style = MaterialTheme.editorialTypography.eyebrowSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(Spacing.xs))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = Corners.md,
            color = MaterialTheme.colorScheme.surfaceContainerLow,
        ) {
            fixture.render(
                Modifier
                    .fillMaxWidth()
                    .padding(Spacing.md),
            )
        }
    }
}
