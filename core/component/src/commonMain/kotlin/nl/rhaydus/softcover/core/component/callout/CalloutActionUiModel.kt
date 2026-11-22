package nl.rhaydus.softcover.core.component.callout

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList

@Immutable
sealed interface CalloutActionUiModel {
    data class Button(val label: String) : CalloutActionUiModel

    data class Busy(val label: String) : CalloutActionUiModel

    /** @property fraction completion in `0..1`, or `null` for an indeterminate bar. */
    data class Progress(val fraction: Float? = null) : CalloutActionUiModel

    /** @property emptyText shown muted in place of [lines] while there are none. */
    data class Detail(
        val label: String,
        val lines: ImmutableList<String>,
        val emptyText: String,
    ) : CalloutActionUiModel
}
