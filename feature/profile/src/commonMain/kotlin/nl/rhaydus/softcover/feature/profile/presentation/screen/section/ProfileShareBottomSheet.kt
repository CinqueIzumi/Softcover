package nl.rhaydus.softcover.feature.profile.presentation.screen.section

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import nl.rhaydus.common.AppLog
import nl.rhaydus.designsystem.component.DesktopTooltip
import nl.rhaydus.designsystem.component.RhaydusButton
import nl.rhaydus.designsystem.model.ButtonSize
import nl.rhaydus.designsystem.model.ButtonStyle
import nl.rhaydus.designsystem.modifier.pointerHandCursor
import nl.rhaydus.designsystem.share.SaveOutcome
import nl.rhaydus.designsystem.share.ShareCardCapture
import nl.rhaydus.designsystem.share.ShareOutcome
import nl.rhaydus.designsystem.share.rememberShareCardCapture
import nl.rhaydus.designsystem.util.SnackBarManager
import nl.rhaydus.softcover.core.component.share.ReadingLifeShareCardUiModel
import nl.rhaydus.softcover.core.component.share.ScaledShareCardPreview
import nl.rhaydus.softcover.core.component.share.ShareCard
import nl.rhaydus.softcover.core.component.share.softcoverShareCardCaptureConfig
import nl.rhaydus.softcover.core.component.sheet.SheetScaffold
import nl.rhaydus.softcover.core.component.sheet.SheetScaffoldEvent
import nl.rhaydus.softcover.core.component.sheet.SheetScaffoldUiModel
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.icon.drawableIconResource

@Composable
internal fun ProfileShareBottomSheet(
    content: ReadingLifeShareCardUiModel,
    scaffold: SheetScaffoldUiModel,
    onDismissRequest: () -> Unit,
) {
    val capture = rememberShareCardCapture(config = softcoverShareCardCaptureConfig)
    val coroutineScope = rememberCoroutineScope()

    var isSharing by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    val isBusy = isSharing || isSaving

    SheetScaffold(
        model = scaffold,
        onEvent = { event ->
            when (event) {
                SheetScaffoldEvent.Dismissed -> onDismissRequest()

                SheetScaffoldEvent.PrimaryClicked,
                SheetScaffoldEvent.SecondaryClicked,
                SheetScaffoldEvent.HeaderActionClicked -> Unit
            }
        },
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            ScaledShareCardPreview(
                capture = capture,
                maxWidth = 240.dp,
                maxHeight = 420.dp,
            ) {
                ShareCard(content = content)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                RhaydusButton(
                    label = if (isSaving) "Saving…" else "Save image",
                    onClick = {
                        if (isBusy) return@RhaydusButton

                        isSaving = true

                        coroutineScope.launch {
                            val saved = runCatching {
                                capture.saveToGallery(displayName = "${content.readerName}-reading-life")
                            }.onFailure {
                                AppLog.e("$it")
                            }.getOrNull()

                            val message = if (saved is SaveOutcome.Saved) {
                                "Saved to gallery"
                            } else {
                                "Couldn't save to gallery"
                            }

                            SnackBarManager.showSnackbar(title = message)

                            isSaving = false
                        }
                    },
                    style = ButtonStyle.FILLED,
                    size = ButtonSize.M,
                    modifier = Modifier.weight(1f),
                    enabled = isBusy.not(),
                )

                DesktopTooltip(text = "Share") {
                    Surface(
                        onClick = {
                            if (isBusy) return@Surface

                            isSharing = true

                            coroutineScope.launch {
                                val displayName = "${content.readerName}-reading-life"

                                when (val outcome = capture.share(displayName = displayName)) {
                                    is ShareOutcome.Shared -> Unit

                                    is ShareOutcome.Cancelled -> Unit

                                    is ShareOutcome.Failure -> {
                                        AppLog.e("Failed to share reading-life card: ${outcome.reason}")

                                        SnackBarManager.showSnackbar(title = "Couldn't share — try again")
                                    }
                                }

                                isSharing = false
                            }
                        },
                        enabled = isBusy.not(),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        modifier = Modifier
                            .size(52.dp)
                            .pointerHandCursor(),
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            val icon = drawableIconResource(
                                icon = SoftcoverIcon.Share,
                                contentDescription = "Share",
                            )

                            Icon(
                                painter = icon.getIconPainter(),
                                contentDescription = icon.contentDescription,
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
