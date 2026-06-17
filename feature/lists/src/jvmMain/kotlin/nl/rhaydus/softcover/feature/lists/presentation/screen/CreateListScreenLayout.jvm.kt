package nl.rhaydus.softcover.feature.lists.presentation.screen

import nl.rhaydus.designsystem.component.DesktopBackStrip
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import nl.rhaydus.softcover.core.designsystem.presentation.icon.drawableIconResource
import nl.rhaydus.softcover.feature.lists.presentation.action.CreateListAction
import nl.rhaydus.softcover.feature.lists.presentation.state.CreateListUiState

// Keeps the create-list panel at a readable modal width rather than stretching the field across a
// maximized window.
private val PANEL_MAX_WIDTH = 460.dp

/**
 * Desktop create-list. A centered, modal-style panel (the shared [CreateListForm] inside a bordered
 * [Surface] card) over an opaque background, with a static back strip at the top instead of a
 * scroll-collapsing top bar. The opaque [Surface] background stops the screen beneath from bleeding
 * through during the navigation transition.
 */
@Composable
internal actual fun CreateListScreenLayout(
    state: CreateListUiState,
    runAction: (CreateListAction) -> Unit,
    onNavigateBack: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            DesktopBackStrip(
                onNavigateBack = onNavigateBack,
                backIcon = drawableIconResource(
                    contentDescription = "Navigate back icon",
                    icon = SoftcoverIcon.ArrowBack,
                ),
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 24.dp),
                contentAlignment = Alignment.Center,
            ) {
                Surface(
                    modifier = Modifier.widthIn(max = PANEL_MAX_WIDTH),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant,
                    ),
                ) {
                    CreateListForm(
                        state = state,
                        runAction = runAction,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 28.dp, vertical = 28.dp),
                    )
                }
            }
        }
    }
}
