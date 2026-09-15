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
import nl.rhaydus.designsystem.component.RhaydusButton
import nl.rhaydus.designsystem.model.ButtonSize
import nl.rhaydus.designsystem.model.ButtonStyle
import nl.rhaydus.softcover.core.component.topbar.TopBar
import nl.rhaydus.softcover.core.component.topbar.TopBarEvent
import nl.rhaydus.softcover.core.component.topbar.TopBarNavigation
import nl.rhaydus.softcover.core.component.topbar.TopBarUiModel
import nl.rhaydus.softcover.feature.scan.presentation.component.BarcodeScanner
import nl.rhaydus.softcover.feature.scan.presentation.state.ScanUiState

/** The bar is fixed for this screen, so the model is a constant rather than rebuilt per frame. */
private val SCANNER_TOP_BAR = TopBarUiModel(
    title = "Scan a barcode",
    navigation = TopBarNavigation.Back,
)

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
            TopBar(
                model = SCANNER_TOP_BAR,
                onEvent = { event ->
                    when (event) {
                        TopBarEvent.BackClicked -> onNavigateBack()
                    }
                },
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

        RhaydusButton(
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
