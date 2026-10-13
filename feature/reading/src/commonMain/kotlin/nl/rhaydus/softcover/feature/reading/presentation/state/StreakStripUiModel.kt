package nl.rhaydus.softcover.feature.reading.presentation.state

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import nl.rhaydus.softcover.core.component.statistic.StatNumberUiModel

@Immutable
internal data class StreakStripUiModel(
    /** Most recent first. */
    val days: ImmutableList<StreakDayUiModel> = persistentListOf(),

    /** `null` when today itself was missed or there is no activity. */
    val running: StatNumberUiModel? = null,
    val isLoading: Boolean = false,
)
