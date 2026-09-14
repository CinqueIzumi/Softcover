package nl.rhaydus.softcover.core.component.lists

import kotlinx.collections.immutable.ImmutableList
import nl.rhaydus.softcover.core.component.cover.CoverUiModel

/**
 * Who the choose-lists sheet is shelving — one book, or a bulk selection.
 *
 * A sealed variant rather than the nullable `bookTitle` sentinel this replaced (R2). "Title is null,
 * therefore this is the bulk case" had to be re-derived as an `isBulk` boolean and then threaded
 * through six private composables, each of which could be handed the wrong one. Here the branch is
 * the type, and the header's `when` over it is exhaustive.
 *
 * The header art rides on the variant rather than on [ChooseListsUiModel] for the same reason: a
 * single book with a three-jacket stack is a state the type should not be able to express.
 */
sealed interface ChooseListsVariant {
    /** The `primary`-tinted span in the sheet's "Shelve {…}" headline. */
    val name: String

    data class SingleBook(
        override val name: String,
        val cover: CoverUiModel,
    ) : ChooseListsVariant

    /**
     * [bookCount] is the whole selection; [covers] is the header stack — only those selected books
     * that actually resolved a cover, capped at three by the mapper — so the caption arithmetic and
     * the header art stay independent. **[covers] may be empty**, and the header then draws a single
     * upright coverless jacket rather than a lone tilted one, which would read as a mistake rather
     * than a stack.
     */
    data class ManyBooks(
        val bookCount: Int,
        val covers: ImmutableList<CoverUiModel>,
    ) : ChooseListsVariant {
        override val name: String = "$bookCount books"
    }
}
