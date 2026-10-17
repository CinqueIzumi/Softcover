package nl.rhaydus.softcover.core.component.callout

sealed interface BannerEvent {
    data object DismissClicked : BannerEvent
}
