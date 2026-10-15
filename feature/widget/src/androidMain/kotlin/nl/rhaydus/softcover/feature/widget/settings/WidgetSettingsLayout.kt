package nl.rhaydus.softcover.feature.widget.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import nl.rhaydus.softcover.core.designsystem.presentation.theme.SoftcoverTheme
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import nl.rhaydus.softcover.core.domain.model.ThemeConfiguration
import nl.rhaydus.softcover.core.presentation.theme.isDark
import nl.rhaydus.softcover.core.uibinding.theme.toSpinePalette

@Composable
internal fun WidgetSettingsLayout(
    themeConfiguration: ThemeConfiguration,
    content: @Composable ColumnScope.() -> Unit,
) {
    SoftcoverTheme(
        palette = themeConfiguration.colorPalette.toSpinePalette(),
        darkTheme = themeConfiguration.themeMode.isDark(),
        dynamicColor = themeConfiguration.useDynamicColor,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .systemBarsPadding()
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
