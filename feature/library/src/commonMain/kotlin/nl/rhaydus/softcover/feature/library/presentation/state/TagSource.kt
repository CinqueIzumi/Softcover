package nl.rhaydus.softcover.feature.library.presentation.state

internal enum class TagSource(
    val title: String,
    val searchPlaceholder: String,
    val gloss: String?,
) {
    YOURS(
        title = "Your tags",
        searchPlaceholder = "Search your tags",
        gloss = null,
    ),
    HARDCOVER(
        title = "Hardcover tags",
        searchPlaceholder = "Search Hardcover tags",
        gloss = "The tags readers on Hardcover gave these books.",
    ),
}
