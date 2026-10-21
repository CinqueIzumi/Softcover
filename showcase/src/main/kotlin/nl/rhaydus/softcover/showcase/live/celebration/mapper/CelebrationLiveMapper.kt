package nl.rhaydus.softcover.showcase.live.celebration.mapper

import nl.rhaydus.softcover.core.component.celebration.MarkAsReadBurstScale
import nl.rhaydus.softcover.core.component.celebration.MarkAsReadBurstUiModel
import nl.rhaydus.softcover.core.component.gallery.GalleryFamily
import nl.rhaydus.softcover.core.component.topbar.TopBarUiModel

internal fun celebrationLiveTopBar(): TopBarUiModel = TopBarUiModel(title = GalleryFamily.CELEBRATION.label)

internal fun restingBurst(scale: MarkAsReadBurstScale): MarkAsReadBurstUiModel = MarkAsReadBurstUiModel(
    triggerKey = 0,
    scale = scale,
)
