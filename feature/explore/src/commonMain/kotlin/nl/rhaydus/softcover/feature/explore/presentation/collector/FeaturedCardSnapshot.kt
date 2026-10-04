package nl.rhaydus.softcover.feature.explore.presentation.collector

import nl.rhaydus.softcover.core.component.badge.BadgeUiModel
import nl.rhaydus.softcover.core.component.cover.CoverUiModel
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.domain.model.Book

/** The state fields the featured card is derived from. */
internal data class FeaturedCardSnapshot(
    val book: Book?,
    val cover: CoverUiModel?,
    val badge: BadgeUiModel?,
    val header: SectionHeaderUiModel,
    val isLoading: Boolean,
)
