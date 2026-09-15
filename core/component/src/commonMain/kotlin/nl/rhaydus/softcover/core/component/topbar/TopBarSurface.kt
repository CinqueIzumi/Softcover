package nl.rhaydus.softcover.core.component.topbar

/**
 * What a [TopBar] sits on, which decides both its own fill and the ink its controls need to stay
 * legible.
 *
 * One field rather than three: before the component library, the book page derived a container
 * colour, a back-button colour pair and an overflow-menu colour pair separately from the same
 * "has the hero scrolled away yet" flag, and any one of them could have been forgotten.
 */
enum class TopBarSurface {
    /** The page's own background — the bar fills with the theme's app-bar container. */
    OPAQUE,

    /**
     * Cover art or another image scrolling beneath a transparent bar. Controls take a dark scrim and
     * white ink so they read against whatever passes under them.
     */
    OVER_MEDIA,
}
