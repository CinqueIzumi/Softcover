package nl.rhaydus.softcover.feature.book_detail.presentation.state

import nl.rhaydus.softcover.core.component.button.PillButtonUiModel
import nl.rhaydus.softcover.core.component.field.TextFieldImeAction
import nl.rhaydus.softcover.core.component.field.TextFieldUiModel
import nl.rhaydus.softcover.core.component.field.TextFieldVariant
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.domain.model.TagCategory

internal fun tagNamingFieldFor(
    input: String,
    category: TagCategory,
    addPill: PillButtonUiModel,
): TextFieldUiModel = TextFieldUiModel(
    key = "tag-name",
    text = input,
    placeholder = category.namingHint,
    variant = TextFieldVariant.Pill(
        leadingIcon = SoftcoverIcon.Edit,
        trailing = addPill.takeIf { input.isNotBlank() },
    ),
    imeAction = TextFieldImeAction.Done,
)

/**
 * [BookDetailUiState.tagEditorInput] is the stored draft; its text is mirrored onto
 * [BookDetailUiState.tagNamingField] here so a keystroke never waits on a collector.
 */
internal fun BookDetailUiState.withTagDraft(input: String): BookDetailUiState = copy(
    tagEditorInput = input,
    tagNamingField = tagNamingField.copy(text = input),
)

/**
 * The name-field hint per category. Deliberately not "Name a ${'$'}{label} tag" — that template
 * degenerates to "Name a Tag tag" for the plain Tag category.
 */
private val TagCategory.namingHint: String
    get() = when (this) {
        TagCategory.GENRE -> "Name a genre…"
        TagCategory.MOOD -> "Name a mood…"
        TagCategory.TAG -> "Name a tag…"
        TagCategory.CONTENT_WARNING -> "Name a content warning…"
        TagCategory.OTHER -> "Name a tag…"
    }
