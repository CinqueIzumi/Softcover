package nl.rhaydus.softcover.core.component.gallery

/**
 * One entry per family directory in `:core:component` (the migration tracker's § 3 package layout).
 * A new family package and a new enum entry land together — adding `component/foo/` without a
 * matching entry here means the gallery can never show it.
 *
 * Declared in the reading order the gallery should present families: primitives first
 * (chips, badges, headers, controls, state, progress), composites after (rows, cards, sheets,
 * statistics, top bars, share). [label] drives the gallery's family filter.
 */
enum class GalleryFamily(val label: String) {
    CHIP(label = "Chips"),
    BADGE(label = "Badges"),
    HEADER(label = "Headers"),
    CONTROL(label = "Controls"),
    STATE(label = "States"),
    PROGRESS(label = "Progress"),
    COVER(label = "Covers"),
    ROW(label = "Rows"),
    BOOKCARD(label = "Book cards"),
    CALLOUT(label = "Callouts"),
    SHEET(label = "Sheets"),
    STATISTIC(label = "Statistics"),
    TOPBAR(label = "Top bars"),
    SHARE(label = "Share cards"),
}
