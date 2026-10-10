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
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import nl.rhaydus.common.AppLog
import nl.rhaydus.designsystem.share.SaveOutcome
import nl.rhaydus.designsystem.share.ShareOutcome
import nl.rhaydus.designsystem.share.rememberShareCardCapture
import nl.rhaydus.designsystem.util.SnackBarManager
import nl.rhaydus.softcover.core.component.sheet.SheetScaffold
import nl.rhaydus.softcover.core.component.sheet.SheetScaffoldEvent
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing

/**
 * The share sheet every share card goes through: a [SheetScaffold] around a scaled preview of
 * [ShareCardSheetUiModel.card] with "Save to gallery" and "Share" as its inline footer.
 *
 * The capture runs here because it needs composition, but progress is reported through [onEvent]:
 * the host holds the capture progress and close state and passes it back in [ShareCardSheetUiModel.scaffold].
 * A second capture is ignored until the running one ends; the scaffold's busy state only locks the
 * footer. Outcome snackbars are shown here.
 * [aboveCard] renders over the preview, in the sheet's centred column.
 */
@Composable
fun ShareCardSheetContent(
    model: ShareCardSheetUiModel,
    onEvent: (ShareCardSheetEvent) -> Unit,
    modifier: Modifier = Modifier,
    aboveCard: @Composable ColumnScope.() -> Unit = {},
) {
    val capture = rememberShareCardCapture(config = softcoverShareCardCaptureConfig)
    val coroutineScope = rememberCoroutineScope()
    val currentOnEvent by rememberUpdatedState(onEvent)
    var isCaptureRunning by remember { mutableStateOf(false) }

    fun runCapture(
        started: ShareCardSheetEvent,
        failureMessage: String,
        block: suspend () -> Unit,
    ) {
        if (isCaptureRunning) return

        isCaptureRunning = true

        currentOnEvent(started)

        coroutineScope.launch {
            try {
                runCatching { block() }.onFailure { error ->
                    if (error is CancellationException) throw error

                    AppLog.e("Share capture failed: $error")

                    SnackBarManager.showSnackbar(title = failureMessage)
                }
            } finally {
                isCaptureRunning = false

                currentOnEvent(ShareCardSheetEvent.CaptureFinished)
            }
        }
    }

    SheetScaffold(
        model = model.scaffold,
        onEvent = { event ->
            when (event) {
                SheetScaffoldEvent.Dismissed -> onEvent(ShareCardSheetEvent.Dismissed)

                SheetScaffoldEvent.PrimaryClicked -> runCapture(
                    started = ShareCardSheetEvent.ShareStarted,
                    failureMessage = "Couldn't share — try again",
                ) {
                    when (val outcome = capture.share(displayName = model.displayName)) {
                        is ShareOutcome.Shared -> currentOnEvent(ShareCardSheetEvent.Shared)

                        is ShareOutcome.Cancelled -> Unit

                        is ShareOutcome.Failure -> {
                            AppLog.e("Failed to share card: ${outcome.reason}")

                            SnackBarManager.showSnackbar(title = "Couldn't share — try again")
                        }
                    }
                }

                SheetScaffoldEvent.SecondaryClicked -> runCapture(
                    started = ShareCardSheetEvent.SaveStarted,
                    failureMessage = "Couldn't save to gallery",
                ) {
                    val outcome = capture.saveToGallery(displayName = model.displayName)

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
                maxWidth = model.previewMaxWidth,
                maxHeight = model.previewMaxHeight,
            ) {
                ShareCard(model = model.card)
            }
        }
    }
}
