package nl.rhaydus.softcover.core.component.share

/**
 * One ranked genre on the reading-life share card. [percentage] is a whole-number share of the
 * reader's genre-tagged books, shown as the finished [percentLabel] ("34%"). Genres overlap, so the
 * percentages on a card do not sum to 100 and are not parts of a whole.
 */
data class ReadingLifeGenre(
    val name: String,
    val percentLabel: String,
)
