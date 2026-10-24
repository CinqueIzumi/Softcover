package nl.rhaydus.softcover.feature.profile.presentation.screen.section

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import nl.rhaydus.softcover.core.component.statistic.StatTile
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import nl.rhaydus.softcover.feature.profile.presentation.state.AtlasOpeningUiModel

/**
 * Horizontal padding is left to the caller so the block can sit inside the mobile content column or the
 * desktop reading column.
 */
@Composable
internal fun AtlasOpeningSection(
    opening: AtlasOpeningUiModel,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        StatTile(model = opening.hero)

        Spacer(modifier = Modifier.height(Spacing.md))

        StatTile(
            model = opening.books,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
