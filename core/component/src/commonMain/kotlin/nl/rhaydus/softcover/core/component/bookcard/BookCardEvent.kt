package nl.rhaydus.softcover.core.component.bookcard

sealed interface BookCardEvent {
    val key: BookCardKey

    data class Clicked(override val key: BookCardKey) : BookCardEvent

    /** Emitted only under [BookCardDecorations.Press.TapAndLongPress]. */
    data class LongClicked(override val key: BookCardKey) : BookCardEvent

    /** The pointer went down on the card itself, before any click resolves; never for a child control. */
    data class PressStarted(override val key: BookCardKey) : BookCardEvent

    data class TrailingClicked(override val key: BookCardKey) : BookCardEvent

    data class MenuClicked(override val key: BookCardKey) : BookCardEvent

    data class ShakeEnded(override val key: BookCardKey) : BookCardEvent
}
