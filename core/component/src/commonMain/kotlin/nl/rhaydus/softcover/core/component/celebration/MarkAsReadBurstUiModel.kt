package nl.rhaydus.softcover.core.component.celebration

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf
import nl.rhaydus.softcover.core.component.gallery.UiModelPreviews

@Immutable
data class MarkAsReadBurstUiModel(
    val triggerKey: Int,
    val scale: MarkAsReadBurstScale,
) {
    fun next(): MarkAsReadBurstUiModel = copy(triggerKey = triggerKey + 1)

    companion object : UiModelPreviews<MarkAsReadBurstUiModel> {
        override val previews: ImmutableMap<String, MarkAsReadBurstUiModel> = persistentMapOf(
            "Card" to MarkAsReadBurstUiModel(
                triggerKey = 1,
                scale = MarkAsReadBurstScale.Card,
            ),
            "Screen" to MarkAsReadBurstUiModel(
                triggerKey = 1,
                scale = MarkAsReadBurstScale.Screen,
            ),
        )
    }
}
