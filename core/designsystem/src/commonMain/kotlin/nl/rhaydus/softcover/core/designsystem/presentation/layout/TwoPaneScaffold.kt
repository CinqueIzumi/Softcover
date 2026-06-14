package nl.rhaydus.softcover.core.designsystem.presentation.layout

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * A fixed-leading-pane / flexible-trailing-pane split. Pure layout — it knows nothing about
 * navigation, screen models, or what either pane renders, so it backs any list-detail spread on a
 * large window (the canonical use is a library/reading shelf in [list] beside a book detail in
 * [detail]).
 *
 * The [list] pane takes a fixed [listPaneWidth]; the [detail] pane takes the rest. A [divider]
 * separates them by default.
 */
@Composable
fun TwoPaneScaffold(
    list: @Composable () -> Unit,
    detail: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    listPaneWidth: Dp = 360.dp,
    divider: @Composable () -> Unit = { VerticalDivider() },
) {
    Row(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(listPaneWidth),
        ) {
            list()
        }

        divider()

        Box(
            modifier = Modifier
                .fillMaxHeight()
                .weight(1f),
        ) {
            detail()
        }
    }
}
