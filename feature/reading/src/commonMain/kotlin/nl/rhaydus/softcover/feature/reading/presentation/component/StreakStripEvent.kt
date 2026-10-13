package nl.rhaydus.softcover.feature.reading.presentation.component

internal sealed interface StreakStripEvent {
    data object Clicked : StreakStripEvent
}
