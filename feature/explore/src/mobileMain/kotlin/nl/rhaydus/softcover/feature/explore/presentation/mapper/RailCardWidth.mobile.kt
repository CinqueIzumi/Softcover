package nl.rhaydus.softcover.feature.explore.presentation.mapper

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import nl.rhaydus.softcover.feature.explore.presentation.screen.section.ExploreRail

internal actual fun railCardWidth(rail: ExploreRail): Dp = when (rail) {
    ExploreRail.UP_NEXT -> 126.dp
    ExploreRail.BECAUSE_YOU_READ, ExploreRail.TRENDING -> 118.dp
}
