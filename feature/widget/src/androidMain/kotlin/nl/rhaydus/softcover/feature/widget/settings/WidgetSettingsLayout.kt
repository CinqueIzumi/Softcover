package nl.rhaydus.softcover.feature.widget.settings

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import nl.rhaydus.softcover.core.component.topbar.TopBar
import nl.rhaydus.softcover.core.component.topbar.TopBarEvent
import nl.rhaydus.softcover.core.component.topbar.TopBarUiModel
import nl.rhaydus.softcover.core.designsystem.presentation.theme.SoftcoverTheme
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import nl.rhaydus.softcover.core.domain.model.ThemeConfiguration
import nl.rhaydus.softcover.core.uibinding.theme.toSchemeBrightness
import nl.rhaydus.softcover.core.uibinding.theme.toSpinePalette

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun WidgetSettingsLayout(
    themeConfiguration: ThemeConfiguration,
    topBar: TopBarUiModel,
    onBack: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    SoftcoverTheme(
        palette = themeConfiguration.colorPalette.toSpinePalette(),
        brightness = themeConfiguration.themeMode.toSchemeBrightness(systemInDark = isSystemInDarkTheme()),
        dynamicColor = themeConfiguration.useDynamicColor,
    ) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                TopBar(
                    model = topBar,
                    onEvent = { event ->
                        when (event) {
                            TopBarEvent.BackClicked -> onBack()
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
                    .padding(
                        horizontal = Spacing.gutter,
                        vertical = Spacing.lg,
                    ),
                verticalArrangement = Arrangement.spacedBy(Spacing.md),
                content = content,
            )
        }
    }
}
