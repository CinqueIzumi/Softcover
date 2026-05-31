package nl.rhaydus.softcover.feature.book_detail.presentation.component

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
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
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt
import kotlinx.coroutines.launch
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.presentation.component.SoftcoverButton
import nl.rhaydus.softcover.core.presentation.model.ButtonSize
import nl.rhaydus.softcover.core.presentation.model.ButtonStyle
import nl.rhaydus.softcover.core.presentation.share.BookShareContent
import nl.rhaydus.softcover.core.presentation.share.CapturableShareCard
import nl.rhaydus.softcover.core.presentation.share.SaveOutcome
import nl.rhaydus.softcover.core.presentation.share.ShareCardCapture
import nl.rhaydus.softcover.core.presentation.share.rememberShareCardCapture
import nl.rhaydus.softcover.core.presentation.theme.editorialTypography
import timber.log.Timber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareBookBottomSheet(
    book: Book,
    edition: BookEdition?,
    onDismissRequest: () -> Unit,
) {
    val content = remember(book, edition) { book.toShareContent(edition = edition) }
    val capture = rememberShareCardCapture()
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    var isSharing by remember { mutableStateOf(false) }
    var isSavingToGallery by remember { mutableStateOf(false) }
    val isBusy = isSharing || isSavingToGallery

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
    ) {
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
                text = "An editorial card of ${book.title}",
                style = MaterialTheme.editorialTypography.body,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(20.dp))

            ShareCardPreview(
                content = content,
                capture = capture,
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                SoftcoverButton(
                    label = "Cancel",
                    onClick = onDismissRequest,
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
                            runCatching {
                                val outcome = capture.saveToCache(displayName = book.title)

                                context.launchShareImageChooser(
                                    uri = outcome.uri,
                                    bookTitle = book.title,
                                )
                            }.onFailure {
                                Timber.e("$it")
                            }

                            isSharing = false
                            onDismissRequest()
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
                            Timber.e("$it")
                        }.getOrNull()

                        val message = if (saved is SaveOutcome.Saved) {
                            "Saved to gallery"
                        } else {
                            "Couldn't save to gallery"
                        }

                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()

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

@Composable
private fun ShareCardPreview(
    content: BookShareContent,
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
                val scale = minOf(widthScale, heightScale, 1f)

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

private fun Context.launchShareImageChooser(
    uri: Uri,
    bookTitle: String,
) {
    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        type = "image/png"
        putExtra(Intent.EXTRA_STREAM, uri)
        putExtra(Intent.EXTRA_SUBJECT, bookTitle)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    val chooser = Intent.createChooser(sendIntent, "Share $bookTitle").apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    startActivity(chooser)
}
