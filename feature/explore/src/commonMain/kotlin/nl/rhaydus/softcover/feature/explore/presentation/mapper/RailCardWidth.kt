package nl.rhaydus.softcover.feature.explore.presentation.mapper

import androidx.compose.ui.unit.Dp
import nl.rhaydus.softcover.feature.explore.presentation.screen.section.ExploreRail

/** The card width [rail] uses on this platform. */
internal expect fun railCardWidth(rail: ExploreRail): Dp
