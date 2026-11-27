package nl.rhaydus.softcover.core.domain.model

data class BookCopy(
    val bookId: Int,
    val source: AcquisitionSource? = null,
) {
    val isEmpty: Boolean
        get() = source == null
}
