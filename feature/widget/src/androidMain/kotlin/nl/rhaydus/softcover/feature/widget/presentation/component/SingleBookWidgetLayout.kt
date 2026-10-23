package nl.rhaydus.softcover.feature.widget.presentation.component

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.appwidget.ImageProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.ContentScale
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import nl.rhaydus.softcover.feature.widget.presentation.model.BookArtworkUiModel
import nl.rhaydus.softcover.feature.widget.presentation.util.TALL_HEIGHT_DP
import nl.rhaydus.softcover.feature.widget.presentation.util.isSideBySide
import nl.rhaydus.softcover.feature.widget.presentation.util.sideCoverWidthDp
import nl.rhaydus.softcover.feature.widget.presentation.util.sideShowsAuthor
import nl.rhaydus.softcover.feature.widget.presentation.util.sideTitleLines
import nl.rhaydus.softcover.feature.widget.presentation.util.stackedTitleLines

/**
 * The book state both single-book widgets share. [eyebrowHeightDp] and [footHeightDp] are the heights [eyebrow] and
 * [foot] take at the current size, 0 for a slot that draws nothing there; the side layout fits the title lines to
 * what they leave.
 */
@Composable
internal fun SingleBookWidgetLayout(
    title: String,
    author: String,
    cover: Uri?,
    artwork: BookArtworkUiModel?,
    eyebrowHeightDp: Int,
    footHeightDp: Int,
    eyebrow: @Composable () -> Unit,
    foot: @Composable () -> Unit,
) {
    val size = LocalSize.current
    val fontScale = LocalContext.current.resources.configuration.fontScale

    val showsAuthor = sideShowsAuthor(
        size = size,
        fixedRowsDp = eyebrowHeightDp + footHeightDp,
        fontScale = fontScale,
    )

    Box(modifier = GlanceModifier.fillMaxSize()) {
        if (artwork != null) {
            BackdropUris(
                backdrop = artwork.backdrop,
                veil = artwork.veil,
                veilLift = artwork.veilLift,
                washPrimary = artwork.washPrimary,
                washTertiary = artwork.washTertiary,
            )
        }

        if (isSideBySide(size)) {
            Row(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .padding(Spacing.xxs),
            ) {
                Box(
                    modifier = GlanceModifier
                        .width(sideCoverWidthDp(size).dp + Spacing.xs)
                        .fillMaxHeight()
                        .padding(
                            start = Spacing.xs,
                            top = Spacing.xs,
                            bottom = Spacing.xs,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    BookCover(cover = cover)
                }

                Spacer(modifier = GlanceModifier.width(Spacing.xs))

                Column(
                    modifier = GlanceModifier
                        .defaultWeight()
                        .fillMaxHeight(),
                ) {
                    eyebrow()

                    Column(
                        modifier = GlanceModifier
                            .fillMaxWidth()
                            .defaultWeight(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = title,
                            maxLines = sideTitleLines(
                                size = size,
                                fixedRowsDp = eyebrowHeightDp + footHeightDp,
                                fontScale = fontScale,
                            ),
                            style = bookTitleStyle(TextAlign.Start),
                        )

                        if (showsAuthor) {
                            Author(
                                author = author,
                                textAlign = TextAlign.Start,
                            )
                        }
                    }

                    foot()
                }
            }
        } else {
            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .padding(Spacing.xxs),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                eyebrow()

                Box(
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .defaultWeight(),
                    contentAlignment = Alignment.Center,
                ) {
                    BookCover(
                        cover = cover,
                        modifier = GlanceModifier.padding(horizontal = Spacing.xs),
                    )
                }

                if (size.height >= TALL_HEIGHT_DP.dp) {
                    Spacer(modifier = GlanceModifier.height(Spacing.xxs))

                    ReservedLinesTitle(
                        title = title,
                        reservedLines = stackedTitleLines(size.height.value.toInt()),
                    )

                    Author(
                        author = author,
                        textAlign = TextAlign.Center,
                        modifier = GlanceModifier.padding(horizontal = Spacing.xs),
                    )
                }

                foot()
            }
        }
    }
}

@Composable
private fun BookCover(
    cover: Uri?,
    modifier: GlanceModifier = GlanceModifier,
) {
    if (cover != null) {
        Image(
            provider = ImageProvider(cover),
            contentDescription = null,
            modifier = modifier.fillMaxSize(),
            contentScale = ContentScale.Fit,
        )
    } else {
        CoverPlaceholder()
    }
}

@Composable
private fun Author(
    author: String,
    textAlign: TextAlign,
    modifier: GlanceModifier = GlanceModifier,
) {
    Text(
        text = author,
        maxLines = 1,
        modifier = modifier,
        style = TextStyle(
            color = GlanceTheme.colors.onSurfaceVariant,
            fontSize = 12.sp,
            textAlign = textAlign,
        ),
    )
}
