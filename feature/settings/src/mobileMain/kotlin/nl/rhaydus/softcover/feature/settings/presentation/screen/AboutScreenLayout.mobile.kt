package nl.rhaydus.softcover.feature.settings.presentation.screen

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.layout.cappedContentWidth
import nl.rhaydus.designsystem.theme.StandardPreview
import nl.rhaydus.softcover.core.designsystem.presentation.component.SoftcoverTopBar
import nl.rhaydus.softcover.core.designsystem.presentation.theme.SoftcoverTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal actual fun AboutScreenLayout(
    versionName: String,
    versionCode: Int,
    openUrl: (String) -> Unit,
    onNavigateBack: () -> Unit,
    onRoadmapClick: () -> Unit,
) {
    Scaffold(
        topBar = {
            SoftcoverTopBar(
                title = "About",
                onNavigateBack = onNavigateBack,
            )
        },
    ) { innerPadding ->
        AboutContent(
            versionName = versionName,
            versionCode = versionCode,
            openUrl = openUrl,
            onRoadmapClick = onRoadmapClick,
            modifier = Modifier
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .cappedContentWidth()
                .padding(
                    horizontal = 24.dp,
                    vertical = 16.dp,
                ),
        )
    }
}

@StandardPreview
@Composable
private fun AboutScreenPreview() {
    SoftcoverTheme {
        AboutScreenLayout(
            versionName = "1.0.0",
            versionCode = 1,
            openUrl = {},
            onNavigateBack = {},
            onRoadmapClick = {},
        )
    }
}
