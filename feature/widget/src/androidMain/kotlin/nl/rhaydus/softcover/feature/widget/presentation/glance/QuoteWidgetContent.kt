package nl.rhaydus.softcover.feature.widget.presentation.glance

import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
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
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontFamily
import androidx.glance.text.FontStyle
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import nl.rhaydus.softcover.feature.widget.R
import nl.rhaydus.softcover.feature.widget.presentation.callback.CycleQuoteCallback
import nl.rhaydus.softcover.feature.widget.presentation.component.CYCLE_EYEBROW_HEIGHT_DP
import nl.rhaydus.softcover.feature.widget.presentation.component.CycleEyebrow
import nl.rhaydus.softcover.feature.widget.presentation.model.QuoteWidgetUiModel
import nl.rhaydus.softcover.feature.widget.presentation.theme.LocalWidgetExtraColors
import nl.rhaydus.softcover.feature.widget.presentation.theme.SoftcoverWidgetTheme
import nl.rhaydus.softcover.feature.widget.presentation.util.androidQuoteTextMeasurer
import nl.rhaydus.softcover.feature.widget.presentation.util.fitQuoteText
import nl.rhaydus.softcover.feature.widget.presentation.util.singleLineHeightPx

private const val BYLINE_FONT_SIZE_SP = 12

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
                QuoteWidgetUiModel.Content.Loading -> Unit

                QuoteWidgetUiModel.Content.SignedOut -> Message(
                    headline = context.getString(R.string.widget_sign_in),
                    subline = null,
                )

                QuoteWidgetUiModel.Content.Empty -> Message(
                    headline = context.getString(R.string.quote_empty_headline),
                    subline = context.getString(R.string.quote_empty_subline),
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
    }
}

@Composable
private fun Shown(
    content: QuoteWidgetUiModel.Content.Shown,
    openBook: Action,
) {
    val context = LocalContext.current
    val size = LocalSize.current
    val density = context.resources.displayMetrics.density

    val bylineHeightPx = if (content.byline != null) {
        singleLineHeightPx(
            context = context,
            fontSizeSp = BYLINE_FONT_SIZE_SP,
        ) + (Spacing.xxs.value * density).toInt()
    } else {
        0
    }

    val textFit = fitQuoteText(
        availableWidthPx = ((size.width - Spacing.xxs * 2 - Spacing.sm * 2).value * density).toInt(),
        availableHeightPx = ((size.height - Spacing.xxs * 2 - CYCLE_EYEBROW_HEIGHT_DP.dp).value * density)
            .toInt() - bylineHeightPx,
        measure = androidQuoteTextMeasurer(
            context = context,
            text = content.quoteText,
        ),
    )

    Box(modifier = GlanceModifier.fillMaxSize()) {
        Text(
            text = "“",
            modifier = GlanceModifier.padding(
                start = Spacing.xs,
                top = Spacing.xxs,
            ),
            style = TextStyle(
                color = LocalWidgetExtraColors.current.quoteGlyph,
                fontSize = 96.sp,
                fontFamily = FontFamily.Serif,
            ),
        )

        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .padding(Spacing.xxs),
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

            Column(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .defaultWeight()
                    .padding(horizontal = Spacing.sm)
                    .clickable(openBook),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = content.quoteText,
                    maxLines = textFit.maxLines,
                    style = TextStyle(
                        color = GlanceTheme.colors.onBackground,
                        fontSize = textFit.fontSizeSp.sp,
                        fontStyle = FontStyle.Italic,
                        fontFamily = FontFamily.Serif,
                    ),
                )

                if (content.byline != null) {
                    Spacer(modifier = GlanceModifier.height(Spacing.xxs))

                    Text(
                        text = content.byline,
                        maxLines = 1,
                        style = TextStyle(
                            color = GlanceTheme.colors.primary,
                            fontSize = BYLINE_FONT_SIZE_SP.sp,
                        ),
                    )
                }
            }
        }
    }
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
