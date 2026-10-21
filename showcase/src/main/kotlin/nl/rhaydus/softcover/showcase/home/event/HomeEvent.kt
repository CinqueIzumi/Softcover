package nl.rhaydus.softcover.showcase.home.event

import nl.rhaydus.softcover.core.component.gallery.GalleryFamily
import nl.rhaydus.toad.UiEvent

internal sealed interface HomeEvent : UiEvent {
    data class OpenFamily(val family: GalleryFamily) : HomeEvent
}
