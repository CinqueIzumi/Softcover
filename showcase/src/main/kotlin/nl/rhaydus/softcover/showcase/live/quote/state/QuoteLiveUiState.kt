package nl.rhaydus.softcover.showcase.live.quote.state

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import nl.rhaydus.softcover.core.component.quote.QuoteSheetUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetActionRowUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetSession
import nl.rhaydus.softcover.core.component.topbar.TopBarUiModel
import nl.rhaydus.softcover.showcase.appearance.state.AppearanceState
import nl.rhaydus.softcover.showcase.appearance.state.AppearanceUiState
import nl.rhaydus.softcover.showcase.live.quote.mapper.quoteDemos
import nl.rhaydus.softcover.showcase.live.quote.mapper.quoteLiveTopBar
import nl.rhaydus.softcover.showcase.live.quote.model.QuoteDemo

@Immutable
internal data class QuoteLiveUiState(
    val topBar: TopBarUiModel = quoteLiveTopBar(),
    val demos: ImmutableList<SheetActionRowUiModel<QuoteDemo>> = quoteDemos(),
    val sheet: SheetSession<QuoteSheetUiModel, Nothing> = SheetSession.Closed,
    override val appearance: AppearanceState = AppearanceState(),
) : AppearanceUiState<QuoteLiveUiState> {
    override fun withAppearance(appearance: AppearanceState): QuoteLiveUiState = copy(appearance = appearance)
}
