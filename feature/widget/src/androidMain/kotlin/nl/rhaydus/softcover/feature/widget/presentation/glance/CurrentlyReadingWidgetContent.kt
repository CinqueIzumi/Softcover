package nl.rhaydus.softcover.feature.widget.presentation.glance

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
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.ContentScale
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.semantics.contentDescription
import androidx.glance.semantics.semantics
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import nl.rhaydus.softcover.feature.widget.R
import nl.rhaydus.softcover.feature.widget.presentation.activity.updateProgressIntent
import nl.rhaydus.softcover.feature.widget.presentation.callback.CycleBookCallback
import nl.rhaydus.softcover.feature.widget.presentation.component.CYCLE_EYEBROW_HEIGHT_DP
import nl.rhaydus.softcover.feature.widget.presentation.component.CycleEyebrow
import nl.rhaydus.softcover.feature.widget.presentation.component.SingleBookWidgetLayout
import nl.rhaydus.softcover.feature.widget.presentation.component.UriMask
import nl.rhaydus.softcover.feature.widget.presentation.component.WidgetMessage
import nl.rhaydus.softcover.feature.widget.presentation.component.widgetCornerRadius
import nl.rhaydus.softcover.feature.widget.presentation.model.CurrentlyReadingWidgetUiModel
import nl.rhaydus.softcover.feature.widget.presentation.render.WavyProgressUris
import nl.rhaydus.softcover.feature.widget.presentation.theme.LocalWidgetExtraColors
import nl.rhaydus.softcover.feature.widget.presentation.theme.SoftcoverWidgetTheme
import nl.rhaydus.softcover.feature.widget.presentation.util.PROGRESS_BAR_HEIGHT_DP
import nl.rhaydus.softcover.feature.widget.presentation.util.PROGRESS_LABEL_WIDTH_DP
import nl.rhaydus.softcover.feature.widget.presentation.util.PROGRESS_ROW_HEIGHT_DP
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
                        is CurrentlyReadingWidgetUiModel.Content.Reading ->
                            LocalWidgetExtraColors.current.surfaceContainerHigh
                        else -> GlanceTheme.colors.background
                    },
                )
                .then(widgetCornerRadius())
                .clickable(openReading),
            contentAlignment = Alignment.Center,
        ) {
            when (val content = model.content) {
                CurrentlyReadingWidgetUiModel.Content.Loading -> CircularProgressIndicator(
                    color = GlanceTheme.colors.primary,
                )

                CurrentlyReadingWidgetUiModel.Content.SignedOut -> WidgetMessage(
                    headline = context.getString(R.string.widget_sign_in),
                    subline = null,
                )

                CurrentlyReadingWidgetUiModel.Content.Empty -> WidgetMessage(
                    headline = context.getString(R.string.currently_reading_between_books),
                    subline = context.getString(R.string.currently_reading_between_books_subline),
                )

                is CurrentlyReadingWidgetUiModel.Content.Reading -> Reading(content)
            }
        }
    }
}

@Composable
private fun Reading(content: CurrentlyReadingWidgetUiModel.Content.Reading) {
    val context = LocalContext.current

    SingleBookWidgetLayout(
        title = content.title,
        author = content.author,
        cover = content.cover,
        eyebrowHeightDp = CYCLE_EYEBROW_HEIGHT_DP,
        footHeightDp = PROGRESS_ROW_HEIGHT_DP,
        artwork = content.artwork,
        eyebrow = {
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
        },
        foot = {
            ProgressRow(content = content)
        },
    )
}

@Composable
private fun ProgressRow(content: CurrentlyReadingWidgetUiModel.Content.Reading) {
    val context = LocalContext.current

    Row(
        modifier = GlanceModifier
            .height(PROGRESS_ROW_HEIGHT_DP.dp)
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
            maxLines = 1,
            style = TextStyle(
                color = GlanceTheme.colors.onSurface,
                fontSize = 12.sp,
            ),
            modifier = GlanceModifier.width(PROGRESS_LABEL_WIDTH_DP.dp),
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
    val widthDp = progressBarWidthDp(LocalSize.current)

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
