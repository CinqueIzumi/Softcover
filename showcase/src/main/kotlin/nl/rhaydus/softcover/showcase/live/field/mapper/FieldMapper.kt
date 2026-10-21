package nl.rhaydus.softcover.showcase.live.field.mapper

import kotlinx.collections.immutable.persistentListOf
import nl.rhaydus.softcover.core.component.field.TextFieldError
import nl.rhaydus.softcover.core.component.field.TextFieldPillSize
import nl.rhaydus.softcover.core.component.field.TextFieldUiModel
import nl.rhaydus.softcover.core.component.field.TextFieldVariant
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.component.topbar.TopBarUiModel
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.showcase.live.field.model.FieldSectionUiModel
import nl.rhaydus.softcover.showcase.live.field.state.FieldLiveUiState

private const val PILL = "pill"
private const val PILL_COMPACT = "pill-compact"
private const val TITLE = "title"
private const val PAGES = "pages"
private const val NOTES = "notes"
private const val HERO = "hero"
private const val YEAR = "year"
private const val YEAR_DIGITS = 4

internal fun fieldLiveState(): FieldLiveUiState = FieldLiveUiState(
    topBar = TopBarUiModel(title = "Fields"),
    fields = persistentListOf(
        section(
            eyebrow = "Pill",
            description = "A rounded field for search and quick entry.",
            field = TextFieldUiModel(
                key = PILL,
                text = "",
                placeholder = "Search the shelves",
                variant = TextFieldVariant.Pill(leadingIcon = SoftcoverIcon.Search),
            ),
        ),
        section(
            eyebrow = "Pill, compact",
            description = "The same field, set tighter.",
            field = TextFieldUiModel(
                key = PILL_COMPACT,
                text = "",
                placeholder = "Filter by title",
                variant = TextFieldVariant.Pill(size = TextFieldPillSize.Compact),
            ),
        ),
        section(
            eyebrow = "Outlined",
            description = "A labelled field for a single line.",
            field = TextFieldUiModel(
                key = TITLE,
                text = "Piranesi",
                placeholder = "Title",
                variant = TextFieldVariant.Outlined(label = "Edition title"),
            ),
        ),
        section(
            eyebrow = "Outlined, numeric",
            description = "Opens the number keyboard, and objects to letters.",
            field = TextFieldUiModel(
                key = PAGES,
                text = "272",
                placeholder = "Pages",
                variant = TextFieldVariant.Outlined(
                    label = "Pages",
                    numeric = true,
                ),
            ),
        ),
        section(
            eyebrow = "Multiline",
            description = "Room for a review or a few notes.",
            field = TextFieldUiModel(
                key = NOTES,
                text = "",
                placeholder = "What did you make of it?",
                variant = TextFieldVariant.Multiline,
            ),
        ),
        section(
            eyebrow = "Hero",
            description = "One large, centred line for a title.",
            field = TextFieldUiModel(
                key = HERO,
                text = "",
                placeholder = "Name this shelf",
                variant = TextFieldVariant.Hero,
            ),
        ),
        section(
            eyebrow = "Numeral",
            description = "A fixed run of digits, such as a year. Clear a digit to see it object.",
            field = TextFieldUiModel(
                key = YEAR,
                text = "1984",
                placeholder = "Year",
                variant = TextFieldVariant.Numeral(charCount = YEAR_DIGITS),
            ),
        ),
    ),
)

internal fun fieldError(
    key: String,
    text: String,
): TextFieldError? = when {
    key == YEAR && (text.length != YEAR_DIGITS || text.all { it.isDigit() }.not()) ->
        TextFieldError(message = "A year takes four digits.")
    key == PAGES && text.all { it.isDigit() }.not() ->
        TextFieldError(message = "Pages are counted in numbers.")
    else -> null
}

private fun section(
    eyebrow: String,
    description: String,
    field: TextFieldUiModel,
): FieldSectionUiModel = FieldSectionUiModel(
    header = SectionHeaderUiModel.Section(
        eyebrow = eyebrow,
        description = description,
    ),
    field = field,
)
