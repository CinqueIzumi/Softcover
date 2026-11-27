package nl.rhaydus.softcover.core.profile.domain.model

data class CharacterRepresentation(
    val bipoc: CharacterBreakdown = CharacterBreakdown(),
    val lgbtq: CharacterBreakdown = CharacterBreakdown(),
) {
    fun excludingNotAnswered(): CharacterRepresentation = CharacterRepresentation(
        bipoc = bipoc.excludingNotAnswered(),
        lgbtq = lgbtq.excludingNotAnswered(),
    )
}
