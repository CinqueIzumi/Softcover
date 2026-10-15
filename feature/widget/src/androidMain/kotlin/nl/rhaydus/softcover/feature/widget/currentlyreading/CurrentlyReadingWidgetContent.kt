package nl.rhaydus.softcover.feature.widget.currentlyreading

import android.graphics.Bitmap
import android.os.Build
import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
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
import nl.rhaydus.softcover.feature.widget.progress.WavyProgressBitmaps
import nl.rhaydus.softcover.feature.widget.theme.LocalWidgetExtraColors
import nl.rhaydus.softcover.feature.widget.theme.SoftcoverWidgetTheme

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
    val hasArrows = content.position != null

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
                .padding(Spacing.xs),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .defaultWeight(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (hasArrows) {
                    PreviousArrow(
                        modifier = GlanceModifier
                            .width(30.dp)
                            .fillMaxHeight(),
                    )
                }

                Box(
                    modifier = GlanceModifier
                        .defaultWeight()
                        .fillMaxHeight(),
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
                                .width(40.dp)
                                .cornerRadius(CornerRadius.xxs)
                                .background(GlanceTheme.colors.surfaceVariant),
                        ) {}
                    }
                }

                if (hasArrows) {
                    NextArrow(
                        modifier = GlanceModifier
                            .width(30.dp)
                            .fillMaxHeight(),
                    )
                }
            }

            Spacer(modifier = GlanceModifier.height(Spacing.xxs))

            ProgressBar(bars = content.progress)

            Text(
                text = content.progressLabel,
                style = TextStyle(
                    color = GlanceTheme.colors.onSurface,
                    fontSize = 12.sp,
                ),
            )
        }
    }
}

@Composable
private fun TallReading(content: CurrentlyReadingWidgetUiModel.Content.Reading) {
    val artwork = content.artwork

    Box(
        modifier = GlanceModifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        if (artwork != null) {
            Backdrop(
                backdrop = artwork.tallBackdrop,
                artwork = artwork,
            )
        }

        Column(
            modifier = GlanceModifier
                .width(110.dp)
                .fillMaxHeight()
                .padding(
                    horizontal = Spacing.xxs,
                    vertical = Spacing.xxs,
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .defaultWeight(),
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
                            .width(56.dp)
                            .cornerRadius(CornerRadius.xxs)
                            .background(GlanceTheme.colors.surfaceVariant),
                    ) {}
                }
            }

            Spacer(modifier = GlanceModifier.height(Spacing.xxs))

            Text(
                text = content.title,
                maxLines = 2,
                style = TextStyle(
                    color = GlanceTheme.colors.onSurface,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif,
                    textAlign = TextAlign.Center,
                ),
            )

            Text(
                text = content.author,
                maxLines = 1,
                style = TextStyle(
                    color = GlanceTheme.colors.onSurfaceVariant,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                ),
            )

            Spacer(modifier = GlanceModifier.height(Spacing.xxs))

            ProgressBar(bars = content.progress)

            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = GlanceModifier.height(36.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = content.progressLabel,
                        style = TextStyle(
                            color = GlanceTheme.colors.onSurface,
                            fontSize = 12.sp,
                        ),
                    )
                }

                Spacer(modifier = GlanceModifier.defaultWeight())

                if (content.position != null) {
                    PreviousArrow(
                        modifier = GlanceModifier.size(
                            width = 26.dp,
                            height = 36.dp,
                        ),
                    )

                    Box(
                        modifier = GlanceModifier.height(36.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = content.position,
                            style = TextStyle(
                                color = GlanceTheme.colors.onSurface,
                                fontSize = 11.sp,
                            ),
                        )
                    }

                    NextArrow(
                        modifier = GlanceModifier.size(
                            width = 26.dp,
                            height = 36.dp,
                        ),
                    )
                }
            }
        }
    }
}

@Composable
private fun Backdrop(
    backdrop: Bitmap?,
    artwork: ReadingArtwork,
) {
    val extra = LocalWidgetExtraColors.current

    if (backdrop != null) {
        Image(
            provider = ImageProvider(backdrop),
            contentDescription = null,
            modifier = GlanceModifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
    } else {
        Mask(
            mask = artwork.gradients.washPrimary,
            tint = GlanceTheme.colors.primary,
        )

        Mask(
            mask = artwork.gradients.washTertiary,
            tint = GlanceTheme.colors.tertiary,
        )
    }

    Mask(
        mask = artwork.gradients.veil,
        tint = extra.surfaceContainerHigh,
    )

    Mask(
        mask = artwork.gradients.veilLift,
        tint = extra.daySurfaceContainerHigh,
    )
}

@Composable
private fun Mask(
    mask: Bitmap,
    tint: ColorProvider,
    contentScale: ContentScale = ContentScale.FillBounds,
) {
    Image(
        provider = ImageProvider(mask),
        contentDescription = null,
        modifier = GlanceModifier.fillMaxSize(),
        contentScale = contentScale,
        colorFilter = ColorFilter.tint(tint),
    )
}

@Composable
private fun ProgressBar(bars: WavyProgressBitmaps) {
    Box(
        modifier = GlanceModifier
            .width(BAR_WIDTH_DP.dp)
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
private fun PreviousArrow(modifier: GlanceModifier) {
    Arrow(
        icon = R.drawable.ic_widget_chevron_left,
        step = -1,
        description = LocalContext.current.getString(R.string.currently_reading_previous_book),
        modifier = modifier,
    )
}

@Composable
private fun NextArrow(modifier: GlanceModifier) {
    Arrow(
        icon = R.drawable.ic_widget_chevron_right,
        step = 1,
        description = LocalContext.current.getString(R.string.currently_reading_next_book),
        modifier = modifier,
    )
}

@Composable
private fun Arrow(
    @DrawableRes icon: Int,
    step: Int,
    description: String,
    modifier: GlanceModifier,
) {
    Box(
        modifier = modifier
            .semantics { contentDescription = description }
            .clickable(
                actionRunCallback<CycleBookCallback>(
                    actionParametersOf(CycleBookCallback.StepKey to step),
                ),
            ),
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
