package nl.rhaydus.softcover.feature.book_detail.presentation.collector

import nl.rhaydus.softcover.core.component.cover.CoverUiModel
import nl.rhaydus.softcover.core.component.richtext.RichTextUiModel
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.uibinding.verdict.VerdictSheetContext

/** The state fields the verdict sheet's UI model is derived from. */
internal data class VerdictSheetSnapshot(
    val book: Book?,
    val context: VerdictSheetContext?,
    val review: RichTextUiModel?,
    val cover: CoverUiModel?,
)
