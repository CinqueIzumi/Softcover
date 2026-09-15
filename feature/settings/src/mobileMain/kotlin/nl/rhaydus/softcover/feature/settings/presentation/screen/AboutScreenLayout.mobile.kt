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
import nl.rhaydus.softcover.core.component.topbar.TopBar
import nl.rhaydus.softcover.core.component.topbar.TopBarEvent
import nl.rhaydus.softcover.core.component.topbar.TopBarNavigation
import nl.rhaydus.softcover.core.component.topbar.TopBarUiModel
import nl.rhaydus.softcover.core.designsystem.presentation.theme.SoftcoverTheme

/** The bar is fixed for this screen, so the model is a constant rather than rebuilt per frame. */
private val ABOUT_TOP_BAR = TopBarUiModel(
    title = "About",
    navigation = TopBarNavigation.Back,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal actual fun AboutScreenLayout(
    versionName: String,
    versionCode: Int,
    openUrl: (String) -> Unit,
    onNavigateBack: () -> Unit,
    onRoadmapClick: () -> Unit,
    onComponentGalleryUnlocked: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopBar(
                model = ABOUT_TOP_BAR,
                onEvent = { event ->
                    when (event) {
                        TopBarEvent.BackClicked -> onNavigateBack()
                    }
                },
            )
        },
    ) { innerPadding ->
        AboutContent(
            versionName = versionName,
            versionCode = versionCode,
            openUrl = openUrl,
            onRoadmapClick = onRoadmapClick,
            onComponentGalleryUnlocked = onComponentGalleryUnlocked,
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
            onComponentGalleryUnlocked = {},
        )
    }
}
