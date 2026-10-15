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

private const val ROADMAP_STAND_IN_ENTRY_COUNT = 3

/** One headerless section of three entries, each a heading and a list, drawn masked until the roadmap loads. */
internal val ROADMAP_STAND_IN_SECTIONS: ImmutableList<DocumentSection> = persistentListOf(
    DocumentSection(
        header = null,
        dateText = null,
        body = MarkdownDocumentUiModel(
            blocks = List(ROADMAP_STAND_IN_ENTRY_COUNT) {
                listOf(
                    MarkdownDocumentBlockUiModel.SectionHeading(
                        header = SectionHeaderUiModel.Section(
                            eyebrow = "0.0.0",
                            headline = "Roadmap entry title",
                            isLoading = true,
                        ),
                    ),
                    MarkdownDocumentBlockUiModel.ListGroup(
                        items = List(3) {
                            MarkdownDocumentListItemUiModel(
                                marker = "•",
                                spans = persistentListOf(
                                    MarkdownDocumentSpanUiModel(
                                        text = "A roadmap item describing what is coming next",
                                    ),
                                ),
                            )
                        }.toImmutableList(),
                    ),
                )
            }.flatten().toImmutableList(),
            isLoading = true,
        ),
    ),
)
