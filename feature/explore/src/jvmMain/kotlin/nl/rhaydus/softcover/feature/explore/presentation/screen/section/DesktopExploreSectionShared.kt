package nl.rhaydus.softcover.feature.explore.presentation.screen.section

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.editorial.component.EditorialSectionHeader

internal val DESKTOP_TRENDING_CARD_WIDTH = 168.dp
internal val DESKTOP_UP_NEXT_CARD_WIDTH = 150.dp
internal const val DESKTOP_DISCOVERY_SKELETON_COUNT = 8

/** The 32×4 accent-bar section opener (design-system.md §2.3), padded to the desktop grid gutter. */
@Composable
internal fun SectionHeaderBar(
    eyebrow: String,
    headline: String,
) {
    EditorialSectionHeader(
        eyebrow = eyebrow,
        headline = headline,
        modifier = Modifier.padding(horizontal = 24.dp),
    )
}
