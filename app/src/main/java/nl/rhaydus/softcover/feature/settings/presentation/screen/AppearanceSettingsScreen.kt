package nl.rhaydus.softcover.feature.settings.presentation.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import nl.rhaydus.softcover.core.presentation.component.SoftcoverTopBar
import nl.rhaydus.softcover.core.presentation.theme.SoftcoverTheme
import nl.rhaydus.softcover.core.presentation.theme.StandardPreview
import nl.rhaydus.softcover.feature.settings.domain.model.DateStyle
import nl.rhaydus.softcover.feature.settings.presentation.action.OnDateDropdownExpandedChange
import nl.rhaydus.softcover.feature.settings.presentation.action.OnDateStyleClickAction
import nl.rhaydus.softcover.feature.settings.presentation.action.OnFloatingBarToggledAction
import nl.rhaydus.softcover.feature.settings.presentation.action.SettingsAction
import nl.rhaydus.softcover.feature.settings.presentation.screenmodel.SettingsScreenScreenModel
import nl.rhaydus.softcover.feature.settings.presentation.state.SettingsScreenUiState

class AppearanceSettingsScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        val screenModel = koinScreenModel<SettingsScreenScreenModel>()

        val state by screenModel.state.collectAsStateWithLifecycle()

        Screen(
            state = state,
            onNavigateBack = navigator::pop,
            runAction = screenModel::runAction,
        )
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Screen(
        state: SettingsScreenUiState,
        runAction: (SettingsAction) -> Unit,
        onNavigateBack: () -> Unit,
    ) {
        Scaffold(
            topBar = {
                SoftcoverTopBar(
                    title = "Appearance settings",
                    onNavigateBack = onNavigateBack,
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier.padding(innerPadding)
            ) {
                FloatingBarSection(
                    state = state,
                    runAction = runAction,
                )

                DateSelectorSection(
                    state = state,
                    runAction = runAction,
                )
            }
        }
    }

    @Composable
    private fun FloatingBarSection(
        state: SettingsScreenUiState,
        runAction: (SettingsAction) -> Unit,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Use floating bottom bar",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "When turned off, a normal docked bottom bar will be used instead of the experimantal floating bottom bar.",
                    style = MaterialTheme.typography.labelSmall
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Switch(
                checked = state.useFloatingBarChecked,
                onCheckedChange = {
                    runAction(OnFloatingBarToggledAction(newValue = it))
                }
            )
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun DateSelectorSection(
        state: SettingsScreenUiState,
        runAction: (SettingsAction) -> Unit,
    ) {
        val focusManager = LocalFocusManager.current

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = 16.dp)
        ) {
            Text(
                text = "Date notation",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "The format in which dates displayed within the app should be shown.",
                style = MaterialTheme.typography.labelSmall
            )

            Spacer(modifier = Modifier.height(8.dp))

            ExposedDropdownMenuBox(
                expanded = state.dropDownExpanded,
                onExpandedChange = {
                    runAction(OnDateDropdownExpandedChange(expanded = it))
                }
            ) {
                OutlinedTextField(
                    value = state.userDateStyle.label,
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = state.dropDownExpanded)
                    },
                    modifier = Modifier
                        .menuAnchor(type = ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                        .fillMaxWidth()
                )

                ExposedDropdownMenu(
                    expanded = state.dropDownExpanded,
                    onDismissRequest = {
                        focusManager.clearFocus()

                        runAction(OnDateDropdownExpandedChange(expanded = false))
                    },
                    modifier = Modifier.exposedDropdownSize()
                ) {
                    DateStyle.entries.forEach { style ->
                        DropdownMenuItem(
                            text = {
                                Text(text = style.label)
                            },
                            onClick = {
                                focusManager.clearFocus()

                                runAction(OnDateStyleClickAction(style = style))
                            }
                        )
                    }
                }
            }
        }
    }
}

@StandardPreview
@Composable
private fun AppearanceSettingsPreview() {
    SoftcoverTheme {
        AppearanceSettingsScreen().Screen(
            onNavigateBack = {},
            runAction = {},
            state = SettingsScreenUiState()
        )
    }
}