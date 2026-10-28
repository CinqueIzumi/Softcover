package nl.rhaydus.softcover.feature.profile.presentation.state

import kotlinx.collections.immutable.ImmutableList
import nl.rhaydus.softcover.core.component.statistic.StatTileUiModel

/**
 * The Time to finish section: the median over all counted reads and over each length band, in
 * [tiles] order.
 *
 * @property hasReads false when no read in scope has both dates, so the tiles are dropped and
 * [framing] says why.
 */
internal data class TimeToFinishUiModel(
    val title: String,
    val framing: String,
    val tiles: ImmutableList<StatTileUiModel>,
    val caption: String,
    val hasReads: Boolean,
    val isLoading: Boolean,
)
