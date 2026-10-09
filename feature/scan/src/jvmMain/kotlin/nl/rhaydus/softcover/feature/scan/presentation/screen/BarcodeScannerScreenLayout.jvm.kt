package nl.rhaydus.softcover.feature.scan.presentation.screen

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
import nl.rhaydus.softcover.core.component.field.TextFieldEvent
import nl.rhaydus.softcover.core.component.topbar.EditorialTopBar
import nl.rhaydus.softcover.core.component.topbar.TopBarEvent
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import nl.rhaydus.softcover.feature.scan.presentation.state.ScanUiState

/**
 * Desktop barcode scanner. Desktop has no camera (the permission stub reports none), so this opens
 * straight into the shared manual ISBN form — the `manualMode` / `cameraGranted` / `onEnterManually`
 * params (the camera branches) are unused here. A centered, modal-style panel matching desktop Create
 * List: an editorial top bar over an opaque background, with the focused form inside a bordered card.
 */
@Composable
internal actual fun BarcodeScannerScreenLayout(
    state: ScanUiState,
    manualMode: Boolean,
    cameraGranted: Boolean,
    onIsbnSubmit: (String) -> Unit,
    onIsbnFieldEvent: (TextFieldEvent) -> Unit,
    onEnterManually: () -> Unit,
    onNavigateBack: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            EditorialTopBar(
                model = state.topBar,
                onEvent = { event ->
                    when (event) {
                        TopBarEvent.BackClicked -> onNavigateBack()
                    }
                },
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.gutter, vertical = Spacing.xl),
                contentAlignment = Alignment.Center,
            ) {
                Surface(
                    modifier = Modifier.widthIn(max = 460.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant,
                    ),
                ) {
                    ManualEntryContent(
                        field = state.isbnField,
                        isResolving = state.isResolving,
                        isAddingBook = state.isAddingBook,
                        header = state.manualEntryHeader,
                        onFieldEvent = onIsbnFieldEvent,
                        onIsbnSubmit = onIsbnSubmit,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Spacing.xxl, vertical = Spacing.xxl),
                    )
                }
            }
        }
    }
}
