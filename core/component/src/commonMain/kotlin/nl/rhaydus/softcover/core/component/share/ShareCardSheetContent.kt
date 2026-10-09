package nl.rhaydus.softcover.core.component.share

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import nl.rhaydus.common.AppLog
import nl.rhaydus.designsystem.share.SaveOutcome
import nl.rhaydus.designsystem.share.ShareOutcome
import nl.rhaydus.designsystem.share.rememberShareCardCapture
import nl.rhaydus.designsystem.util.SnackBarManager
import nl.rhaydus.softcover.core.component.sheet.SheetFooterButton
import nl.rhaydus.softcover.core.component.sheet.SheetFooterUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetScaffold
import nl.rhaydus.softcover.core.component.sheet.SheetScaffoldEvent
import nl.rhaydus.softcover.core.component.sheet.SheetScaffoldUiModel
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing

/**
 * The share sheet every share card goes through: a [SheetScaffold] around a scaled preview of [card]
 * with "Save to gallery" and "Share" as its inline footer.
 *
 * Capture, the busy state and the outcome snackbars live here, not in the host. While a capture runs
 * the scaffold is busy, so both buttons lock and the sheet cannot be dismissed; a successful share
 * closes the sheet. [displayName] names the captured file. [aboveCard] renders over the preview, in
 * the sheet's centred column.
 */
@Composable
fun ShareCardSheetContent(
    card: ShareCardUiModel,
    scaffold: SheetScaffoldUiModel,
    displayName: String,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    previewMaxWidth: Dp = 280.dp,
    previewMaxHeight: Dp = 360.dp,
    aboveCard: @Composable ColumnScope.() -> Unit = {},
) {
    val capture = rememberShareCardCapture(config = softcoverShareCardCaptureConfig)
    val coroutineScope = rememberCoroutineScope()

    var isSharing by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    var closeRequested by remember { mutableStateOf(false) }
    val isBusy = isSharing || isSaving

    fun runCapture(
        failureMessage: String,
        setRunning: (Boolean) -> Unit,
        block: suspend () -> Unit,
    ) {
        if (isBusy) return

        setRunning(true)

        coroutineScope.launch {
            try {
                runCatching { block() }.onFailure { error ->
                    if (error is CancellationException) throw error

                    AppLog.e("Share capture failed: $error")

                    SnackBarManager.showSnackbar(title = failureMessage)
                }
            } finally {
                setRunning(false)
            }
        }
    }

    SheetScaffold(
        model = scaffold.copy(
            footer = SheetFooterUiModel.Inline(
                primary = SheetFooterButton(label = if (isSharing) "Sharing…" else "Share"),
                secondary = SheetFooterButton(label = if (isSaving) "Saving to gallery…" else "Save to gallery"),
            ),
            busy = isBusy,
            closeRequested = closeRequested,
        ),
        onEvent = { event ->
            when (event) {
                SheetScaffoldEvent.Dismissed -> onDismissRequest()

                SheetScaffoldEvent.PrimaryClicked -> runCapture(
                    failureMessage = "Couldn't share — try again",
                    setRunning = { isSharing = it },
                ) {
                    when (val outcome = capture.share(displayName = displayName)) {
                        is ShareOutcome.Shared -> closeRequested = true

                        is ShareOutcome.Cancelled -> Unit

                        is ShareOutcome.Failure -> {
                            AppLog.e("Failed to share card: ${outcome.reason}")

                            SnackBarManager.showSnackbar(title = "Couldn't share — try again")
                        }
                    }
                }

                SheetScaffoldEvent.SecondaryClicked -> runCapture(
                    failureMessage = "Couldn't save to gallery",
                    setRunning = { isSaving = it },
                ) {
                    val outcome = capture.saveToGallery(displayName = displayName)

                    val message = if (outcome is SaveOutcome.Saved) "Saved to gallery" else "Couldn't save to gallery"

                    SnackBarManager.showSnackbar(title = message)
                }

                SheetScaffoldEvent.HeaderActionClicked -> Unit
            }
        },
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            aboveCard()

            Spacer(modifier = Modifier.height(Spacing.lg))

            ScaledShareCardPreview(
                capture = capture,
                maxWidth = previewMaxWidth,
                maxHeight = previewMaxHeight,
            ) {
                ShareCard(model = card)
            }
        }
    }
}
