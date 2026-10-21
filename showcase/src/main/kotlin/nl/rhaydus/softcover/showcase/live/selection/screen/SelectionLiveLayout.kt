package nl.rhaydus.softcover.showcase.live.selection.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import nl.rhaydus.softcover.core.component.chrome.ChromeIconButton
import nl.rhaydus.softcover.core.component.cover.Cover
import nl.rhaydus.softcover.core.component.header.SectionHeader
import nl.rhaydus.softcover.core.component.modifier.tactileToggleable
import nl.rhaydus.softcover.core.component.selection.SelectionCheck
import nl.rhaydus.softcover.core.component.selection.SelectionMark
import nl.rhaydus.softcover.core.component.selection.SelectionMarkBackdrop
import nl.rhaydus.softcover.core.component.topbar.TopBar
import nl.rhaydus.softcover.core.component.topbar.TopBarEvent
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import nl.rhaydus.softcover.showcase.appearance.action.OnAppearanceClickedAction
import nl.rhaydus.softcover.showcase.appearance.action.OnBackClickedAction
import nl.rhaydus.softcover.showcase.appearance.screen.AppearanceSheetHost
import nl.rhaydus.softcover.showcase.live.selection.action.OnCoverToggledAction
import nl.rhaydus.softcover.showcase.live.selection.action.OnRowToggledAction
import nl.rhaydus.softcover.showcase.live.selection.event.SelectionLiveEvent
import nl.rhaydus.softcover.showcase.live.selection.screenmodel.SelectionLiveDependencies
import nl.rhaydus.softcover.showcase.live.selection.state.SelectionLiveLocalVariables
import nl.rhaydus.softcover.showcase.live.selection.state.SelectionLiveUiState
import nl.rhaydus.toad.UiAction

@Composable
internal fun SelectionLiveLayout(
    state: SelectionLiveUiState,
    runAction: (UiAction<SelectionLiveDependencies, SelectionLiveUiState, SelectionLiveEvent, SelectionLiveLocalVariables>) -> Unit,
) {
    Scaffold(
        contentWindowInsets = WindowInsets.systemBars.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal),
        topBar = {
            TopBar(
                model = state.topBar,
                onEvent = { event ->
                    when (event) {
                        TopBarEvent.BackClicked -> runAction(OnBackClickedAction(backEvent = SelectionLiveEvent.NavigateBack))
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
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(
                start = Spacing.xl,
                top = Spacing.xl,
                end = Spacing.xl,
                bottom = Spacing.xl + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding(),
            ),
            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
            verticalArrangement = Arrangement.spacedBy(Spacing.md),
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                SectionHeader(model = state.coversHeader)
            }

            items(items = state.covers, key = { it.key }) { item ->
                Box(
                    modifier = Modifier.tactileToggleable(
                        value = item.selected,
                        onValueChange = { runAction(OnCoverToggledAction(key = item.key)) },
                        role = Role.Checkbox,
                    ),
                ) {
                    Cover(
                        model = item.cover,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    SelectionMark(
                        selected = item.selected,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(Spacing.xs),
                    )
                }
            }

            item(span = { GridItemSpan(maxLineSpan) }) {
                SectionHeader(model = state.shelvesHeader)
            }

            items(
                items = state.shelves,
                key = { it.key },
                span = { GridItemSpan(maxLineSpan) },
            ) { row ->
                SelectableRow(
                    title = row.title,
                    selected = row.selected,
                    onToggle = { runAction(OnRowToggledAction(key = row.key)) },
                ) {
                    SelectionMark(
                        selected = row.selected,
                        backdrop = SelectionMarkBackdrop.Surface,
                    )
                }
            }

            item(span = { GridItemSpan(maxLineSpan) }) {
                SectionHeader(model = state.formatsHeader)
            }

            items(
                items = state.formats,
                key = { it.key },
                span = { GridItemSpan(maxLineSpan) },
            ) { row ->
                SelectableRow(
                    title = row.title,
                    selected = row.selected,
                    onToggle = { runAction(OnRowToggledAction(key = row.key)) },
                ) {
                    SelectionCheck(selected = row.selected)
                }
            }
        }
    }

    AppearanceSheetHost(
        appearance = state.appearance,
        runAction = runAction,
    )
}
