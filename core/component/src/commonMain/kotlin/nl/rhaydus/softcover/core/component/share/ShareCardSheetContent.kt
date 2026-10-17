package nl.rhaydus.softcover.core.component.share

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.snap
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import nl.rhaydus.common.AppLog
import nl.rhaydus.designsystem.haptics.LocalHaptics
import nl.rhaydus.designsystem.motion.playDecorativeMotion
import nl.rhaydus.designsystem.util.SnackBarManager
import nl.rhaydus.softcover.core.component.haptics.Feedback
import nl.rhaydus.softcover.core.component.haptics.play
import nl.rhaydus.softcover.core.component.modifier.rememberShakeTrigger
import nl.rhaydus.softcover.core.component.modifier.shakeOnError
import nl.rhaydus.softcover.core.component.sheet.SheetScaffold
import nl.rhaydus.softcover.core.component.sheet.SheetScaffoldEvent

/**
 * The share sheet every share card goes through: a [SheetScaffold] around a scaled preview of
 * [ShareCardSheetUiModel.card] with the platform's save and share actions as its inline footer.
 *
 * The capture runs here because it needs composition, but progress is reported through [onEvent]:
 * the host folds it into a [ShareCaptureState], passes it back in [ShareCardSheetUiModel.scaffold], and on
 * [ShareCardSheetEvent.Shared] closes the sheet through its [nl.rhaydus.softcover.core.component.sheet.SheetSession].
 * A second capture is ignored until the running one ends; the scaffold's busy state only locks the
 * footer. Outcome snackbars, haptics and the preview's settle or shake are handled here.
 * [aboveCard] and [belowCard] render over and under the preview, in the sheet's centred column; the host
 * emits its own gap after [aboveCard] content.
 */
@Composable
fun ShareCardSheetContent(
    model: ShareCardSheetUiModel,
    onEvent: (ShareCardSheetEvent) -> Unit,
    modifier: Modifier = Modifier,
    aboveCard: @Composable ColumnScope.() -> Unit = {},
    belowCard: @Composable ColumnScope.() -> Unit = {},
) {
    val capture = rememberShareCardCapture(config = softcoverShareCardCaptureConfig)
    val coroutineScope = rememberCoroutineScope()
    val currentOnEvent by rememberUpdatedState(onEvent)
    val haptics = LocalHaptics.current
    val playMotion = playDecorativeMotion()
    val shake = rememberShakeTrigger()
    val settleScale = remember { Animatable(initialValue = 1f) }
    val settleSpec = MaterialTheme.motionScheme.defaultSpatialSpec<Float>()

    fun succeed() {
        haptics.play(Feedback.Routine)

        if (playMotion.not()) return

        coroutineScope.launch {
            settleScale.animateTo(
                targetValue = SETTLE_SCALE,
                animationSpec = settleSpec,
            )

            settleScale.animateTo(
                targetValue = 1f,
                animationSpec = settleSpec,
            )
        }
    }

    fun runCapture(
        started: ShareCardSheetEvent,
        failureMessage: String,
        block: suspend () -> Unit,
    ) {
        currentOnEvent(started)

        coroutineScope.launch {
            try {
                runCatching { block() }.onFailure { error ->
                    if (error is CancellationException) throw error

                    AppLog.e("Share capture failed: $error")

                    SnackBarManager.showSnackbar(title = failureMessage)

                    shake.shake()
                }
            } finally {
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
                    failureMessage = "Couldn't share. Try again.",
                ) {
                    when (val outcome = capture.share(displayName = model.displayName)) {
                        is ShareOutcome.Shared -> {
                            succeed()

                            currentOnEvent(ShareCardSheetEvent.Shared)
                        }

                        is ShareOutcome.CopiedToClipboard -> {
                            succeed()

                            SnackBarManager.showSnackbar(title = "Copied to clipboard")

                            currentOnEvent(ShareCardSheetEvent.Shared)
                        }

                        is ShareOutcome.Cancelled -> Unit

                        is ShareOutcome.Failure -> {
                            AppLog.e("Failed to share card: ${outcome.reason}")

                            SnackBarManager.showSnackbar(title = "Couldn't share. Try again.")

                            shake.shake()
                        }
                    }
                }

                SheetScaffoldEvent.SecondaryClicked -> runCapture(
                    started = ShareCardSheetEvent.SaveStarted,
                    failureMessage = shareCaptureCopy.saveFailedMessage,
                ) {
                    when (val outcome = capture.saveToGallery(displayName = model.displayName)) {
                        is SaveOutcome.Saved -> {
                            succeed()

                            SnackBarManager.showSnackbar(title = shareCaptureCopy.savedMessage)
                        }

                        is SaveOutcome.Cancelled -> Unit

                        is SaveOutcome.Cached -> {
                            SnackBarManager.showSnackbar(title = shareCaptureCopy.saveFailedMessage)

                            shake.shake()
                        }

                        is SaveOutcome.Failure -> {
                            AppLog.e("Failed to save card: ${outcome.reason}")

                            SnackBarManager.showSnackbar(title = shareCaptureCopy.saveFailedMessage)

                            shake.shake()
                        }
                    }
                }

                SheetScaffoldEvent.HeaderActionClicked -> Unit
            }
        },
        modifier = modifier,
    ) {
        ShareCardSheetBody(
            model = model,
            capture = capture,
            previewModifier = Modifier
                .shakeOnError(trigger = shake)
                .graphicsLayer {
                    scaleX = settleScale.value
                    scaleY = settleScale.value
                },
            aboveCard = aboveCard,
            belowCard = belowCard,
        )
    }
}

/** The sheet's body without the modal container or the capture effects, so the gallery can host it inline. */
@Composable
internal fun ShareCardSheetBody(
    model: ShareCardSheetUiModel,
    capture: ShareCardCapture,
    modifier: Modifier = Modifier,
    previewModifier: Modifier = Modifier,
    aboveCard: @Composable ColumnScope.() -> Unit = {},
    belowCard: @Composable ColumnScope.() -> Unit = {},
) {
    val maxHeight by animateDpAsState(
        targetValue = model.previewMaxHeight,
        animationSpec = if (playDecorativeMotion()) {
            MaterialTheme.motionScheme.defaultSpatialSpec()
        } else {
            snap()
        },
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        aboveCard()

        ScaledShareCardPreview(
            capture = capture,
            maxWidth = model.previewMaxWidth,
            maxHeight = maxHeight,
            modifier = previewModifier,
        ) {
            ShareCard(model = model.card)
        }

        belowCard()
    }
}

private const val SETTLE_SCALE = 0.97f
