package nl.rhaydus.softcover.core.profile.domain.model

/** The author and character shares of the books read inside one stats range, each book counted once. */
data class DiversityStats(
    val booksRead: Int = 0,
    val authorDemographics: AuthorDemographics = AuthorDemographics(),
    val characterRepresentation: CharacterRepresentation = CharacterRepresentation(),
    val publication: PublicationStats = PublicationStats(),
)
