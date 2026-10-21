package nl.rhaydus.softcover.showcase.live.control.state

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import nl.rhaydus.softcover.core.component.control.ColorPalettePreviewTileUiModel
import nl.rhaydus.softcover.core.component.control.SegmentedControlUiModel
import nl.rhaydus.softcover.core.component.control.ThemePreviewTileUiModel
import nl.rhaydus.softcover.core.component.control.ThemeTilePainting
import nl.rhaydus.softcover.core.component.control.ToggleRowUiModel
import nl.rhaydus.softcover.core.component.topbar.TopBarUiModel
import nl.rhaydus.softcover.core.designsystem.presentation.theme.SpinePalette
import nl.rhaydus.softcover.showcase.appearance.state.AppearanceState
import nl.rhaydus.softcover.showcase.appearance.state.AppearanceUiState
import nl.rhaydus.softcover.showcase.live.control.model.ControlHeadersUiModel
import nl.rhaydus.softcover.showcase.live.control.model.IconToggleUiModel

@Immutable
internal data class ControlLiveUiState(
    val topBar: TopBarUiModel,
    val headers: ControlHeadersUiModel,
    val iconToggles: ImmutableList<IconToggleUiModel>,
    val toggleRows: ImmutableList<ToggleRowUiModel<String>>,
    val trackSegments: SegmentedControlUiModel<String>,
    val proseSegments: SegmentedControlUiModel<String>,
    val themeTiles: ImmutableList<ThemePreviewTileUiModel<ThemeTilePainting>>,
    val paletteTiles: ImmutableList<ColorPalettePreviewTileUiModel<SpinePalette>>,
    override val appearance: AppearanceState = AppearanceState(),
) : AppearanceUiState<ControlLiveUiState> {
    override fun withAppearance(appearance: AppearanceState): ControlLiveUiState = copy(appearance = appearance)
}
