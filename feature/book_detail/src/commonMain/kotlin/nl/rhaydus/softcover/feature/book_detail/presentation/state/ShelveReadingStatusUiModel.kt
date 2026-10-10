package nl.rhaydus.softcover.feature.book_detail.presentation.state

import androidx.compose.runtime.Immutable
import nl.rhaydus.softcover.core.component.statistic.StatNumberUiModel

/** The "Reading" shelve row's trailing status, such as "42% · p. 120". */
@Immutable
internal data class ShelveReadingStatusUiModel(
    val percent: StatNumberUiModel,
    val position: StatNumberUiModel,
)
