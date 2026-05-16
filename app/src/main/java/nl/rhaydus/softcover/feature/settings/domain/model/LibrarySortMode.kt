package nl.rhaydus.softcover.feature.settings.domain.model

enum class LibrarySortMode(val label: String) {
    DATE_ADDED(label = "Date added"),
    DATE_FINISHED(label = "Date finished"),
    TITLE(label = "Title"),
    AUTHOR(label = "Author"),
    RATING(label = "Rating"),
    PROGRESS(label = "Progress"),
    DEADLINE_URGENCY(label = "Deadline urgency"),
    PAGE_COUNT(label = "Page count"),
    ;

    companion object {
        val Default: LibrarySortMode = DATE_ADDED
    }
}
