package nl.rhaydus.softcover.feature.book_detail.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
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
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import nl.rhaydus.designsystem.component.AdaptiveModalSheet
import nl.rhaydus.designsystem.component.LocalModalSheetDismiss
import nl.rhaydus.designsystem.util.SnackBarManager
import nl.rhaydus.softcover.core.designsystem.presentation.component.SoftcoverButton
import nl.rhaydus.softcover.core.designsystem.presentation.model.ButtonSize
import nl.rhaydus.softcover.core.designsystem.presentation.model.ButtonStyle
import nl.rhaydus.softcover.core.designsystem.presentation.share.BookShareContent
import nl.rhaydus.softcover.core.designsystem.presentation.share.CapturableShareCard
import nl.rhaydus.softcover.core.designsystem.presentation.share.SaveOutcome
import nl.rhaydus.softcover.core.designsystem.presentation.share.ShareCardCapture
import nl.rhaydus.softcover.core.designsystem.presentation.share.ShareContent
import nl.rhaydus.softcover.core.designsystem.presentation.share.ShareOutcome
import nl.rhaydus.softcover.core.designsystem.presentation.share.rememberShareCardCapture
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.core.domain.logging.AppLog
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.UserTag

@Composable
internal fun ShareBookBottomSheet(
    book: Book,
    edition: BookEdition?,
    currentUsername: String?,
    currentUserAvatarUrl: String?,
    userTags: List<UserTag>,
    onDismissRequest: () -> Unit,
) {
    val bookContent = remember(book, edition) { book.toShareContent(edition = edition) }

    val updateContent = remember(book, edition, currentUsername, currentUserAvatarUrl, userTags) {
        book.toReadingUpdateContent(
            edition = edition,
            username = currentUsername,
            avatarUrl = currentUserAvatarUrl,
            userTags = userTags,
        )
    }

    val capture = rememberShareCardCapture()
    val coroutineScope = rememberCoroutineScope()

    var isSharing by remember { mutableStateOf(false) }
    var isSavingToGallery by remember { mutableStateOf(false) }
    val isBusy = isSharing || isSavingToGallery

    var selectedVariant by remember(updateContent != null) {
        mutableStateOf(if (updateContent != null) ShareCardVariant.MY_UPDATE else ShareCardVariant.BOOK)
    }

    val displayedContent: ShareContent = when (selectedVariant) {
        ShareCardVariant.MY_UPDATE -> updateContent ?: bookContent
        ShareCardVariant.BOOK -> bookContent
    }

    val subtitle = if (updateContent != null && selectedVariant == ShareCardVariant.MY_UPDATE) {
        "Your reading update for ${book.title}"
    } else {
        "An editorial card of ${book.title}"
    }

    AdaptiveModalSheet(onDismissRequest = onDismissRequest) {
        val dismiss = LocalModalSheetDismiss.current

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(state = rememberScrollState())
                .imePadding()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "SHARE",
                style = MaterialTheme.editorialTypography.eyebrow,
                color = MaterialTheme.colorScheme.primary,
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = subtitle,
                style = MaterialTheme.editorialTypography.body,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            if (updateContent != null) {
                Spacer(modifier = Modifier.height(20.dp))

                ShareCardVariantToggle(
                    selectedVariant = selectedVariant,
                    onVariantSelected = { selectedVariant = it },
                    enabled = isBusy.not(),
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            ShareCardPreview(
                content = displayedContent,
                capture = capture,
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                SoftcoverButton(
                    label = "Cancel",
                    onClick = dismiss,
                    style = ButtonStyle.OUTLINED,
                    size = ButtonSize.M,
                    modifier = Modifier.weight(1f),
                    enabled = isBusy.not(),
                )

                SoftcoverButton(
                    label = if (isSharing) "Sharing…" else "Share",
                    onClick = {
                        if (isBusy) return@SoftcoverButton

                        isSharing = true

                        coroutineScope.launch {
                            when (val outcome = capture.share(displayName = book.title)) {
                                is ShareOutcome.Shared -> Unit

                                is ShareOutcome.Failure -> {
                                    AppLog.e("Failed to share book card: ${outcome.reason}")

                                    SnackBarManager.showSnackbar(title = "Couldn't share — try again")
                                }
                            }

                            isSharing = false
                            dismiss()
                        }
                    },
                    style = ButtonStyle.FILLED,
                    size = ButtonSize.M,
                    modifier = Modifier.weight(1f),
                    enabled = isBusy.not(),
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            SoftcoverButton(
                label = if (isSavingToGallery) "Saving to gallery…" else "Save to gallery",
                onClick = {
                    if (isBusy) return@SoftcoverButton

                    isSavingToGallery = true

                    coroutineScope.launch {
                        val saved = runCatching {
                            capture.saveToGallery(displayName = book.title)
                        }.onFailure {
                            AppLog.e("$it")
                        }.getOrNull()

                        val message = if (saved is SaveOutcome.Saved) {
                            "Saved to gallery"
                        } else {
                            "Couldn't save to gallery"
                        }

                        SnackBarManager.showSnackbar(title = message)

                        isSavingToGallery = false
                    }
                },
                style = ButtonStyle.TEXT,
                size = ButtonSize.M,
                modifier = Modifier.fillMaxWidth(),
                enabled = isBusy.not(),
            )

            Spacer(modifier = Modifier.height(8.dp))
        }
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
                    Text(
                        text = variant.label,
                        style = MaterialTheme.typography.labelLarge,
                    )
                },
            )
        }
    }
}

@Composable
private fun ShareCardPreview(
    content: ShareContent,
    capture: ShareCardCapture,
) {
    val maxPreviewWidth = 280.dp
    val maxPreviewHeight = 360.dp

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        contentAlignment = Alignment.Center,
    ) {
        CapturableShareCard(
            capture = capture,
            content = content,
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
        )
    }
}

private fun Book.toShareContent(edition: BookEdition?): BookShareContent {
    val resolvedEdition = edition ?: defaultEdition

    return BookShareContent(
        coverUrl = resolvedEdition?.localImagePath
            ?: resolvedEdition?.url
            ?: coverUrl,
        title = title,
        author = authors.firstOrNull()?.name.orEmpty(),
        communityRating = rating.takeIf { it > 0.0 },
        userRating = userBook?.rating?.toInt(),
        releaseYear = resolvedEdition?.releaseYear?.takeIf { it != -1 } ?: releaseYear.takeIf { it != -1 },
        pageCount = resolvedEdition?.pages,
        description = description.takeIf { it.isNotBlank() },
        quote = null,
    )
}
