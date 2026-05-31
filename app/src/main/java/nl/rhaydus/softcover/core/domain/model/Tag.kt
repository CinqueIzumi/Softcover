package nl.rhaydus.softcover.core.domain.model

data class Tag(
    val id: Int,
    val name: String,
    val category: TagCategory = TagCategory.OTHER,
    val count: Int = 0,
)
