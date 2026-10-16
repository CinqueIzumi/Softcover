package nl.rhaydus.softcover.feature.widget.presentation.glance

import android.graphics.Bitmap
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
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.ContentScale
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
import nl.rhaydus.softcover.core.designsystem.presentation.theme.CornerRadius
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import nl.rhaydus.softcover.feature.widget.R
import nl.rhaydus.softcover.feature.widget.presentation.callback.ShuffleCallback
import nl.rhaydus.softcover.feature.widget.presentation.component.Backdrop
import nl.rhaydus.softcover.feature.widget.presentation.model.RandomPickWidgetUiModel
import nl.rhaydus.softcover.feature.widget.presentation.theme.LocalWidgetExtraColors
import nl.rhaydus.softcover.feature.widget.presentation.theme.SoftcoverWidgetTheme

@Composable
internal fun RandomPickWidgetContent(
    model: RandomPickWidgetUiModel,
    openApp: Action,
    openSettings: Action,
    openBook: (bookId: Int) -> Action,
) {
    val context = LocalContext.current

    SoftcoverWidgetTheme(themeConfiguration = model.themeConfiguration) {
        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .appWidgetBackground()
                .background(LocalWidgetExtraColors.current.surfaceContainerHigh)
                .then(widgetCornerRadius())
                .clickable(
                    when (val content = model.content) {
                        is RandomPickWidgetUiModel.Content.Picked -> openBook(content.bookId)
                        else -> openApp
                    },
                ),
            contentAlignment = Alignment.Center,
        ) {
            when (val content = model.content) {
                RandomPickWidgetUiModel.Content.Loading -> Unit

                RandomPickWidgetUiModel.Content.SignedOut -> Message(
                    eyebrow = null,
                    headline = context.getString(R.string.widget_sign_in),
                    subline = null,
                    openSettings = openSettings,
                )

                is RandomPickWidgetUiModel.Content.EmptyPool -> Message(
                    eyebrow = content.eyebrow,
                    headline = context.getString(R.string.random_pick_empty_headline),
                    subline = context.getString(R.string.random_pick_empty_subline),
                    openSettings = openSettings,
                )

                is RandomPickWidgetUiModel.Content.Picked -> PickedBook(
                    content = content,
                    isTall = LocalSize.current.height >= 150.dp,
                    openSettings = openSettings,
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
    eyebrow: String?,
    headline: String,
    subline: String?,
    openSettings: Action,
) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .padding(Spacing.xxs),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (eyebrow != null) {
            Eyebrow(
                text = eyebrow,
                openSettings = openSettings,
            )
        }

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
}

@Composable
private fun PickedBook(
    content: RandomPickWidgetUiModel.Content.Picked,
    isTall: Boolean,
    openSettings: Action,
) {
    val artwork = content.artwork

    Box(modifier = GlanceModifier.fillMaxSize()) {
        if (artwork != null) {
            Backdrop(
                backdrop = if (isTall) artwork.tallBackdrop else artwork.compactBackdrop,
                artwork = artwork,
            )
        }

        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .padding(Spacing.xxs),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Eyebrow(
                text = content.eyebrow,
                openSettings = openSettings,
            )

            Box(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .defaultWeight(),
                contentAlignment = Alignment.Center,
            ) {
                Cover(cover = content.cover)
            }

            if (isTall) {
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
            }

            ShuffleButton()
        }
    }
}

@Composable
private fun Eyebrow(
    text: String,
    openSettings: Action,
) {
    Box(
        modifier = GlanceModifier
            .fillMaxWidth()
            .height(28.dp)
            .clickable(openSettings),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            maxLines = 1,
            style = TextStyle(
                color = GlanceTheme.colors.onSurfaceVariant,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            ),
        )
    }
}

@Composable
private fun Cover(cover: Bitmap?) {
    if (cover != null) {
        Image(
            provider = ImageProvider(cover),
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

@Composable
private fun ShuffleButton() {
    val description = LocalContext.current.getString(R.string.random_pick_shuffle)

    Box(
        modifier = GlanceModifier
            .size(
                width = 44.dp,
                height = 40.dp,
            )
            .semantics { contentDescription = description }
            .clickable(actionRunCallback<ShuffleCallback>()),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            provider = ImageProvider(R.drawable.ic_widget_shuffle),
            contentDescription = null,
            modifier = GlanceModifier.size(20.dp),
            colorFilter = ColorFilter.tint(GlanceTheme.colors.onSurface),
        )
    }
}
