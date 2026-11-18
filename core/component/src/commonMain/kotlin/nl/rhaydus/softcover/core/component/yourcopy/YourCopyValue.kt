package nl.rhaydus.softcover.core.component.yourcopy

import androidx.compose.runtime.Immutable

@Immutable
sealed interface YourCopyValue {
    data class Set(val text: String) : YourCopyValue

    /** Nothing set yet; [invitation] shows muted in place of a value. */
    data class Empty(val invitation: String) : YourCopyValue
}
