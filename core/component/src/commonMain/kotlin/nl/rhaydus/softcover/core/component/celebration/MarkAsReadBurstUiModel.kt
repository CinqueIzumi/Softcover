package nl.rhaydus.softcover.core.component.celebration

import androidx.compose.runtime.Immutable

@Immutable
data class MarkAsReadBurstUiModel(
    val triggerKey: Int,
    val scale: MarkAsReadBurstScale,
) {
    fun next(): MarkAsReadBurstUiModel = copy(triggerKey = triggerKey + 1)
}
