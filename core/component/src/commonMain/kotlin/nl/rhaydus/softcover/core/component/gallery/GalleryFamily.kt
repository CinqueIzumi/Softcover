package nl.rhaydus.softcover.core.component.gallery

/**
 * One entry per top-level package under `:core:component`, named for the package in upper case
 * (`chip/` is [CHIP]). A new package and a new enum entry land together — adding `component/foo/`
 * without a matching entry here means the gallery can never show it.
 *
 * Declared in the reading order the gallery should present families — the primitives a screen is
 * assembled from first, the composites they assemble into after — rather than alphabetically, so the
 * gallery reads as a progression. Add a new entry at the position that ordering implies, not at the
 * end. [label] drives the gallery's family filter.
 */
enum class GalleryFamily(val label: String) {
    CHIP(label = "Chips"),
    BUTTON(label = "Buttons"),
    BADGE(label = "Badges"),
    DEADLINE(label = "Deadlines"),
    SELECTION(label = "Selection"),
    HEADER(label = "Headers"),
    CONTROL(label = "Controls"),
    FIELD(label = "Fields"),
    DIVIDER(label = "Dividers"),
    TEXT(label = "Text"),
    STATE(label = "States"),
    CELEBRATION(label = "Celebrations"),
    RICHTEXT(label = "Rich text"),
    PROGRESS(label = "Progress"),
    COVER(label = "Covers"),
    ROW(label = "Rows"),
    SIDEBAR(label = "Sidebars"),
    CHROME(label = "Chrome"),
    SNACKBAR(label = "Snackbars"),
    BOOKCARD(label = "Book cards"),
    MOOD(label = "Mood tiles"),
    CALLOUT(label = "Callouts"),
    SHEET(label = "Sheets"),
    DIALOG(label = "Dialogs"),
    LISTS(label = "Lists"),
    STATISTIC(label = "Statistics"),
    CHART(label = "Charts"),
    TOPBAR(label = "Top bars"),
    VERDICT(label = "Verdict"),
    SHARE(label = "Share cards"),
}
