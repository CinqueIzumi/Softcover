package nl.rhaydus.softcover.feature.scan.presentation.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import nl.rhaydus.softcover.core.designsystem.presentation.component.BarcodeScanner
import nl.rhaydus.softcover.core.designsystem.presentation.component.SoftcoverButton
import nl.rhaydus.softcover.core.designsystem.presentation.component.SoftcoverTopBar
import nl.rhaydus.softcover.core.designsystem.presentation.model.ButtonSize
import nl.rhaydus.softcover.core.designsystem.presentation.model.ButtonStyle
import nl.rhaydus.softcover.feature.scan.presentation.state.ScanUiState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal actual fun BarcodeScannerScreenLayout(
    state: ScanUiState,
    manualMode: Boolean,
    cameraGranted: Boolean,
    onIsbnSubmit: (String) -> Unit,
    onEnterManually: () -> Unit,
    onNavigateBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            SoftcoverTopBar(
                title = "Scan a barcode",
                onNavigateBack = onNavigateBack,
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
        ) {
            when {
                manualMode -> ManualEntryContent(
                    isResolving = state.isResolving,
                    isAddingBook = state.isAddingBook,
                    onIsbnSubmit = onIsbnSubmit,
                    autoFocus = false,
                    modifier = Modifier
                        .fillMaxSize()
                        .imePadding()
                        .padding(horizontal = 24.dp),
                )

                cameraGranted -> ScannerContent(
                    cameraPaused = state.isResolving || state.unknownIsbn != null,
                    onIsbnSubmit = onIsbnSubmit,
                    onEnterManually = onEnterManually,
                )

                else -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    ContainedLoadingIndicator()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ScannerContent(
    cameraPaused: Boolean,
    onIsbnSubmit: (String) -> Unit,
    onEnterManually: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        if (cameraPaused) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                ContainedLoadingIndicator()
            }
        } else {
            BarcodeScanner(
                onIsbnDetected = onIsbnSubmit,
                modifier = Modifier.fillMaxSize(),
            )
        }

        SoftcoverButton(
            label = "Enter ISBN manually",
            style = ButtonStyle.TEXT,
            size = ButtonSize.M,
            onClick = onEnterManually,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp),
        )
    }
}
