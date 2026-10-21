package nl.rhaydus.softcover.showcase.live.control.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import nl.rhaydus.softcover.core.component.chrome.ChromeIconButton
import nl.rhaydus.softcover.core.component.control.ColorPalettePreviewTile
import nl.rhaydus.softcover.core.component.control.PreviewTileEvent
import nl.rhaydus.softcover.core.component.control.SegmentedControl
import nl.rhaydus.softcover.core.component.control.SegmentedControlEvent
import nl.rhaydus.softcover.core.component.control.ThemePreviewTile
import nl.rhaydus.softcover.core.component.control.Toggle
import nl.rhaydus.softcover.core.component.control.ToggleEvent
import nl.rhaydus.softcover.core.component.control.ToggleRow
import nl.rhaydus.softcover.core.component.header.SectionHeader
import nl.rhaydus.softcover.core.component.topbar.TopBar
import nl.rhaydus.softcover.core.component.topbar.TopBarEvent
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import nl.rhaydus.softcover.showcase.appearance.action.OnAppearanceClickedAction
import nl.rhaydus.softcover.showcase.appearance.action.OnBackClickedAction
import nl.rhaydus.softcover.showcase.appearance.screen.AppearanceSheetHost
import nl.rhaydus.softcover.showcase.live.control.action.OnIconToggledAction
import nl.rhaydus.softcover.showcase.live.control.action.OnPaletteTileSelectedAction
import nl.rhaydus.softcover.showcase.live.control.action.OnProseSegmentSelectedAction
import nl.rhaydus.softcover.showcase.live.control.action.OnRowToggledAction
import nl.rhaydus.softcover.showcase.live.control.action.OnThemeTileSelectedAction
import nl.rhaydus.softcover.showcase.live.control.action.OnTrackSegmentSelectedAction
import nl.rhaydus.softcover.showcase.live.control.event.ControlLiveEvent
import nl.rhaydus.softcover.showcase.live.control.screenmodel.ControlLiveDependencies
import nl.rhaydus.softcover.showcase.live.control.state.ControlLiveLocalVariables
import nl.rhaydus.softcover.showcase.live.control.state.ControlLiveUiState
import nl.rhaydus.toad.UiAction

@Composable
internal fun ControlLiveLayout(
    state: ControlLiveUiState,
    runAction: (UiAction<ControlLiveDependencies, ControlLiveUiState, ControlLiveEvent, ControlLiveLocalVariables>) -> Unit,
) {
    Scaffold(
        contentWindowInsets = WindowInsets.systemBars.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal),
        topBar = {
            TopBar(
                model = state.topBar,
                onEvent = { event ->
                    when (event) {
                        TopBarEvent.BackClicked -> runAction(OnBackClickedAction(backEvent = ControlLiveEvent.NavigateBack))
                    }
                },
            ) { colors ->
                ChromeIconButton(
                    icon = SoftcoverIcon.Palette,
                    contentDescription = "Appearance",
                    tooltip = "Appearance",
                    onClick = { runAction(OnAppearanceClickedAction()) },
                    colors = colors,
                )
            }
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(
                start = Spacing.xl,
                top = Spacing.xl,
                end = Spacing.xl,
                bottom = Spacing.xl + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding(),
            ),
            verticalArrangement = Arrangement.spacedBy(Spacing.xl),
        ) {
            item { SectionHeader(model = state.headers.toggles) }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
                    state.iconToggles.forEach { item ->
                        Toggle(
                            model = item.toggle,
                            onEvent = { event ->
                                when (event) {
                                    is ToggleEvent.Toggled -> runAction(
                                        OnIconToggledAction(
                                            key = item.key,
                                            checked = event.checked,
                                        ),
                                    )
                                }
                            },
                        )
                    }
                }
            }

            item { SectionHeader(model = state.headers.rows) }

            items(items = state.toggleRows, key = { it.key }) { row ->
                ToggleRow(
                    model = row,
                    onEvent = { event ->
                        when (event) {
                            is ToggleEvent.Toggled -> runAction(
                                OnRowToggledAction(
                                    key = row.key,
                                    checked = event.checked,
                                ),
                            )
                        }
                    },
                )
            }

            item { SectionHeader(model = state.headers.segments) }

            item {
                SegmentedControl(
                    model = state.trackSegments,
                    onEvent = { event ->
                        when (event) {
                            is SegmentedControlEvent.Selected -> runAction(OnTrackSegmentSelectedAction(key = event.key))
                        }
                    },
                )
            }

            item {
                SegmentedControl(
                    model = state.proseSegments,
                    onEvent = { event ->
                        when (event) {
                            is SegmentedControlEvent.Selected -> runAction(OnProseSegmentSelectedAction(key = event.key))
                        }
                    },
                )
            }

            item { SectionHeader(model = state.headers.themes) }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
                    state.themeTiles.forEach { tile ->
                        ThemePreviewTile(
                            model = tile,
                            onEvent = { event ->
                                when (event) {
                                    is PreviewTileEvent.Clicked -> runAction(OnThemeTileSelectedAction(painting = event.key))
                                }
                            },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }

            item { SectionHeader(model = state.headers.palettes) }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
                    state.paletteTiles.forEach { tile ->
                        ColorPalettePreviewTile(
                            model = tile,
                            onEvent = { event ->
                                when (event) {
                                    is PreviewTileEvent.Clicked -> runAction(OnPaletteTileSelectedAction(palette = event.key))
                                }
                            },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }
    }

    AppearanceSheetHost(
        appearance = state.appearance,
        runAction = runAction,
    )
}
