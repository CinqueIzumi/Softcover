package nl.rhaydus.softcover.core.domain.connectivity

data class PendingUserBookWrite(
    val kind: PendingUserBookWriteKind,
    val userBookId: Int,
    val userBookReadId: Int,
    val bookId: Int,
    val editionId: Int?,
    val progressPages: Int?,
    val progressSeconds: Int?,
    val startedAt: String?,
    val finishedAt: String?,
    val rating: Double? = null,
    val reviewBody: String? = null,
    val reviewHasSpoilers: Boolean? = null,
    val enqueuedAt: String,
)
