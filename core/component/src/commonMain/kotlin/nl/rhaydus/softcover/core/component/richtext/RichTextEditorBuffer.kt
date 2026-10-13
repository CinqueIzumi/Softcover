package nl.rhaydus.softcover.core.component.richtext

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList

/**
 * A rich-text editor's working state: plain [text] plus the flat list of formatting [marks] over it.
 *
 * Flat marks over plain text rather than a [RichTextUiModel] tree, because that is the shape the
 * editing operations want — toggling a mark over a selection and shifting marks across an edit are
 * both cheap on ranges and awkward on a tree. `RichTextEditing.kt` converts between the two.
 */
@Immutable
data class RichTextEditorBuffer(
    val text: String,
    val marks: ImmutableList<RichTextMark>,
)
