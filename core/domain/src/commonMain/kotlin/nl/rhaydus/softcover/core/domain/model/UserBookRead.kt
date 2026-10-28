package nl.rhaydus.softcover.core.domain.model

data class UserBookRead(
    val id: Int,
    val currentPage: Int?,
    val currentSeconds: Int?,
    val progress: Float,
    val startedAt: String?,
    val finishedAt: String?,
    val finishedAtPrecision: DatePrecision?,
    /** Pages of the edition this read was in; null when the read has no edition or the edition has no page count. */
    val editionPages: Int?,
) {
    /** A read created locally that the server has not assigned an id to yet. */
    val isPlaceholder: Boolean
        get() = id < 0

    val isFinished: Boolean
        get() = finishedAt != null
}
