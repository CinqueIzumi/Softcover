package nl.rhaydus.softcover.feature.book_detail.presentation.state

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import nl.rhaydus.softcover.core.component.field.SelectFieldUiModel
import nl.rhaydus.softcover.core.component.field.TextFieldUiModel
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetRowGroupUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetRowUiModel

@Immutable
internal sealed interface WhereYouGotItBodyUiModel {
    data class Choices(
        val group: SheetRowGroupUiModel<String>,
        val priceHeader: SectionHeaderUiModel,
        val amount: TextFieldUiModel,
        val currency: SelectFieldUiModel,
        val caption: String,
    ) : WhereYouGotItBodyUiModel

    data class Currency(
        val searchQuery: String,
        val searchPlaceholder: String,
        val rows: ImmutableList<SheetRowUiModel<String>>,
    ) : WhereYouGotItBodyUiModel
}
