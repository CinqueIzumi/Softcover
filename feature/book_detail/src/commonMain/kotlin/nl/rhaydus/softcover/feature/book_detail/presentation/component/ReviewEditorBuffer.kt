package nl.rhaydus.softcover.feature.book_detail.presentation.component

/** The review editor's working state: plain [text] plus the flat list of formatting [marks] over it. */
internal data class ReviewEditorBuffer(
    val text: String,
    val marks: List<ReviewMark>,
)
