package nl.rhaydus.softcover.core.component.quote

import androidx.compose.runtime.Immutable
import nl.rhaydus.softcover.core.component.field.TextFieldUiModel

/** The position entry under the active [QuotePositionTab]; an empty field means the quote has no position. */
@Immutable
sealed interface QuotePositionInputUiModel {
    /** [suffix] reads like "of 437 pages"; null when the page count is unknown. */
    data class Page(
        val field: TextFieldUiModel,
        val suffix: String?,
    ) : QuotePositionInputUiModel

    data class Percentage(val field: TextFieldUiModel) : QuotePositionInputUiModel

    data class Time(
        val hours: TextFieldUiModel,
        val minutes: TextFieldUiModel,
        val seconds: TextFieldUiModel,
        val suffix: String?,
    ) : QuotePositionInputUiModel
}
