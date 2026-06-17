package nl.rhaydus.softcover.core.preferences.data.model

import kotlinx.serialization.Serializable
import nl.rhaydus.softcover.core.domain.model.DesktopWindowState
import nl.rhaydus.softcover.core.domain.model.WindowPlacement

@Serializable
internal data class DesktopWindowStateEntity(
    val width: Int = DesktopWindowState.DEFAULT_WIDTH,
    val height: Int = DesktopWindowState.DEFAULT_HEIGHT,
    val x: Int? = null,
    val y: Int? = null,
    val placement: WindowPlacement = WindowPlacement.FLOATING,
)

internal fun DesktopWindowStateEntity.toModel(): DesktopWindowState {
    return DesktopWindowState(
        width = this.width,
        height = this.height,
        x = this.x,
        y = this.y,
        placement = this.placement,
    )
}

internal fun DesktopWindowState.toEntity(): DesktopWindowStateEntity {
    return DesktopWindowStateEntity(
        width = this.width,
        height = this.height,
        x = this.x,
        y = this.y,
        placement = this.placement,
    )
}
