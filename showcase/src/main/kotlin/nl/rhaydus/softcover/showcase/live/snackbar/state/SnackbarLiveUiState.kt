package nl.rhaydus.softcover.showcase.live.snackbar.state

import androidx.compose.runtime.Immutable
import nl.rhaydus.softcover.core.component.sheet.SheetScaffoldUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetSession
import nl.rhaydus.softcover.core.component.topbar.TopBarUiModel
import nl.rhaydus.softcover.showcase.appearance.state.AppearanceState
import nl.rhaydus.softcover.showcase.appearance.state.AppearanceUiState
import nl.rhaydus.softcover.showcase.live.overlaypage.model.OverlayTriggerUiModel

@Immutable
internal data class SnackbarLiveUiState(
    val topBar: TopBarUiModel,
    val plainTrigger: OverlayTriggerUiModel,
    val actionTrigger: OverlayTriggerUiModel,
    val sheetTrigger: OverlayTriggerUiModel,
    val sheet: SheetSession<SheetScaffoldUiModel, Nothing> = SheetSession.Closed,
    override val appearance: AppearanceState = AppearanceState(),
) : AppearanceUiState<SnackbarLiveUiState> {
    override fun withAppearance(appearance: AppearanceState): SnackbarLiveUiState = copy(appearance = appearance)
}
