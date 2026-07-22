package nl.rhaydus.softcover.core.designsystem.presentation.component

/** A formatting mark of [type] covering the half-open range [[start], [end]) of the editor's plain text. */
internal data class ReviewMark(
    val start: Int,
    val end: Int,
    val type: ReviewMarkType,
)
