package nl.rhaydus.softcover.showcase.family.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import nl.rhaydus.softcover.core.component.text.SoftcoverText
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Corners
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.showcase.family.model.EntrySectionUiModel
import nl.rhaydus.softcover.showcase.family.model.FixtureFrameUiModel

@Composable
internal fun EntrySection(entry: EntrySectionUiModel) {
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

        entry.fixtures.forEachIndexed { index, frame ->
            if (index > 0) Spacer(modifier = Modifier.height(Spacing.md))

            FixtureFrame(frame = frame)
        }
    }
}

/**
 * The fixture's label above a tile that fixes its width and leaves its height to the fixture, so a chip
 * stays a line tall and a card keeps its own body.
 */
@Composable
private fun FixtureFrame(frame: FixtureFrameUiModel) {
    Column(modifier = Modifier.fillMaxWidth()) {
        SoftcoverText(
            text = frame.label,
            style = MaterialTheme.editorialTypography.eyebrowSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(Spacing.xs))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = Corners.md,
            color = MaterialTheme.colorScheme.surfaceContainerLow,
        ) {
            frame.fixture.render(
                Modifier
                    .fillMaxWidth()
                    .padding(Spacing.md),
            )
        }
    }
}
