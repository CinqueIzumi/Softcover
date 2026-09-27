package nl.rhaydus.softcover.feature.settings.presentation.screen.section

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.component.DesktopVerticalScrollbar
import nl.rhaydus.designsystem.layout.cappedContentWidth
import nl.rhaydus.designsystem.layout.rememberBottomBarPadding
import nl.rhaydus.softcover.core.component.header.PageMasthead
import nl.rhaydus.softcover.core.component.header.PageMastheadUiModel
import nl.rhaydus.softcover.core.domain.model.AppUpdateState
import nl.rhaydus.softcover.feature.settings.presentation.screen.AboutContent

/**
 * The master–detail `About` category: [AboutContent] (Credits/Source/Contact, closing with its own
 * `VersionFooter`), then the app-update card and the debug section. [AboutContent] is the app's one and
 * only place the version shows — the sidebar's own copy was dropped so it isn't on screen twice at once
 * alongside this pane — so this doesn't render a second, separate `VersionFooter` of its own.
 * [onComponentGalleryUnlocked] is threaded straight through to that `VersionFooter` (via
 * [AboutContent]'s own parameter of the same name) — see its KDoc for the seven-tap gesture itself.
 */
@Composable
internal fun AboutPane(
    masthead: PageMastheadUiModel,
    versionName: String,
    versionCode: Int,
    appUpdateState: AppUpdateState,
    onStartAppUpdate: () -> Unit,
    openUrl: (String) -> Unit,
    onRoadmapClick: () -> Unit,
    onComponentGalleryUnlocked: () -> Unit,
    debugSection: @Composable () -> Unit,
) {
    val scrollState = rememberScrollState()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(
                    top = 24.dp,
                    bottom = 24.dp + rememberBottomBarPadding(),
                ),
        ) {
            Column(
                modifier = Modifier
                    .cappedContentWidth()
                    .padding(horizontal = 32.dp),
            ) {
                PageMasthead(model = masthead)

                Spacer(modifier = Modifier.height(28.dp))

                AboutContent(
                    versionName = versionName,
                    versionCode = versionCode,
                    openUrl = openUrl,
                    onRoadmapClick = onRoadmapClick,
                    onComponentGalleryUnlocked = onComponentGalleryUnlocked,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(40.dp))

                if (appUpdateState != AppUpdateState.Idle) {
                    AppUpdateSection(
                        appUpdateState = appUpdateState,
                        onClick = onStartAppUpdate,
                    )

                    Spacer(modifier = Modifier.height(40.dp))
                }

                debugSection()
            }
        }

        DesktopVerticalScrollbar(
            scrollState = scrollState,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .fillMaxHeight()
                .padding(vertical = 4.dp),
        )
    }
}
