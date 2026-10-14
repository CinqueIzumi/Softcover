package nl.rhaydus.softcover.feature.settings.presentation.model

import androidx.compose.runtime.Immutable
import nl.rhaydus.softcover.core.component.document.MarkdownDocumentUiModel
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel

/**
 * One titled run of a document page. A page that is a single continuous document leaves [header] and
 * [dateText] `null`; [dateText] is also `null` when the section has no date line.
 */
@Immutable
internal data class DocumentSection(
    val header: SectionHeaderUiModel.Section?,
    val dateText: String?,
    val body: MarkdownDocumentUiModel,
)
