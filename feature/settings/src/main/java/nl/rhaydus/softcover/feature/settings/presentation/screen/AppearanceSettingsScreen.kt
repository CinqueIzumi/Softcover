package nl.rhaydus.softcover.feature.settings.presentation.screen

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
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
import nl.rhaydus.softcover.core.designsystem.presentation.component.EditorialSectionHeader
import nl.rhaydus.softcover.core.designsystem.presentation.component.SoftcoverTopBar
import nl.rhaydus.softcover.core.designsystem.presentation.modifier.noRippleClickable
import nl.rhaydus.softcover.core.designsystem.presentation.theme.SoftcoverTheme
import nl.rhaydus.softcover.core.designsystem.presentation.theme.StandardPreview
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.core.domain.model.DateStyle
import nl.rhaydus.softcover.feature.settings.presentation.action.OnDateStyleClickAction
import nl.rhaydus.softcover.feature.settings.presentation.action.OnDynamicColorToggledAction
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
                    title = "Appearance",
                    onNavigateBack = onNavigateBack,
                )
            },
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(
                        horizontal = 24.dp,
                        vertical = 16.dp,
                    ),
            ) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    DynamicColorSection(
                        state = state,
                        runAction = runAction,
                    )

                    Spacer(modifier = Modifier.height(40.dp))
                }

                BottomBarSection(
                    state = state,
                    runAction = runAction,
                )

                Spacer(modifier = Modifier.height(40.dp))

                DateStyleSection(
                    state = state,
                    runAction = runAction,
                )

                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }
    // region Dynamic color section
    @Composable
    private fun DynamicColorSection(
        state: SettingsScreenUiState,
        runAction: (SettingsAction) -> Unit,
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            EditorialSectionHeader(
                eyebrow = "Material You",
                headline = "Tint to your wallpaper",
                description = "Recolour Softcover with the system palette from your wallpaper.",
            )

            Spacer(modifier = Modifier.height(20.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surfaceContainer,
                shape = RoundedCornerShape(20.dp),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = 20.dp,
                            vertical = 18.dp,
                        ),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Dynamic colour",
                            style = MaterialTheme.editorialTypography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = if (state.useDynamicColorChecked) "On" else "Off",
                            style = MaterialTheme.editorialTypography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Switch(
                        checked = state.useDynamicColorChecked,
                        onCheckedChange = {
                            runAction(OnDynamicColorToggledAction(newValue = it))
                        },
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "The source palette is picked in the system Wallpaper & style settings.",
                style = MaterialTheme.editorialTypography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 4.dp),
            )
        }
    }
    // endregion
    // region Bottom bar section
    @Composable
    private fun BottomBarSection(
        state: SettingsScreenUiState,
        runAction: (SettingsAction) -> Unit,
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            EditorialSectionHeader(
                eyebrow = "Bottom bar",
                headline = "Floating navigation",
                description = "When turned off, a docked bottom bar is shown instead of the floating variant.",
            )

            Spacer(modifier = Modifier.height(20.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surfaceContainer,
                shape = RoundedCornerShape(20.dp),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = 20.dp,
                            vertical = 18.dp,
                        ),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Floating bottom bar",
                            style = MaterialTheme.editorialTypography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = if (state.useFloatingBarChecked) "On" else "Off",
                            style = MaterialTheme.editorialTypography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Switch(
                        checked = state.useFloatingBarChecked,
                        onCheckedChange = {
                            runAction(OnFloatingBarToggledAction(newValue = it))
                        },
                    )
                }
            }
        }
    }
    // endregion
    // region Date style section
    @Composable
    private fun DateStyleSection(
        state: SettingsScreenUiState,
        runAction: (SettingsAction) -> Unit,
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            EditorialSectionHeader(
                eyebrow = "Date notation",
                headline = "How dates read",
                description = "The format used wherever a date appears in the app.",
            )

            Spacer(modifier = Modifier.height(20.dp))

            DateStyle.entries.forEachIndexed { index, style ->
                if (index > 0) {
                    Spacer(modifier = Modifier.height(12.dp))
                }

                DateStyleOption(
                    style = style,
                    isSelected = state.userDateStyle == style,
                    onClick = {
                        runAction(OnDateStyleClickAction(style = style))
                    },
                )
            }
        }
    }

    @Composable
    private fun DateStyleOption(
        style: DateStyle,
        isSelected: Boolean,
        onClick: () -> Unit,
    ) {
        val containerColor = if (isSelected) {
            MaterialTheme.colorScheme.surfaceContainerHigh
        } else {
            MaterialTheme.colorScheme.surfaceContainer
        }

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .noRippleClickable(onClick = onClick),
            color = containerColor,
            shape = RoundedCornerShape(20.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 20.dp,
                        vertical = 18.dp,
                    ),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SelectionIndicator(isSelected = isSelected)

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = style.label,
                    style = MaterialTheme.editorialTypography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }

    @Composable
    private fun SelectionIndicator(isSelected: Boolean) {
        val borderColor = if (isSelected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.outline
        }

        Box(
            modifier = Modifier
                .size(22.dp)
                .border(
                    width = 1.5.dp,
                    color = borderColor,
                    shape = CircleShape,
                ),
            contentAlignment = Alignment.Center,
        ) {
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primary,
                            shape = CircleShape,
                        ),
                )
            }
        }
    }
    // endregion
}

@StandardPreview
@Composable
private fun AppearanceSettingsPreview() {
    SoftcoverTheme {
        AppearanceSettingsScreen().Screen(
            onNavigateBack = {},
            runAction = {},
            state = SettingsScreenUiState(),
        )
    }
}
