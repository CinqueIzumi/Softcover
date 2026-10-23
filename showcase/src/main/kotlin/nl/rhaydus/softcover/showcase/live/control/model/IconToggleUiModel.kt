package nl.rhaydus.softcover.showcase.live.control.model

import androidx.compose.runtime.Immutable
import nl.rhaydus.softcover.core.component.control.ToggleUiModel

@Immutable
internal data class IconToggleUiModel(
    val key: String,
    val toggle: ToggleUiModel,
)
