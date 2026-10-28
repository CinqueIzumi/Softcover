package nl.rhaydus.softcover.core.book.domain.model

data class ServerUserBookRead(
    val id: Int,
    val progressPages: Int?,
    val progressSeconds: Int?,
    val finishedAt: String?,
)
