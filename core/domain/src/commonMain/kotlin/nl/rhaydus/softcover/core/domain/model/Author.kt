package nl.rhaydus.softcover.core.domain.model

/**
 * [isBipoc] and [isLgbtq] are `null` when Hardcover has no value for the author; [gender] is
 * [Gender.Unknown] in that case, which is distinct from [Gender.Other].
 */
data class Author(
    val id: Int,
    val name: String,
    val isBipoc: Boolean? = null,
    val isLgbtq: Boolean? = null,
    val gender: Gender = Gender.Unknown,
)
