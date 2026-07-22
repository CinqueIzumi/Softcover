package nl.rhaydus.softcover.core.designsystem.presentation.share

/**
 * One ranked genre on the reading-life share card. [percentage] is a whole-number share of the
 * reader's genre assignments, already rounded at the call site — the card only renders it.
 */
data class ReadingLifeGenre(
    val name: String,
    val percentage: Int,
)
