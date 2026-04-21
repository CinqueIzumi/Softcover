package nl.rhaydus.softcover.feature.settings.presentation.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import nl.rhaydus.softcover.core.presentation.component.SoftcoverTopBar
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
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
            ) {
                SectionHeader(text = "Statuses")

                AlwaysOnRow(
                    title = LibraryTab.Status.labelFor(UserBookStatus.CURRENTLY_READING),
                    subtitle = "Always visible",
                )

                HorizontalDivider()

                state.togglableStatuses.forEach { status ->
                    ToggleRow(
                        title = LibraryTab.Status.labelFor(status),
                        checked = status.code in state.draftEnabledStatusCodes,
                        onCheckedChange = { enabled ->
                            runAction(OnStatusToggleAction(code = status.code, enabled = enabled))
                        },
                    )

                    HorizontalDivider()
                }

                Spacer(modifier = Modifier.height(16.dp))

                SectionHeader(text = "Lists")

                if (state.availableLists.isEmpty()) {
                    Text(
                        text = "You don't have any lists yet.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                } else {
                    state.availableLists.forEach { list ->
                        ToggleRow(
                            title = list.name,
                            checked = list.id in state.draftEnabledListIds,
                            onCheckedChange = { enabled ->
                                runAction(OnListToggleAction(id = list.id, enabled = enabled))
                            },
                        )

                        HorizontalDivider()
                    }
                }
            }
        }
    }

    @Composable
    private fun SaveBar(
        isDirty: Boolean,
        isSaving: Boolean,
        onSave: () -> Unit,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Button(
                onClick = onSave,
                enabled = isDirty && isSaving.not(),
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )

                    Spacer(modifier = Modifier.width(8.dp))
                }

                Text(text = if (isSaving) "Saving…" else "Save")
            }
        }
    }

    @Composable
    private fun SectionHeader(text: String) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp),
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
                .padding(all = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f),
            )

            Spacer(modifier = Modifier.width(16.dp))

            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
    }

    @Composable
    private fun AlwaysOnRow(title: String, subtitle: String) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                )

                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Switch(
                checked = true,
                enabled = false,
                onCheckedChange = {},
            )
        }
    }
}
