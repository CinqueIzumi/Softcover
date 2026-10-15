package nl.rhaydus.softcover.core.component.share

/**
 * How much of the text a book or reading-update card shows. [Story] is the fixed 9:16 frame that
 * truncates long text; [FullText] keeps the width and grows downward so nothing is cut.
 */
enum class ShareCardFormat {
    Story,
    FullText,
}
