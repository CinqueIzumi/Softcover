package nl.rhaydus.softcover.feature.scan.presentation.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import org.koin.compose.koinInject
import nl.rhaydus.softcover.core.designsystem.presentation.model.BookInitialCover
import nl.rhaydus.softcover.core.designsystem.presentation.navigation.AppNavigator
import nl.rhaydus.softcover.core.designsystem.presentation.navigation.ScreenDestination
import nl.rhaydus.softcover.core.designsystem.presentation.util.ObserveAsEvents
import nl.rhaydus.softcover.core.designsystem.presentation.util.SnackBarManager
import nl.rhaydus.softcover.feature.scan.presentation.action.OnAddUnknownIsbnConfirmedAction
import nl.rhaydus.softcover.feature.scan.presentation.action.OnAddUnknownIsbnDismissedAction
import nl.rhaydus.softcover.feature.scan.presentation.action.OnIsbnSubmittedAction
import nl.rhaydus.softcover.feature.scan.presentation.component.UnknownIsbnSheet
import nl.rhaydus.softcover.feature.scan.presentation.event.AddBookFailedEvent
import nl.rhaydus.softcover.feature.scan.presentation.event.BookResolvedEvent
import nl.rhaydus.softcover.feature.scan.presentation.event.InvalidIsbnEvent
import nl.rhaydus.softcover.feature.scan.presentation.event.ResolutionFailedEvent
import nl.rhaydus.softcover.feature.scan.presentation.permission.isCameraAvailable
import nl.rhaydus.softcover.feature.scan.presentation.permission.isCameraPermissionGranted
import nl.rhaydus.softcover.feature.scan.presentation.permission.rememberCameraPermissionRequester
import nl.rhaydus.softcover.feature.scan.presentation.screenmodel.ScanScreenModel
import nl.rhaydus.softcover.feature.scan.presentation.state.ScanUiState

class BarcodeScannerScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        val appNavigator = koinInject<AppNavigator>()

        val screenModel = koinScreenModel<ScanScreenModel>()

        val state by screenModel.state.collectAsStateWithLifecycle()

        val hasCamera = isCameraAvailable()

        var manualMode by remember { mutableStateOf(hasCamera.not()) }

        val initialCameraGranted = isCameraPermissionGranted()

        var cameraGranted by remember { mutableStateOf(initialCameraGranted) }

        val permissionRequester = rememberCameraPermissionRequester { granted ->
            cameraGranted = granted

            if (granted.not()) manualMode = true
        }

        LaunchedEffect(Unit) {
            if (hasCamera && cameraGranted.not()) permissionRequester.request()
        }

        ObserveAsEvents(flow = screenModel.events) { event ->
            when (event) {
                is BookResolvedEvent -> {
                    val scannedEdition = event.book.editions.firstOrNull { it.id == event.editionId }

                    navigator.pop()

                    navigator.push(
                        item = appNavigator.screen(
                            ScreenDestination.BookDetail(
                                id = event.book.id,
                                initialCover = BookInitialCover
                                    .fromBook(book = event.book)
                                    .copy(
                                        currentEdition = scannedEdition ?: event.book.currentEdition,
                                        scannedEditionId = event.editionId,
                                    ),
                            ),
                        ),
                    )
                }

                is InvalidIsbnEvent -> SnackBarManager.showSnackbar(title = "That doesn't look like a valid ISBN.")
                is ResolutionFailedEvent -> SnackBarManager.showSnackbar(title = "Couldn't look that up — try again.")
                is AddBookFailedEvent -> SnackBarManager.showSnackbar(title = "Couldn't add it — try again.")
            }
        }

        BarcodeScannerScreenLayout(
            state = state,
            manualMode = manualMode,
            cameraGranted = cameraGranted,
            onIsbnSubmit = { isbn -> screenModel.runAction(OnIsbnSubmittedAction(isbn = isbn)) },
            onEnterManually = { manualMode = true },
            onNavigateBack = navigator::pop,
        )

        state.unknownIsbn?.let { unknownIsbn ->
            UnknownIsbnSheet(
                isbn = unknownIsbn,
                isAdding = state.isAddingBook,
                onConfirm = { screenModel.runAction(OnAddUnknownIsbnConfirmedAction()) },
                onDismiss = { screenModel.runAction(OnAddUnknownIsbnDismissedAction()) },
            )
        }
    }
}

// The mobile actual keeps today's camera-first Scaffold (scanner / manual / awaiting-permission
// branches); the desktop actual ignores the camera-related params and opens straight into a centered
// manual-ISBN panel (desktop has no camera — the permission stub reports none). Both render the shared
// [ManualEntryContent]. No default arguments — they are not allowed on an expect declaration, so every
// argument is supplied explicitly at the single call site above.
@Composable
internal expect fun BarcodeScannerScreenLayout(
    state: ScanUiState,
    manualMode: Boolean,
    cameraGranted: Boolean,
    onIsbnSubmit: (String) -> Unit,
    onEnterManually: () -> Unit,
    onNavigateBack: () -> Unit,
)
