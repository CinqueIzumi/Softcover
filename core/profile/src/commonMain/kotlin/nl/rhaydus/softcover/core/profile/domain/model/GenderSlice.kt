package nl.rhaydus.softcover.core.profile.domain.model

data class GenderSlice(
    val gender: AuthorshipGender,
    val count: Int,
    val fraction: Double,
)
