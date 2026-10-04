package nl.rhaydus.softcover.feature.library.presentation.state

import nl.rhaydus.softcover.core.component.field.TextFieldPillSize
import nl.rhaydus.softcover.core.component.field.TextFieldPillTone
import nl.rhaydus.softcover.core.component.field.TextFieldUiModel
import nl.rhaydus.softcover.core.component.field.TextFieldVariant
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon

internal fun tagSearchFieldFor(query: String): TextFieldUiModel = TextFieldUiModel(
    key = "tag-search",
    text = query,
    placeholder = "Search your tags",
    variant = TextFieldVariant.Pill(
        leadingIcon = SoftcoverIcon.Search,
        size = TextFieldPillSize.Compact,
        tone = TextFieldPillTone.Container,
    ),
)
