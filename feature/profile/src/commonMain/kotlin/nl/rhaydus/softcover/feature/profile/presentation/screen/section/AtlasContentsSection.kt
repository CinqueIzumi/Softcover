package nl.rhaydus.softcover.feature.profile.presentation.screen.section

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlinx.collections.immutable.ImmutableList
import nl.rhaydus.softcover.core.component.divider.Divider
import nl.rhaydus.softcover.core.component.header.SectionHeader
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import nl.rhaydus.softcover.feature.profile.presentation.state.AtlasChapter
import nl.rhaydus.softcover.feature.profile.presentation.state.AtlasContentsRowUiModel

@Composable
internal fun AtlasContentsSection(
    header: SectionHeaderUiModel,
    rows: ImmutableList<AtlasContentsRowUiModel>,
    onRowClick: (AtlasChapter) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        SectionHeader(model = header)

        Spacer(modifier = Modifier.height(Spacing.xs))

        Divider()

        for (row in rows) {
            AtlasContentsRow(
                model = row,
                onClick = { onRowClick(row.chapter) },
            )

            Divider()
        }
    }
}
