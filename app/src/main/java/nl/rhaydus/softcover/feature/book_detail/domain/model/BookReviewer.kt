package nl.rhaydus.softcover.feature.book_detail.domain.model

data class BookReviewer(
    val id: Int,
    val username: String,
    val name: String?,
    val avatarUrl: String?,
)
