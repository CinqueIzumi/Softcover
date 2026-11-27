package nl.rhaydus.softcover.core.profile.domain.model

/**
 * [notAnsweredCount] counts books with no character answer at all; once any character trait is
 * answered, an unmarked one counts in [noCount].
 */
data class CharacterBreakdown(
    val yesCount: Int = 0,
    val complicatedCount: Int = 0,
    val noCount: Int = 0,
    val notAnsweredCount: Int = 0,
) {
    val total: Int get() = yesCount + complicatedCount + noCount + notAnsweredCount

    fun excludingNotAnswered(): CharacterBreakdown = copy(notAnsweredCount = 0)
}
