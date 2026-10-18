package nl.rhaydus.softcover.feature.widget.presentation.glance

import android.net.Uri
import android.os.Build
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
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.CircularProgressIndicator
import androidx.glance.appwidget.ImageProvider
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.ContentScale
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
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
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import nl.rhaydus.softcover.feature.widget.R
import nl.rhaydus.softcover.feature.widget.presentation.activity.updateProgressIntent
import nl.rhaydus.softcover.feature.widget.presentation.callback.CycleBookCallback
import nl.rhaydus.softcover.feature.widget.presentation.component.BackdropUris
import nl.rhaydus.softcover.feature.widget.presentation.component.CycleEyebrow
import nl.rhaydus.softcover.feature.widget.presentation.component.ReservedLinesTitle
import nl.rhaydus.softcover.feature.widget.presentation.component.UriMask
import nl.rhaydus.softcover.feature.widget.presentation.model.CurrentlyReadingWidgetUiModel
import nl.rhaydus.softcover.feature.widget.presentation.render.WavyProgressUris
import nl.rhaydus.softcover.feature.widget.presentation.theme.LocalWidgetExtraColors
import nl.rhaydus.softcover.feature.widget.presentation.theme.SoftcoverWidgetTheme
import nl.rhaydus.softcover.feature.widget.presentation.util.PROGRESS_BAR_HEIGHT_DP
import nl.rhaydus.softcover.feature.widget.presentation.util.TALLEST_HEIGHT_DP
import nl.rhaydus.softcover.feature.widget.presentation.util.TALL_HEIGHT_DP
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
                .background(
                    when (model.content) {
                        is CurrentlyReadingWidgetUiModel.Content.Reading -> LocalWidgetExtraColors.current.surfaceContainerHigh
                        else -> GlanceTheme.colors.background
                    },
                )
                .then(widgetCornerRadius())
                .clickable(openReading),
            contentAlignment = Alignment.Center,
        ) {
            when (val content = model.content) {
                CurrentlyReadingWidgetUiModel.Content.Loading -> CircularProgressIndicator(color = GlanceTheme.colors.primary)

                CurrentlyReadingWidgetUiModel.Content.SignedOut -> Message(
                    headline = context.getString(R.string.widget_sign_in),
                    subline = null,
                )

                CurrentlyReadingWidgetUiModel.Content.Empty -> Message(
                    headline = context.getString(R.string.currently_reading_between_books),
                    subline = context.getString(R.string.currently_reading_between_books_subline),
                )

                is CurrentlyReadingWidgetUiModel.Content.Reading -> Reading(content)
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
            .padding(Spacing.xxs),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            modifier = GlanceModifier
                .fillMaxWidth()
                .defaultWeight()
                .padding(Spacing.xs),
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = headline,
                style = TextStyle(
                    color = GlanceTheme.colors.onBackground,
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
}

@Composable
private fun Reading(content: CurrentlyReadingWidgetUiModel.Content.Reading) {
    val context = LocalContext.current
    val artwork = content.artwork
    val height = LocalSize.current.height

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

        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .padding(Spacing.xxs),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            CycleEyebrow(
                label = content.eyebrow,
                previous = cycleAction(
                    canCycle = content.canCycle,
                    step = -1,
                ),
                next = cycleAction(
                    canCycle = content.canCycle,
                    step = 1,
                ),
                previousDescription = context.getString(R.string.currently_reading_previous_book),
                nextDescription = context.getString(R.string.currently_reading_next_book),
            )

            Box(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .defaultWeight(),
                contentAlignment = Alignment.Center,
            ) {
                Cover(cover = content.cover)
            }

            if (height >= TALL_HEIGHT_DP.dp) {
                Spacer(modifier = GlanceModifier.height(Spacing.xxs))

                ReservedLinesTitle(
                    title = content.title,
                    reservedLines = if (height >= TALLEST_HEIGHT_DP.dp) 2 else 1,
                )

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
            }

            ProgressRow(content = content)
        }
    }
}

@Composable
private fun Cover(cover: Uri?) {
    if (cover != null) {
        Image(
            provider = ImageProvider(cover),
            contentDescription = null,
            modifier = GlanceModifier.fillMaxSize(),
            contentScale = ContentScale.Fit,
        )
    } else {
        Image(
            provider = ImageProvider(R.drawable.widget_cover_placeholder),
            contentDescription = null,
            modifier = GlanceModifier.fillMaxSize(),
            contentScale = ContentScale.Fit,
            colorFilter = ColorFilter.tint(GlanceTheme.colors.surfaceVariant),
        )
    }
}

@Composable
private fun ProgressRow(content: CurrentlyReadingWidgetUiModel.Content.Reading) {
    val context = LocalContext.current

    Row(
        modifier = GlanceModifier
            .height(40.dp)
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
        ProgressBar(progress = content.progress)

        Spacer(modifier = GlanceModifier.width(Spacing.xxs))

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
private fun ProgressBar(progress: Map<Int, WavyProgressUris>) {
    val widthDp = progressBarWidthDp(LocalSize.current.width)
    val bars = progress[widthDp]

    Box(
        modifier = GlanceModifier
            .width(widthDp.dp)
            .height(PROGRESS_BAR_HEIGHT_DP.dp),
    ) {
        if (bars == null) return@Box

        UriMask(
            mask = bars.track,
            tint = GlanceTheme.colors.secondaryContainer,
            contentScale = ContentScale.Fit,
        )

        UriMask(
            mask = bars.indicator,
            tint = GlanceTheme.colors.primary,
            contentScale = ContentScale.Fit,
        )
    }
}

private fun cycleAction(
    canCycle: Boolean,
    step: Int,
): Action? = if (canCycle) {
    actionRunCallback<CycleBookCallback>(
        actionParametersOf(CycleBookCallback.StepKey to step),
    )
} else {
    null
}
