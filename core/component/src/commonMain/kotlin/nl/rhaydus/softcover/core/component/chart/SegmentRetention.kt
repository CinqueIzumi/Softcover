package nl.rhaydus.softcover.core.component.chart

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

internal class SegmentRetention(
    live: List<ChartSliceUiModel>,
    initialLoading: Boolean,
) {
    enum class Phase { Live, Entering, Departing }

    data class Entry(
        val slice: ChartSliceUiModel,
        val phase: Phase,
    )

    var entries by mutableStateOf(
        live.map {
            Entry(
                slice = it,
                phase = Phase.Live,
            )
        },
    )
        private set

    var isLoading by mutableStateOf(initialLoading)
        private set

    fun sync(
        live: List<ChartSliceUiModel>,
        isLoading: Boolean,
    ) {
        val previous = if (this.isLoading || isLoading) emptyList() else entries

        entries = buildList {
            live.forEach { slice ->
                add(
                    Entry(
                        slice = slice,
                        phase = if (previous.isNotEmpty() && previous.none { it.slice.label == slice.label }) {
                            Phase.Entering
                        } else {
                            Phase.Live
                        },
                    ),
                )
            }

            previous.forEachIndexed { index, old ->
                if (live.none { it.label == old.slice.label }) {
                    add(
                        index = index.coerceAtMost(size),
                        element = old.copy(phase = Phase.Departing),
                    )
                }
            }
        }

        this.isLoading = isLoading
    }

    fun release(label: String) {
        entries = entries.filterNot { it.phase == Phase.Departing && it.slice.label == label }
    }
}
