package nl.rhaydus.softcover.feature.profile.presentation.screen.section

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.layout.layout
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt
import kotlinx.coroutines.launch
import nl.rhaydus.common.AppLog
import nl.rhaydus.designsystem.component.AdaptiveModalSheet
import nl.rhaydus.designsystem.component.DesktopTooltip
import nl.rhaydus.designsystem.component.RhaydusButton
import nl.rhaydus.designsystem.model.ButtonSize
import nl.rhaydus.designsystem.model.ButtonStyle
import nl.rhaydus.designsystem.modifier.pointerHandCursor
import nl.rhaydus.designsystem.share.CapturableShareCard
import nl.rhaydus.designsystem.share.SaveOutcome
import nl.rhaydus.designsystem.share.ShareCardCapture
import nl.rhaydus.designsystem.share.ShareOutcome
import nl.rhaydus.designsystem.share.rememberShareCardCapture
import nl.rhaydus.designsystem.util.SnackBarManager
import nl.rhaydus.softcover.core.component.share.ReadingLifeShareCardUiModel
import nl.rhaydus.softcover.core.component.share.ShareCard
import nl.rhaydus.softcover.core.component.share.softcoverShareCardCaptureConfig
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.icon.drawableIconResource
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography

/**
 * The reading-life share sheet: the canonical mini header, a scaled preview of the exportable
 * [ReadingLifeShareCardUiModel] card, and Save/Share actions. The save/share/[ShareOutcome] handling is
 * copied from `feature/book_detail`'s `ShareBookBottomSheet`.
 */
@Composable
internal fun ProfileShareBottomSheet(
    content: ReadingLifeShareCardUiModel,
    onDismissRequest: () -> Unit,
) {
    val capture = rememberShareCardCapture(config = softcoverShareCardCaptureConfig)
    val coroutineScope = rememberCoroutineScope()

    var isSharing by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    val isBusy = isSharing || isSaving

    AdaptiveModalSheet(onDismissRequest = onDismissRequest) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(state = rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            SectionLabel(text = "Share your year")

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "A card built from everything you've read this year.",
                style = MaterialTheme.editorialTypography.body,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(20.dp))

            ReadingLifeSharePreview(
                content = content,
                capture = capture,
            )

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

@Composable
private fun ReadingLifeSharePreview(
    content: ReadingLifeShareCardUiModel,
    capture: ShareCardCapture,
) {
    val maxPreviewWidth = 240.dp
    val maxPreviewHeight = 420.dp

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        contentAlignment = Alignment.Center,
    ) {
        CapturableShareCard(
            capture = capture,
            modifier = Modifier.layout { measurable, _ ->
                val placeable = measurable.measure(constraints = Constraints())

                val widthScale = maxPreviewWidth.toPx() / placeable.width
                val heightScale = maxPreviewHeight.toPx() / placeable.height
                val scale = minOf(
                    widthScale,
                    heightScale,
                    1f,
                )

                val scaledWidth = (placeable.width * scale).roundToInt()
                val scaledHeight = (placeable.height * scale).roundToInt()

                layout(width = scaledWidth, height = scaledHeight) {
                    placeable.placeWithLayer(
                        x = -(placeable.width - scaledWidth) / 2,
                        y = -(placeable.height - scaledHeight) / 2,
                    ) {
                        scaleX = scale
                        scaleY = scale
                        transformOrigin = TransformOrigin.Center
                    }
                }
            },
        ) {
            ShareCard(content = content)
        }
    }
}
