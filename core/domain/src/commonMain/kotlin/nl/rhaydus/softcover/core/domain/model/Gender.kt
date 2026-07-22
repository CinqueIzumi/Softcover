package nl.rhaydus.softcover.core.domain.model

/**
 * Hardcover's `gender_id` foreign key, which has no lookup type in the GraphQL schema and so must be
 * mapped here (see architecture.md → Unresolvable API enums). [Unknown] (`null` id) is a distinct
 * bucket from [Other] (`3`) and must never be merged with it in statistics.
 */
enum class Gender(
    val id: Int?,
) {
    Male(id = 1),
    Female(id = 2),
    Other(id = 3),
    Unknown(id = null);

    companion object {
        fun fromId(id: Int?): Gender = entries.firstOrNull { it.id == id } ?: Unknown
    }
}
