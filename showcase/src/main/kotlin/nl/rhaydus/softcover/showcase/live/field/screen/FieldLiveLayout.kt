package nl.rhaydus.softcover.showcase.live.field.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.imePadding
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
import nl.rhaydus.softcover.core.component.field.TextField
import nl.rhaydus.softcover.core.component.field.TextFieldEvent
import nl.rhaydus.softcover.core.component.header.SectionHeader
import nl.rhaydus.softcover.core.component.topbar.TopBar
import nl.rhaydus.softcover.core.component.topbar.TopBarEvent
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import nl.rhaydus.softcover.showcase.appearance.action.OnAppearanceClickedAction
import nl.rhaydus.softcover.showcase.appearance.action.OnBackClickedAction
import nl.rhaydus.softcover.showcase.appearance.screen.AppearanceSheetHost
import nl.rhaydus.softcover.showcase.live.field.action.OnFieldShakeEndedAction
import nl.rhaydus.softcover.showcase.live.field.action.OnFieldValueChangedAction
import nl.rhaydus.softcover.showcase.live.field.event.FieldLiveEvent
import nl.rhaydus.softcover.showcase.live.field.screenmodel.FieldLiveDependencies
import nl.rhaydus.softcover.showcase.live.field.state.FieldLiveLocalVariables
import nl.rhaydus.softcover.showcase.live.field.state.FieldLiveUiState
import nl.rhaydus.toad.UiAction

@Composable
internal fun FieldLiveLayout(
    state: FieldLiveUiState,
    runAction: (UiAction<FieldLiveDependencies, FieldLiveUiState, FieldLiveEvent, FieldLiveLocalVariables>) -> Unit,
) {
    Scaffold(
        contentWindowInsets = WindowInsets.systemBars.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal),
        topBar = {
            TopBar(
                model = state.topBar,
                onEvent = { event ->
                    when (event) {
                        TopBarEvent.BackClicked -> runAction(OnBackClickedAction(backEvent = FieldLiveEvent.NavigateBack))
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
            modifier = Modifier.padding(padding).imePadding(),
            contentPadding = PaddingValues(
                start = Spacing.xl,
                top = Spacing.xl,
                end = Spacing.xl,
                bottom = Spacing.xl + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding(),
            ),
            verticalArrangement = Arrangement.spacedBy(Spacing.xl),
        ) {
            items(items = state.fields, key = { it.field.key }) { section ->
                Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                    SectionHeader(model = section.header)

                    TextField(
                        model = section.field,
                        onEvent = { event ->
                            when (event) {
                                is TextFieldEvent.ValueChanged -> runAction(
                                    OnFieldValueChangedAction(
                                        key = event.key,
                                        text = event.text,
                                    ),
                                )
                                is TextFieldEvent.ShakeEnded -> runAction(OnFieldShakeEndedAction(key = event.key))
                                is TextFieldEvent.Submitted -> Unit
                                is TextFieldEvent.TrailingClicked -> Unit
                            }
                        },
                    )
                }
            }
        }
    }

    AppearanceSheetHost(
        appearance = state.appearance,
        runAction = runAction,
    )
}
