package nl.rhaydus.softcover.feature.scan.presentation.screen

import android.content.pm.PackageManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import org.koin.compose.koinInject
import nl.rhaydus.softcover.core.designsystem.presentation.component.BarcodeScanner
import nl.rhaydus.softcover.core.designsystem.presentation.component.EditorialSectionHeader
import nl.rhaydus.softcover.core.designsystem.presentation.component.SoftcoverButton
import nl.rhaydus.softcover.core.designsystem.presentation.component.SoftcoverTopBar
import nl.rhaydus.softcover.core.designsystem.presentation.model.BookInitialCover
import nl.rhaydus.softcover.core.designsystem.presentation.model.ButtonSize
import nl.rhaydus.softcover.core.designsystem.presentation.model.ButtonStyle
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

        val context = LocalContext.current

        val hasCamera = remember {
            context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)
        }

        var manualMode by remember { mutableStateOf(hasCamera.not()) }

        var cameraGranted by remember { mutableStateOf(isCameraPermissionGranted(context = context)) }

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

        Screen(
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

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
    @Composable
    private fun Screen(
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

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun ManualEntryContent(
        isResolving: Boolean,
        isAddingBook: Boolean,
        onIsbnSubmit: (String) -> Unit,
    ) {
        val keyboardController = LocalSoftwareKeyboardController.current

        var isbn by remember { mutableStateOf("") }

        val busy = isResolving || isAddingBook

        val canSubmit = isbn.isNotBlank() && busy.not()

        val submit: () -> Unit = {
            if (canSubmit) {
                keyboardController?.hide()

                onIsbnSubmit(isbn)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.Top,
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            EditorialSectionHeader(
                eyebrow = "By ISBN",
                headline = "Enter the ISBN.",
                description = "Type the ISBN-10 or ISBN-13 printed near the barcode.",
            )

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = isbn,
                onValueChange = { isbn = it },
                shape = RoundedCornerShape(8.dp),
                placeholder = { Text(text = "e.g. 9780374710781") },
                enabled = busy.not(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done,
                ),
                keyboardActions = KeyboardActions(onDone = { submit() }),
                colors = OutlinedTextFieldDefaults.colors().copy(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                ),
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(24.dp))

            SoftcoverButton(
                label = if (isResolving) "Looking up" else "Find book",
                style = ButtonStyle.FILLED,
                size = ButtonSize.M,
                enabled = canSubmit,
                onClick = submit,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
