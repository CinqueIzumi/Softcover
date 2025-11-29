package nl.rhaydus.softcover.core.domain

data class Book(
    val id: Int,
    val title: String,
    val url: String,
    val authors: List<Author>,
    val totalPages: Int,
)