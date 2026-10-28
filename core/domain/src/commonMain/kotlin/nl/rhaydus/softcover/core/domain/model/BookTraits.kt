package nl.rhaydus.softcover.core.domain.model

/**
 * The reader's private, device-only description of how a book read. Never synced to Hardcover.
 *
 * @property plotCharacterBalance 0 is fully plot-driven, 100 fully character-driven, null unanswered.
 */
data class BookTraits(
    val bookId: Int,
    val pace: Pace? = null,
    val plotCharacterBalance: Int? = null,
    val characters: Map<CharacterTrait, TraitAnswer> = emptyMap(),
    val writingStyle: WritingStyle? = null,
    val writingFlags: Set<WritingFlag> = emptySet(),
) {
    val isEmpty: Boolean
        get() = pace == null &&
            plotCharacterBalance == null &&
            characters.isEmpty() &&
            writingStyle == null &&
            writingFlags.isEmpty()
}
