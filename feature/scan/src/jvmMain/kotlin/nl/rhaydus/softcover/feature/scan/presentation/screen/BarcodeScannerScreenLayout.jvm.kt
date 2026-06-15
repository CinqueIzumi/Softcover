package nl.rhaydus.softcover.feature.scan.presentation.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.model.SoftcoverIconResource
import nl.rhaydus.softcover.core.designsystem.presentation.modifier.pointerHandCursor
import nl.rhaydus.softcover.feature.scan.presentation.state.ScanUiState

// Keeps the ISBN panel at a readable modal width rather than stretching the field across a maximized
// window.
private val PANEL_MAX_WIDTH = 460.dp

/**
 * Desktop barcode scanner. Desktop has no camera (the permission stub reports none), so this opens
 * straight into the shared manual ISBN form — the `manualMode` / `cameraGranted` / `onEnterManually`
 * params (the camera branches) are unused here. A centered, modal-style panel matching desktop Create
 * List: a static back strip over an opaque background, with the focused form inside a bordered card.
 */
@Composable
internal actual fun BarcodeScannerScreenLayout(
    state: ScanUiState,
    manualMode: Boolean,
    cameraGranted: Boolean,
    onIsbnSubmit: (String) -> Unit,
    onEnterManually: () -> Unit,
    onNavigateBack: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            DesktopBackStrip(onNavigateBack = onNavigateBack)

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
                    ManualEntryContent(
                        isResolving = state.isResolving,
                        isAddingBook = state.isAddingBook,
                        onIsbnSubmit = onIsbnSubmit,
                        autoFocus = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 28.dp, vertical = 28.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun DesktopBackStrip(onNavigateBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(
            onClick = onNavigateBack,
            modifier = Modifier.pointerHandCursor(),
        ) {
            val backIcon = SoftcoverIconResource.Drawable(
                icon = SoftcoverIcon.ArrowBack,
                contentDescription = "Navigate back icon",
            )

            Icon(
                painter = backIcon.getIconPainter(),
                contentDescription = backIcon.contentDescription,
            )
        }
    }
}
