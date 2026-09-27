package nl.rhaydus.softcover.core.component.header

/** [PageMastheadUiModel]'s type scale, per host surface — see [PageMastheadDimensions]. */
enum class PageMastheadSize {
    /** A page's own masthead. */
    Regular,

    /** A masthead sharing space with other chrome, such as a sidebar rail. */
    Compact,
}
