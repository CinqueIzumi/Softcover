package nl.rhaydus.softcover.core.component.control

import androidx.compose.runtime.Immutable

/**
 * One option of a [SegmentedControl].
 *
 * @property key Identity — [SegmentedControlEvent.Selected] carries it back.
 * @property enabled A segment that cannot be chosen yet (its data has not arrived) renders dimmed and inert.
 */
@Immutable
data class SegmentUiModel<K>(
    val key: K,
    val label: String,
    val enabled: Boolean = true,
)
