package nl.rhaydus.softcover.core.component.control

enum class ToggleRowVariant {
    /** The row fills its container's width and draws no padding of its own. */
    Plain,

    /** The row pads itself 4dp horizontally and 16dp vertically, so its press wash bleeds past the text. */
    Inset,
}
