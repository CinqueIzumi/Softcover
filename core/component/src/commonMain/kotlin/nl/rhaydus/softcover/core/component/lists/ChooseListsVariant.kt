package nl.rhaydus.softcover.core.component.lists

/**
 * Who the choose-lists sheet is shelving — one book, or a bulk selection.
 *
 * A sealed variant rather than the nullable `bookTitle` sentinel this replaced (R2). "Title is null,
 * therefore this is the bulk case" had to be re-derived as an `isBulk` boolean and then threaded
 * through six private composables, each of which could be handed the wrong one. Here the branch is
 * the type, and the header's `when` over it is exhaustive.
 */
sealed interface ChooseListsVariant {
    /** The `primary`-tinted span in the sheet's "Shelve {…}" headline. */
    val name: String

    data class SingleBook(override val name: String) : ChooseListsVariant

    /**
     * [bookCount] is the whole selection; [coverCount] is how many of those books actually resolved
     * a cover for the header stack, capped at three — so the caption arithmetic and the header art
     * stay independent. **[coverCount] may be 0**, and the header then draws a single upright
     * coverless jacket rather than a lone tilted one, which would read as a mistake rather than a
     * stack.
     */
    data class ManyBooks(
        val bookCount: Int,
        val coverCount: Int,
    ) : ChooseListsVariant {
        override val name: String = "$bookCount books"
    }
}
