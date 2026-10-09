package nl.rhaydus.softcover.core.component.layout

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import nl.rhaydus.designsystem.component.DesktopVerticalScrollbar
import nl.rhaydus.designsystem.layout.cappedContentWidth
import nl.rhaydus.designsystem.layout.rememberBottomBarPadding
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing

/**
 * The desktop scroll page: slots for an optional [topBar], [masthead] and docked [bottomBar] around a
 * scroll area whose content is held to the reading measure while its scrollbar stays at the window edge.
 */
@Composable
fun DesktopScrollPage(
    modifier: Modifier = Modifier,
    topBar: (@Composable () -> Unit)? = null,
    masthead: (@Composable () -> Unit)? = null,
    bottomBar: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val scrollState = rememberScrollState()
    val bottomBarPadding = rememberBottomBarPadding()

    Column(modifier = modifier.fillMaxSize()) {
        if (topBar != null) {
            topBar()
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(
                        top = Spacing.xl,
                        bottom = if (bottomBar == null) Spacing.xl + bottomBarPadding else Spacing.xl,
                    ),
            ) {
                Column(
                    modifier = Modifier
                        .cappedContentWidth()
                        .padding(horizontal = Spacing.xxxl),
                ) {
                    if (masthead != null) {
                        masthead()

                        Spacer(modifier = Modifier.height(Spacing.xxl))
                    }

                    content()
                }
            }

            DesktopVerticalScrollbar(
                scrollState = scrollState,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .fillMaxHeight()
                    .padding(vertical = Spacing.xxs),
            )
        }

        if (bottomBar != null) {
            Box(modifier = Modifier.padding(bottom = bottomBarPadding)) {
                bottomBar()
            }
        }
    }
}
