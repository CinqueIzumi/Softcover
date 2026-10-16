package nl.rhaydus.softcover.feature.widget.presentation.glance

import android.os.Build
import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.ColorFilter
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.Action
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
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
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.semantics.contentDescription
import androidx.glance.semantics.semantics
import androidx.glance.text.FontFamily
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import nl.rhaydus.softcover.core.designsystem.presentation.theme.CornerRadius
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import nl.rhaydus.softcover.feature.widget.R
import nl.rhaydus.softcover.feature.widget.presentation.activity.updateProgressIntent
import nl.rhaydus.softcover.feature.widget.presentation.callback.CycleBookCallback
import nl.rhaydus.softcover.feature.widget.presentation.component.Backdrop
import nl.rhaydus.softcover.feature.widget.presentation.component.Mask
import nl.rhaydus.softcover.feature.widget.presentation.model.CurrentlyReadingWidgetUiModel
import nl.rhaydus.softcover.feature.widget.presentation.render.WavyProgressBitmaps
import nl.rhaydus.softcover.feature.widget.presentation.theme.LocalWidgetExtraColors
import nl.rhaydus.softcover.feature.widget.presentation.theme.SoftcoverWidgetTheme
import nl.rhaydus.softcover.feature.widget.presentation.util.progressBarWidthDp

@Composable
internal fun CurrentlyReadingWidgetContent(
    model: CurrentlyReadingWidgetUiModel,
    openReading: Action,
) {
    val context = LocalContext.current

    SoftcoverWidgetTheme(themeConfiguration = model.themeConfiguration) {
        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .appWidgetBackground()
                .background(LocalWidgetExtraColors.current.surfaceContainerHigh)
                .then(widgetCornerRadius())
                .clickable(openReading),
            contentAlignment = Alignment.Center,
        ) {
            when (val content = model.content) {
                CurrentlyReadingWidgetUiModel.Content.Loading -> Unit

                CurrentlyReadingWidgetUiModel.Content.SignedOut -> Message(
                    headline = context.getString(R.string.widget_sign_in),
                    subline = null,
                )

                CurrentlyReadingWidgetUiModel.Content.Empty -> Message(
                    headline = context.getString(R.string.currently_reading_between_books),
                    subline = context.getString(R.string.currently_reading_between_books_subline),
                )

                is CurrentlyReadingWidgetUiModel.Content.Reading -> if (LocalSize.current.height >= 150.dp) {
                    TallReading(content)
                } else {
                    CompactReading(content)
                }
            }
        }
    }
}

private fun widgetCornerRadius(): GlanceModifier = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
    GlanceModifier.cornerRadius(android.R.dimen.system_app_widget_background_radius)
} else {
    GlanceModifier
}

@Composable
private fun Message(
    headline: String,
    subline: String?,
) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .padding(Spacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = headline,
            style = TextStyle(
                color = GlanceTheme.colors.onSurface,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif,
                textAlign = TextAlign.Center,
            ),
        )

        if (subline != null) {
            Spacer(modifier = GlanceModifier.height(Spacing.xxs))

            Text(
                text = subline,
                style = TextStyle(
                    color = GlanceTheme.colors.onSurfaceVariant,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                ),
            )
        }
    }
}

@Composable
private fun CompactReading(content: CurrentlyReadingWidgetUiModel.Content.Reading) {
    val artwork = content.artwork

    Box(modifier = GlanceModifier.fillMaxSize()) {
        if (artwork != null) {
            Backdrop(
                backdrop = artwork.compactBackdrop,
                artwork = artwork,
            )
        }

        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .padding(Spacing.xxs),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            CoverArea(
                content = content,
                coverWidth = 40.dp,
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .defaultWeight(),
            )

            Spacer(modifier = GlanceModifier.height(Spacing.xxs))

            ProgressBar(progress = content.progress)

            BottomRow(content = content)
        }
    }
}

@Composable
private fun TwoLineTitle(title: String) {
    val style = TextStyle(
        color = GlanceTheme.colors.onSurface,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Serif,
        textAlign = TextAlign.Center,
    )

    Box(
        modifier = GlanceModifier.padding(horizontal = Spacing.xs),
        contentAlignment = Alignment.BottomCenter,
    ) {
        // Glance has no minLines: this invisible two-line text holds the box at two lines, at any font scale.
        Text(
            text = " \n ",
            maxLines = 2,
            style = style.copy(color = ColorProvider(Color.Transparent)),
        )

        Text(
            text = title,
            maxLines = 2,
            style = style,
        )
    }
}

@Composable
private fun TallReading(content: CurrentlyReadingWidgetUiModel.Content.Reading) {
    val artwork = content.artwork

    Box(modifier = GlanceModifier.fillMaxSize()) {
        if (artwork != null) {
            Backdrop(
                backdrop = artwork.tallBackdrop,
                artwork = artwork,
            )
        }

        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .padding(Spacing.xxs),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            CoverArea(
                content = content,
                coverWidth = 56.dp,
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .defaultWeight(),
            )

            Spacer(modifier = GlanceModifier.height(Spacing.xxs))

            TwoLineTitle(title = content.title)

            Text(
                text = content.author,
                maxLines = 1,
                modifier = GlanceModifier.padding(horizontal = Spacing.xs),
                style = TextStyle(
                    color = GlanceTheme.colors.onSurfaceVariant,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                ),
            )

            Spacer(modifier = GlanceModifier.height(Spacing.xxs))

            ProgressBar(progress = content.progress)

            BottomRow(content = content)
        }
    }
}

@Composable
private fun CoverArea(
    content: CurrentlyReadingWidgetUiModel.Content.Reading,
    coverWidth: Dp,
    modifier: GlanceModifier,
) {
    val hasArrows = content.position != null

    Box(modifier = modifier) {
        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .padding(horizontal = if (hasArrows) 32.dp else 0.dp),
            contentAlignment = Alignment.Center,
        ) {
            if (content.cover != null) {
                Image(
                    provider = ImageProvider(content.cover),
                    contentDescription = null,
                    modifier = GlanceModifier.fillMaxSize(),
                    contentScale = ContentScale.Fit,
                )
            } else {
                Box(
                    modifier = GlanceModifier
                        .fillMaxHeight()
                        .width(coverWidth)
                        .cornerRadius(CornerRadius.xxs)
                        .background(GlanceTheme.colors.surfaceVariant),
                ) {}
            }
        }

        if (hasArrows) {
            Box(
                modifier = GlanceModifier.fillMaxSize(),
                contentAlignment = Alignment.CenterStart,
            ) {
                PreviousArrow()
            }

            Box(
                modifier = GlanceModifier.fillMaxSize(),
                contentAlignment = Alignment.CenterEnd,
            ) {
                NextArrow()
            }
        }
    }
}

@Composable
private fun BottomRow(content: CurrentlyReadingWidgetUiModel.Content.Reading) {
    Row(
        modifier = GlanceModifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (content.position == null) {
            Spacer(modifier = GlanceModifier.defaultWeight())

            ProgressButton(content = content)

            Spacer(modifier = GlanceModifier.defaultWeight())
        } else {
            ProgressButton(content = content)

            Spacer(modifier = GlanceModifier.defaultWeight())

            Text(
                text = content.position,
                style = TextStyle(
                    color = GlanceTheme.colors.onSurfaceVariant,
                    fontSize = 12.sp,
                ),
            )
        }
    }
}

@Composable
private fun ProgressButton(content: CurrentlyReadingWidgetUiModel.Content.Reading) {
    val context = LocalContext.current

    Row(
        modifier = GlanceModifier
            .height(36.dp)
            .padding(horizontal = Spacing.xs)
            .semantics { contentDescription = context.getString(R.string.currently_reading_update_progress) }
            .clickable(
                actionStartActivity(
                    updateProgressIntent(
                        context = context,
                        bookId = content.bookId,
                    ),
                ),
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = content.progressLabel,
            style = TextStyle(
                color = GlanceTheme.colors.onSurface,
                fontSize = 12.sp,
            ),
        )

        Spacer(modifier = GlanceModifier.width(Spacing.xxs))

        Image(
            provider = ImageProvider(R.drawable.ic_widget_edit),
            contentDescription = null,
            modifier = GlanceModifier.size(16.dp),
            colorFilter = ColorFilter.tint(GlanceTheme.colors.onSurfaceVariant),
        )
    }
}

@Composable
private fun ProgressBar(progress: Map<Int, WavyProgressBitmaps>) {
    val widthDp = progressBarWidthDp(LocalSize.current.width)
    val bars = progress[widthDp] ?: return

    Box(
        modifier = GlanceModifier
            .width(widthDp.dp)
            .height(12.dp),
    ) {
        Mask(
            mask = bars.track,
            tint = GlanceTheme.colors.secondaryContainer,
            contentScale = ContentScale.Fit,
        )

        Mask(
            mask = bars.indicator,
            tint = GlanceTheme.colors.primary,
            contentScale = ContentScale.Fit,
        )
    }
}

@Composable
private fun PreviousArrow() {
    Arrow(
        icon = R.drawable.ic_widget_chevron_left,
        step = -1,
        description = LocalContext.current.getString(R.string.currently_reading_previous_book),
    )
}

@Composable
private fun NextArrow() {
    Arrow(
        icon = R.drawable.ic_widget_chevron_right,
        step = 1,
        description = LocalContext.current.getString(R.string.currently_reading_next_book),
    )
}

@Composable
private fun Arrow(
    @DrawableRes icon: Int,
    step: Int,
    description: String,
) {
    Box(
        modifier = GlanceModifier
            .width(32.dp)
            .fillMaxHeight()
            .semantics { contentDescription = description }
            .clickable(
                actionRunCallback<CycleBookCallback>(
                    actionParametersOf(CycleBookCallback.StepKey to step),
                ),
            ),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = GlanceModifier
                .size(28.dp)
                .cornerRadius(CornerRadius.xxl)
                .background(LocalWidgetExtraColors.current.arrowScrim),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                provider = ImageProvider(icon),
                contentDescription = null,
                modifier = GlanceModifier.size(20.dp),
                colorFilter = ColorFilter.tint(GlanceTheme.colors.onSurface),
            )
        }
    }
}
