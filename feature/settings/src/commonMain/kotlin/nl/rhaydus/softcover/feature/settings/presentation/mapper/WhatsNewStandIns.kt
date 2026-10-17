package nl.rhaydus.softcover.feature.settings.presentation.mapper

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import nl.rhaydus.softcover.core.component.document.MarkdownDocumentBlockUiModel
import nl.rhaydus.softcover.core.component.document.MarkdownDocumentListItemUiModel
import nl.rhaydus.softcover.core.component.document.MarkdownDocumentSpanUiModel
import nl.rhaydus.softcover.core.component.document.MarkdownDocumentUiModel
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.feature.settings.presentation.model.DocumentSection

private const val WHATS_NEW_STAND_IN_ENTRY_COUNT = 2

/** Two entries, each a header and a list sized like real ones, drawn masked until the changelog loads. */
internal val WHATS_NEW_STAND_IN_SECTIONS: ImmutableList<DocumentSection> = List(WHATS_NEW_STAND_IN_ENTRY_COUNT) {
    DocumentSection(
        header = SectionHeaderUiModel.Section(
            eyebrow = "0.0.0",
            headline = "Release headline",
            isLoading = true,
        ),
        dateText = null,
        body = MarkdownDocumentUiModel(
            blocks = persistentListOf(
                MarkdownDocumentBlockUiModel.ListGroup(
                    items = List(3) {
                        MarkdownDocumentListItemUiModel(
                            marker = "•",
                            spans = persistentListOf(
                                MarkdownDocumentSpanUiModel(text = "A change that shipped in this release"),
                            ),
                        )
                    }.toImmutableList(),
                ),
            ),
            isLoading = true,
        ),
    )
}.toImmutableList()
