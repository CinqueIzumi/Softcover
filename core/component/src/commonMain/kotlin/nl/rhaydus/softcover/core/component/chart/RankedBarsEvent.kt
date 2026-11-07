package nl.rhaydus.softcover.core.component.chart

sealed interface RankedBarsEvent {
    /** Emitted when a row of a tappable ranking is clicked; [key] is the row's [RankedBarUiModel.key]. */
    data class RowClicked(val key: String) : RankedBarsEvent
}
