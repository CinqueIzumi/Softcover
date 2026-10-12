package nl.rhaydus.softcover.feature.profile.presentation.screen.section

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import nl.rhaydus.softcover.core.component.header.SectionHeader
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.component.statistic.StatTile
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import nl.rhaydus.softcover.feature.profile.presentation.state.ReadingAtlasUiModel

/**
 * Horizontal padding is left to the caller so the block can sit inside the mobile content column or the
 * desktop stats column.
 */
@Composable
internal fun ReadingAtlasSection(
    readingAtlas: ReadingAtlasUiModel,
    header: SectionHeaderUiModel,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        SectionHeader(model = header)

        Spacer(modifier = Modifier.height(Spacing.md))

        StatTile(model = readingAtlas.hero)

        Spacer(modifier = Modifier.height(Spacing.md))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Max),
            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
        ) {
            StatTile(
                model = readingAtlas.volumes,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(Spacing.md),
            ) {
                StatTile(model = readingAtlas.rating)

                StatTile(model = readingAtlas.streak)
            }
        }
    }
}
