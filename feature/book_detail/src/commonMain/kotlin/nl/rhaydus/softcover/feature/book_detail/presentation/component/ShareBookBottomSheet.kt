package nl.rhaydus.softcover.feature.book_detail.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import nl.rhaydus.common.AppLog
import nl.rhaydus.designsystem.component.LocalModalSheetDismiss
import nl.rhaydus.designsystem.component.RhaydusButton
import nl.rhaydus.designsystem.model.ButtonSize
import nl.rhaydus.designsystem.model.ButtonStyle
import nl.rhaydus.designsystem.share.SaveOutcome
import nl.rhaydus.designsystem.share.ShareCardCapture
import nl.rhaydus.designsystem.share.ShareOutcome
import nl.rhaydus.designsystem.share.rememberShareCardCapture
import nl.rhaydus.designsystem.util.SnackBarManager
import nl.rhaydus.softcover.core.component.share.BookShareCardUiModel
import nl.rhaydus.softcover.core.component.share.ReadingUpdateShareCardUiModel
import nl.rhaydus.softcover.core.component.share.ScaledShareCardPreview
import nl.rhaydus.softcover.core.component.share.ShareCard
import nl.rhaydus.softcover.core.component.share.ShareCardUiModel
import nl.rhaydus.softcover.core.component.share.softcoverShareCardCaptureConfig
import nl.rhaydus.softcover.core.component.sheet.SheetScaffold
import nl.rhaydus.softcover.core.component.sheet.SheetScaffoldEvent
import nl.rhaydus.softcover.core.component.sheet.SheetScaffoldUiModel
import nl.rhaydus.softcover.core.component.text.SoftcoverText
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.state.ShareCardVariant
import nl.rhaydus.softcover.feature.book_detail.presentation.state.ShareSheetBodyUiModel

@Composable
internal fun ShareBookBottomSheet(
    model: SheetScaffoldUiModel,
    shareBookCard: BookShareCardUiModel,
    shareUpdateCard: ReadingUpdateShareCardUiModel?,
    body: ShareSheetBodyUiModel,
    onVariantSelected: (ShareCardVariant) -> Unit,
    captureEvents: Flow<BookDetailEvent.ShareCaptureEvent>,
    onShareClick: () -> Unit,
    onShareFinished: () -> Unit,
    onSaveToGalleryClick: () -> Unit,
    onSaveToGalleryFinished: () -> Unit,
    onDismissRequest: () -> Unit,
) {
    val capture = rememberShareCardCapture(config = softcoverShareCardCaptureConfig)

    val displayedContent: ShareCardUiModel = when (body.variant) {
        ShareCardVariant.MY_UPDATE -> shareUpdateCard ?: shareBookCard
        ShareCardVariant.BOOK -> shareBookCard
    }

    SheetScaffold(
        model = model,
        onEvent = { event ->
            when (event) {
                SheetScaffoldEvent.Dismissed -> onDismissRequest()

                SheetScaffoldEvent.PrimaryClicked,
                SheetScaffoldEvent.SecondaryClicked,
                SheetScaffoldEvent.HeaderActionClicked -> Unit
            }
        },
    ) {
        val dismiss = LocalModalSheetDismiss.current

        val currentCapture by rememberUpdatedState(capture)
        val currentTitle by rememberUpdatedState(shareBookCard.title)
        val currentDismiss by rememberUpdatedState(dismiss)
        val currentOnShareFinished by rememberUpdatedState(onShareFinished)
        val currentOnSaveToGalleryFinished by rememberUpdatedState(onSaveToGalleryFinished)

        LaunchedEffect(captureEvents) {
            captureEvents.collect { event ->
                when (event) {
                    is BookDetailEvent.ShareCaptureRequestedEvent -> {
                        try {
                            reportingFailures(failureMessage = "Couldn't share — try again") {
                                when (val outcome = currentCapture.share(displayName = currentTitle)) {
                                    is ShareOutcome.Shared -> Unit

                                    is ShareOutcome.Cancelled -> Unit

                                    is ShareOutcome.Failure -> {
                                        AppLog.e("Failed to share book card: ${outcome.reason}")

                                        SnackBarManager.showSnackbar(title = "Couldn't share — try again")
                                    }
                                }
                            }

                            currentDismiss()
                        } finally {
                            currentOnShareFinished()
                        }
                    }

                    is BookDetailEvent.SaveToGalleryCaptureRequestedEvent -> {
                        try {
                            reportingFailures(failureMessage = "Couldn't save to gallery") {
                                val outcome = currentCapture.saveToGallery(displayName = currentTitle)

                                val message = if (outcome is SaveOutcome.Saved) {
                                    "Saved to gallery"
                                } else {
                                    "Couldn't save to gallery"
                                }

                                SnackBarManager.showSnackbar(title = message)
                            }
                        } finally {
                            currentOnSaveToGalleryFinished()
                        }
                    }
                }
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (body.hasVariantToggle) {
                ShareCardVariantToggle(
                    selectedVariant = body.variant,
                    onVariantSelected = onVariantSelected,
                    enabled = body.isBusy.not(),
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            ScaledShareCardPreview(
                capture = capture,
            ) {
                ShareCard(content = displayedContent)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                RhaydusButton(
                    label = "Cancel",
                    onClick = dismiss,
                    style = ButtonStyle.TEXT,
                    size = ButtonSize.M,
                    modifier = Modifier.weight(1f),
                    enabled = body.isBusy.not(),
                )

                RhaydusButton(
                    label = body.shareLabel,
                    onClick = onShareClick,
                    style = ButtonStyle.FILLED,
                    size = ButtonSize.M,
                    modifier = Modifier.weight(1f),
                    enabled = body.isBusy.not(),
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            RhaydusButton(
                label = body.saveToGalleryLabel,
                onClick = onSaveToGalleryClick,
                style = ButtonStyle.TEXT,
                size = ButtonSize.M,
                modifier = Modifier.fillMaxWidth(),
                enabled = body.isBusy.not(),
            )
        }
    }
}

private suspend fun reportingFailures(
    failureMessage: String,
    block: suspend () -> Unit,
) {
    runCatching { block() }.onFailure { error ->
        if (error is CancellationException) throw error

        AppLog.e("Share capture failed: $error")

        SnackBarManager.showSnackbar(title = failureMessage)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ShareCardVariantToggle(
    selectedVariant: ShareCardVariant,
    onVariantSelected: (ShareCardVariant) -> Unit,
    enabled: Boolean,
) {
    val variants = ShareCardVariant.entries

    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        variants.forEachIndexed { index, variant ->
            SegmentedButton(
                selected = variant == selectedVariant,
                onClick = { onVariantSelected(variant) },
                enabled = enabled,
                shape = SegmentedButtonDefaults.itemShape(
                    index = index,
                    count = variants.size,
                ),
                label = {
                    SoftcoverText(
                        text = variant.label,
                        style = MaterialTheme.typography.labelLarge,
                    )
                },
            )
        }
    }
}
