package nl.rhaydus.softcover.feature.settings.presentation.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import nl.rhaydus.designsystem.layout.cappedContentWidth
import nl.rhaydus.softcover.core.component.header.PageMasthead
import nl.rhaydus.softcover.core.component.header.SectionHeader
import nl.rhaydus.softcover.core.component.text.SoftcoverText
import nl.rhaydus.softcover.core.component.topbar.TopBar
import nl.rhaydus.softcover.core.component.topbar.TopBarEvent
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.feature.settings.presentation.action.OnAddWidgetClickAction
import nl.rhaydus.softcover.feature.settings.presentation.action.OnEditWidgetClickAction
import nl.rhaydus.softcover.feature.settings.presentation.action.OnWidgetsBackClickAction
import nl.rhaydus.softcover.feature.settings.presentation.action.WidgetsSettingsAction
import nl.rhaydus.softcover.feature.settings.presentation.screen.section.SettingsMenuRow
import nl.rhaydus.softcover.feature.settings.presentation.screen.section.SettingsMenuRowTarget
import nl.rhaydus.softcover.feature.settings.presentation.screen.section.SettingsRowGroup
import nl.rhaydus.softcover.feature.settings.presentation.state.WidgetsSettingsUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun WidgetsSettingsScreenLayout(
    state: WidgetsSettingsUiState,
    runAction: (WidgetsSettingsAction) -> Unit,
) {
    Scaffold(
        modifier = Modifier.testTag(SettingsTestTags.WIDGETS_PAGE),
        topBar = {
            TopBar(
                model = state.topBar,
                onEvent = { event ->
                    when (event) {
                        TopBarEvent.BackClicked -> runAction(OnWidgetsBackClickAction())
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .cappedContentWidth()
                .padding(horizontal = Spacing.gutter),
        ) {
            Spacer(modifier = Modifier.height(Spacing.xs))

            PageMasthead(model = state.masthead)

            Spacer(modifier = Modifier.height(Spacing.xxxl))

            SectionHeader(model = state.placedHeader)

            Spacer(modifier = Modifier.height(Spacing.md))

            if (state.placedRows.isEmpty()) {
                SoftcoverText(
                    text = state.emptyPlacedGloss,
                    style = MaterialTheme.editorialTypography.body,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                SettingsRowGroup {
                    state.placedRows.forEach { rowModel ->
                        row {
                            SettingsMenuRow(
                                title = rowModel.title,
                                gloss = rowModel.gloss,
                                icon = rowModel.icon,
                                target = if (rowModel.hasSettings) {
                                    SettingsMenuRowTarget.Edit(
                                        onClick = { runAction(OnEditWidgetClickAction(widgetId = rowModel.widgetId)) },
                                    )
                                } else {
                                    SettingsMenuRowTarget.Static
                                },
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(Spacing.section))

            SectionHeader(model = state.addHeader)

            Spacer(modifier = Modifier.height(Spacing.md))

            if (state.canPin) {
                SettingsRowGroup {
                    state.addRows.forEach { rowModel ->
                        row {
                            SettingsMenuRow(
                                title = rowModel.title,
                                gloss = rowModel.gloss,
                                icon = rowModel.icon,
                                target = SettingsMenuRowTarget.Add(
                                    onClick = { runAction(OnAddWidgetClickAction(kind = rowModel.kind)) },
                                ),
                            )
                        }
                    }
                }
            } else {
                SoftcoverText(
                    text = state.pinUnsupportedGloss,
                    style = MaterialTheme.editorialTypography.body,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(modifier = Modifier.height(Spacing.section))
        }
    }
}
