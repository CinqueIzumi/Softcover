package nl.rhaydus.softcover.core.profile.domain.model

/**
 * [Unknown] also covers a book with one known gender and an untagged co-author, since the untagged
 * author could change the answer.
 */
enum class AuthorshipGender {
    Female,
    Male,
    Other,
    Mixed,
    Unknown,
}
