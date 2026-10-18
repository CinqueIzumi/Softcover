package nl.rhaydus.softcover.feature.widget.presentation.glance

import android.content.Context
import android.content.res.Configuration
import android.graphics.Typeface
import android.os.Build
import android.text.TextPaint
import android.util.TypedValue
import android.widget.RemoteViews
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.toArgb
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
import androidx.glance.appwidget.AndroidRemoteViews
import androidx.glance.appwidget.CircularProgressIndicator
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontFamily
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import nl.rhaydus.softcover.feature.widget.R
import nl.rhaydus.softcover.feature.widget.presentation.callback.CycleQuoteCallback
import nl.rhaydus.softcover.feature.widget.presentation.callback.QuoteRetryCallback
import nl.rhaydus.softcover.feature.widget.presentation.component.BackdropUris
import nl.rhaydus.softcover.feature.widget.presentation.component.CYCLE_EYEBROW_HEIGHT_DP
import nl.rhaydus.softcover.feature.widget.presentation.component.CycleEyebrow
import nl.rhaydus.softcover.feature.widget.presentation.component.RefreshButton
import nl.rhaydus.softcover.feature.widget.presentation.model.QuoteWidgetUiModel
import nl.rhaydus.softcover.feature.widget.presentation.theme.LocalWidgetExtraColors
import nl.rhaydus.softcover.feature.widget.presentation.theme.SoftcoverWidgetTheme

private const val BYLINE_FONT_SIZE_SP = 12
private const val QUOTE_MIN_FONT_SIZE_SP = 12

@Composable
internal fun QuoteWidgetContent(
    model: QuoteWidgetUiModel,
    openApp: Action,
    openBook: (bookId: Int) -> Action,
) {
    val context = LocalContext.current

    SoftcoverWidgetTheme(themeConfiguration = model.themeConfiguration) {
        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .appWidgetBackground()
                .background(GlanceTheme.colors.background)
                .then(widgetCornerRadius())
                .clickable(openApp),
            contentAlignment = Alignment.Center,
        ) {
            when (val content = model.content) {
                QuoteWidgetUiModel.Content.Loading -> CircularProgressIndicator(color = GlanceTheme.colors.primary)

                QuoteWidgetUiModel.Content.SignedOut -> Message(
                    headline = context.getString(R.string.widget_sign_in),
                    subline = null,
                )

                QuoteWidgetUiModel.Content.Empty -> Message(
                    headline = context.getString(R.string.quote_empty_headline),
                    subline = context.getString(R.string.quote_empty_subline),
                )

                QuoteWidgetUiModel.Content.FetchFailed -> Message(
                    headline = context.getString(R.string.quote_failed_headline),
                    subline = context.getString(R.string.quote_failed_subline),
                    onRefresh = actionRunCallback<QuoteRetryCallback>(),
                )

                is QuoteWidgetUiModel.Content.Shown -> Shown(
                    content = content,
                    openBook = openBook(content.bookId),
                )
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
    onRefresh: Action? = null,
) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
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

        if (onRefresh != null) RefreshButton(onClick = onRefresh)
    }
}

@Composable
private fun Shown(
    content: QuoteWidgetUiModel.Content.Shown,
    openBook: Action,
) {
    val context = LocalContext.current
    val outerPadding = Spacing.xxs
    val rowTopPadding = Spacing.xxs
    val bylineGap = Spacing.xxs

    val bylineHeight = if (content.byline != null) {
        bylineGap + context.pxToDp(px = bylineLineHeightPx(context = context))
    } else {
        0.dp
    }

    val quoteHeight = LocalSize.current.height - outerPadding * 2 - CYCLE_EYEBROW_HEIGHT_DP.dp - rowTopPadding -
        bylineHeight

    val quoteView = quoteRemoteViews(
        context = context,
        text = content.quoteText,
        color = GlanceTheme.colors.onSurface,
        maxLines = quoteMaxLines(
            context = context,
            heightPx = (quoteHeight.value * context.resources.displayMetrics.density).toInt(),
        ),
    )

    Box(modifier = GlanceModifier.fillMaxSize()) {
        val artwork = content.artwork

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
                .padding(outerPadding),
        ) {
            CycleEyebrow(
                label = content.eyebrow,
                previous = cycleAction(
                    hasArrows = content.hasArrows,
                    step = -1,
                ),
                next = cycleAction(
                    hasArrows = content.hasArrows,
                    step = 1,
                ),
                previousDescription = context.getString(R.string.quote_previous),
                nextDescription = context.getString(R.string.quote_next),
            )

            Row(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .defaultWeight()
                    .padding(
                        start = Spacing.md,
                        top = rowTopPadding,
                    ),
            ) {
                Image(
                    provider = ImageProvider(R.drawable.ic_widget_quote_mark),
                    contentDescription = null,
                    modifier = GlanceModifier
                        .width(26.dp)
                        .height(20.dp),
                    colorFilter = ColorFilter.tint(LocalWidgetExtraColors.current.quoteGlyph),
                )

                Spacer(modifier = GlanceModifier.width(Spacing.xs))

                Column(
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .defaultWeight()
                        .padding(end = Spacing.sm)
                        .clickable(openBook),
                ) {
                    AndroidRemoteViews(
                        remoteViews = quoteView,
                        modifier = GlanceModifier
                            .fillMaxWidth()
                            .defaultWeight(),
                    )

                    if (content.byline != null) {
                        Spacer(modifier = GlanceModifier.height(bylineGap))

                        Text(
                            text = content.byline,
                            maxLines = 1,
                            style = TextStyle(
                                color = GlanceTheme.colors.onSurfaceVariant,
                                fontSize = BYLINE_FONT_SIZE_SP.sp,
                            ),
                        )
                    }
                }
            }
        }
    }
}

private fun quoteRemoteViews(
    context: Context,
    text: String,
    color: ColorProvider,
    maxLines: Int,
): RemoteViews {
    val views = RemoteViews(
        context.packageName,
        R.layout.widget_quote_text,
    )

    views.setTextViewText(
        R.id.widget_quote_text,
        text,
    )

    views.setInt(
        R.id.widget_quote_text,
        "setMaxLines",
        maxLines,
    )

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        views.setColorInt(
            R.id.widget_quote_text,
            "setTextColor",
            color.getColor(context.withNightMode(isNight = false)).toArgb(),
            color.getColor(context.withNightMode(isNight = true)).toArgb(),
        )
    } else {
        views.setTextColor(
            R.id.widget_quote_text,
            color.getColor(context).toArgb(),
        )
    }

    return views
}

private fun quoteMaxLines(
    context: Context,
    heightPx: Int,
): Int {
    val paint = TextPaint().apply {
        typeface = Typeface.create(
            Typeface.SERIF,
            Typeface.ITALIC,
        )
        textSize = context.spToPx(sp = QUOTE_MIN_FONT_SIZE_SP)
    }

    val lineHeightPx = paint.fontMetricsInt.descent - paint.fontMetricsInt.ascent

    return (heightPx / lineHeightPx.coerceAtLeast(1)).coerceAtLeast(1)
}

private fun bylineLineHeightPx(context: Context): Int {
    val paint = TextPaint().apply {
        textSize = context.spToPx(sp = BYLINE_FONT_SIZE_SP)
    }

    return paint.fontMetricsInt.bottom - paint.fontMetricsInt.top
}

private fun Context.spToPx(sp: Int): Float = TypedValue.applyDimension(
    TypedValue.COMPLEX_UNIT_SP,
    sp.toFloat(),
    resources.displayMetrics,
)

private fun Context.pxToDp(px: Int): Dp = (px / resources.displayMetrics.density).dp

private fun Context.withNightMode(isNight: Boolean): Context {
    val configuration = Configuration(resources.configuration)
    val nightFlag = if (isNight) Configuration.UI_MODE_NIGHT_YES else Configuration.UI_MODE_NIGHT_NO

    configuration.uiMode = (configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK.inv()) or nightFlag

    return createConfigurationContext(configuration)
}

private fun cycleAction(
    hasArrows: Boolean,
    step: Int,
): Action? = if (hasArrows) {
    actionRunCallback<CycleQuoteCallback>(
        actionParametersOf(CycleQuoteCallback.StepKey to step),
    )
} else {
    null
}
