package nl.rhaydus.softcover.showcase.live.dialog.state

import androidx.compose.runtime.Immutable
import kotlinx.datetime.LocalDate
import nl.rhaydus.softcover.core.component.dialog.DatePickerUiModel
import nl.rhaydus.softcover.core.component.topbar.TopBarUiModel
import nl.rhaydus.softcover.showcase.appearance.state.AppearanceState
import nl.rhaydus.softcover.showcase.appearance.state.AppearanceUiState
import nl.rhaydus.softcover.showcase.live.overlaypage.model.OverlayTriggerUiModel

@Immutable
internal data class DialogLiveUiState(
    val topBar: TopBarUiModel,
    val trigger: OverlayTriggerUiModel,
    val pickedDate: LocalDate? = null,
    val datePicker: DatePickerUiModel? = null,
    override val appearance: AppearanceState = AppearanceState(),
) : AppearanceUiState<DialogLiveUiState> {
    override fun withAppearance(appearance: AppearanceState): DialogLiveUiState = copy(appearance = appearance)
}
