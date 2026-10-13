package nl.rhaydus.softcover.core.presentation.session

import androidx.compose.runtime.Immutable
import nl.rhaydus.softcover.core.component.cover.CoverUiModel

@Immutable
data class ActiveSessionUiModel(
    val title: String,
    val author: String?,
    val isPaused: Boolean,
    val startedAtEpochMs: Long,
    val pausedSeconds: Int,
    val pausedAtEpochMs: Long?,
    val peekBarCover: CoverUiModel,
    val focusCover: CoverUiModel,
)
