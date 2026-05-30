package nl.rhaydus.softcover.core.domain.connectivity

data class PendingProgressUpdate(
    val kind: PendingProgressUpdateKind,
    val userBookId: Int,
    val userBookReadId: Int,
    val bookId: Int,
    val editionId: Int?,
    val progressPages: Int?,
    val progressSeconds: Int?,
    val startedAt: String?,
    val finishedAt: String?,
    val rating: Double? = null,
    val enqueuedAt: String,
)
