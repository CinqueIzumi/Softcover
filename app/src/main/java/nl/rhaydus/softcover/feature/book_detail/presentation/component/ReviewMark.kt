package nl.rhaydus.softcover.feature.book_detail.presentation.component

/** A formatting mark of [type] covering the half-open range [[start], [end]) of the editor's plain text. */
data class ReviewMark(
    val start: Int,
    val end: Int,
    val type: ReviewMarkType,
)
