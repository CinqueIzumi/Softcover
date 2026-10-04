package nl.rhaydus.softcover.feature.explore.presentation.model

import androidx.compose.runtime.Immutable
import nl.rhaydus.softcover.core.component.badge.BadgeUiModel
import nl.rhaydus.softcover.core.component.cover.CoverUiModel
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel

/**
 * The feed-opening upcoming-release hero.
 *
 * @property cover `null` for the one frame between the book landing and its cover being derived; the
 * card keeps its footprint meanwhile.
 * @property description `null` hides the description row.
 * @property isLoading Masks every leaf over its stand-in value and takes no press.
 */
@Immutable
internal data class FeaturedCardUiModel(
    val header: SectionHeaderUiModel,
    val badge: BadgeUiModel?,
    val readersLabel: String,
    val cover: CoverUiModel?,
    val title: String,
    val byline: String,
    val description: String?,
    val inLibrary: Boolean,
    val isLoading: Boolean = false,
)
