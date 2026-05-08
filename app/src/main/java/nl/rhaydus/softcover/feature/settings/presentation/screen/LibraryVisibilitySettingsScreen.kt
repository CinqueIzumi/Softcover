package nl.rhaydus.softcover.feature.settings.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import nl.rhaydus.softcover.core.domain.model.UserBookStatus
import nl.rhaydus.softcover.core.presentation.component.EditorialSectionHeader
import nl.rhaydus.softcover.core.presentation.component.SoftcoverButton
import nl.rhaydus.softcover.core.presentation.component.SoftcoverTopBar
import nl.rhaydus.softcover.core.presentation.model.ButtonSize
import nl.rhaydus.softcover.core.presentation.model.ButtonStyle
import nl.rhaydus.softcover.core.presentation.theme.editorialTypography
import nl.rhaydus.softcover.feature.library.presentation.model.LibraryTab
import nl.rhaydus.softcover.feature.settings.presentation.action.LibraryVisibilityAction
import nl.rhaydus.softcover.feature.settings.presentation.action.OnListToggleAction
import nl.rhaydus.softcover.feature.settings.presentation.action.OnSaveLibraryVisibilityAction
import nl.rhaydus.softcover.feature.settings.presentation.action.OnStatusToggleAction
import nl.rhaydus.softcover.feature.settings.presentation.screenmodel.LibraryVisibilitySettingsScreenModel
import nl.rhaydus.softcover.feature.settings.presentation.state.LibraryVisibilitySettingsUiState

class LibraryVisibilitySettingsScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        val screenModel = koinScreenModel<LibraryVisibilitySettingsScreenModel>()

        val state by screenModel.state.collectAsStateWithLifecycle()

        Screen(
            state = state,
            runAction = screenModel::runAction,
            onNavigateBack = navigator::pop,
        )
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Screen(
        state: LibraryVisibilitySettingsUiState,
        runAction: (LibraryVisibilityAction) -> Unit,
        onNavigateBack: () -> Unit,
    ) {
        Scaffold(
            topBar = {
                SoftcoverTopBar(
                    title = "Library tabs",
                    onNavigateBack = onNavigateBack,
                )
            },
            bottomBar = {
                SaveBar(
                    isDirty = state.isDirty,
                    isSaving = state.isSaving,
                    onSave = { runAction(OnSaveLibraryVisibilityAction()) },
                )
            },
            contentWindowInsets = WindowInsets.statusBars,
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp),
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                EditorialSectionHeader(
                    eyebrow = "Statuses",
                    headline = "Shelves on your library",
                )

                Spacer(modifier = Modifier.height(16.dp))

                StatusesGroup(
                    state = state,
                    runAction = runAction,
                )

                Spacer(modifier = Modifier.height(40.dp))

                EditorialSectionHeader(
                    eyebrow = "Lists",
                    headline = "Custom collections",
                )

                Spacer(modifier = Modifier.height(16.dp))

                ListsGroup(
                    state = state,
                    runAction = runAction,
                )

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    @Composable
    private fun StatusesGroup(
        state: LibraryVisibilitySettingsUiState,
        runAction: (LibraryVisibilityAction) -> Unit,
    ) {
        SettingsGroup {
            AlwaysOnRow(
                title = LibraryTab.Status.labelFor(UserBookStatus.CURRENTLY_READING),
                subtitle = "Always visible",
            )

            state.togglableStatuses.forEach { status ->
                SettingsRowDivider()

                ToggleRow(
                    title = LibraryTab.Status.labelFor(status),
                    checked = status.code in state.draftEnabledStatusCodes,
                    onCheckedChange = { enabled ->
                        runAction(
                            OnStatusToggleAction(
                                code = status.code,
                                enabled = enabled,
                            ),
                        )
                    },
                )
            }
        }
    }

    @Composable
    private fun ListsGroup(
        state: LibraryVisibilitySettingsUiState,
        runAction: (LibraryVisibilityAction) -> Unit,
    ) {
        if (state.availableLists.isEmpty()) {
            EmptyListsCard()

            return
        }

        SettingsGroup {
            state.availableLists.forEachIndexed { index, list ->
                if (index > 0) {
                    SettingsRowDivider()
                }

                ToggleRow(
                    title = list.name,
                    checked = list.id in state.draftEnabledListIds,
                    onCheckedChange = { enabled ->
                        runAction(
                            OnListToggleAction(
                                id = list.id,
                                enabled = enabled,
                            ),
                        )
                    },
                )
            }
        }
    }

    @Composable
    private fun EmptyListsCard() {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surfaceContainer,
            shape = RoundedCornerShape(20.dp),
        ) {
            Text(
                text = "You don't have any lists yet.",
                style = MaterialTheme.editorialTypography.body,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(
                    horizontal = 20.dp,
                    vertical = 18.dp,
                ),
            )
        }
    }

    @OptIn(ExperimentalMaterial3ExpressiveApi::class)
    @Composable
    private fun SaveBar(
        isDirty: Boolean,
        isSaving: Boolean,
        onSave: () -> Unit,
    ) {
        Surface(color = MaterialTheme.colorScheme.surface) {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (isSaving) {
                    LinearWavyProgressIndicator(modifier = Modifier.fillMaxWidth())
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = 24.dp,
                            vertical = 16.dp,
                        ),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    SoftcoverButton(
                        label = if (isSaving) "Saving" else "Save",
                        style = ButtonStyle.FILLED,
                        size = ButtonSize.M,
                        enabled = isDirty && isSaving.not(),
                        onClick = onSave,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }

    @Composable
    private fun SettingsGroup(content: @Composable () -> Unit) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surfaceContainer,
            shape = RoundedCornerShape(20.dp),
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                content()
            }
        }
    }

    @Composable
    private fun SettingsRowDivider() {
        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 20.dp),
            color = MaterialTheme.colorScheme.outlineVariant,
        )
    }

    @Composable
    private fun ToggleRow(
        title: String,
        checked: Boolean,
        onCheckedChange: (Boolean) -> Unit,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 20.dp,
                    vertical = 12.dp,
                ),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = title,
                style = MaterialTheme.editorialTypography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .weight(weight = 1f)
                    .padding(end = 16.dp),
            )

            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
            )
        }
    }

    @Composable
    private fun AlwaysOnRow(
        title: String,
        subtitle: String,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 20.dp,
                    vertical = 12.dp,
                ),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier
                    .weight(weight = 1f)
                    .padding(end = 16.dp),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.editorialTypography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = subtitle,
                    style = MaterialTheme.editorialTypography.eyebrowSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            Switch(
                checked = true,
                enabled = false,
                onCheckedChange = {},
            )
        }
    }
}
