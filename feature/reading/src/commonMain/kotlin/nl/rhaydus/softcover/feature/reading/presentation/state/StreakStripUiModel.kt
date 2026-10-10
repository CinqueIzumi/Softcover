package nl.rhaydus.softcover.feature.reading.presentation.state

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

internal data class StreakStripUiModel(
    /** Most recent first. */
    val days: ImmutableList<StreakDayUiModel> = persistentListOf(),

    /** `null` when today itself was missed or there is no activity. */
    val runningLabel: String? = null,
    val isLoading: Boolean = false,
)
