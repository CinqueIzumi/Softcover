package nl.rhaydus.softcover.feature.profile.presentation.state

import nl.rhaydus.softcover.core.component.chart.ColumnChartUiModel
import nl.rhaydus.softcover.core.component.chip.ChipSet

/**
 * The Books you didn't finish section.
 *
 * @property stopPoint null when no book has a known stop point.
 * @property unknownStopCaption null when every book's stop point is known.
 * @property genres null when none of the books carries a genre; the chips are read-only, so they have no payload.
 */
internal data class DidNotFinishUiModel(
    val stopPointTitle: String,
    val stopPoint: ColumnChartUiModel?,
    val unknownStopCaption: String?,
    val genresTitle: String,
    val genres: ChipSet<Unit>?,
    val genresNote: String,
)
