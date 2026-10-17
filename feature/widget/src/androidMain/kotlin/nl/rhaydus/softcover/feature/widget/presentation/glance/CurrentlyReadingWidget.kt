package nl.rhaydus.softcover.feature.widget.presentation.glance

import android.content.Context
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.provideContent
import androidx.glance.state.PreferencesGlanceStateDefinition
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.transform
import nl.rhaydus.common.AppLog
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import nl.rhaydus.softcover.core.presentation.navigation.TabDestination
import nl.rhaydus.softcover.feature.widget.presentation.mapper.currentlyReadingWidgetModels
import nl.rhaydus.softcover.feature.widget.presentation.model.CurrentlyReadingWidgetUiModel
import nl.rhaydus.softcover.feature.widget.presentation.render.BackdropImages
import nl.rhaydus.softcover.feature.widget.presentation.util.PORTRAIT_HEIGHTS_DP
import nl.rhaydus.softcover.feature.widget.presentation.util.TALLEST_HEIGHT_DP
import nl.rhaydus.softcover.feature.widget.presentation.util.TALL_HEIGHT_DP
import nl.rhaydus.softcover.feature.widget.presentation.util.WIDGET_WIDTHS_DP
import nl.rhaydus.softcover.feature.widget.presentation.util.portraitSizes
import nl.rhaydus.softcover.feature.widget.presentation.util.progressBarWidthsDp
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

internal class CurrentlyReadingWidget : GlanceAppWidget(), KoinComponent {
    override val stateDefinition = PreferencesGlanceStateDefinition

    override val sizeMode = SizeMode.Responsive(
        portraitSizes(WIDGET_WIDTHS_DP),
    )

    override suspend fun provideGlance(
        context: Context,
        id: GlanceId,
    ) {
        val dependencies = get<CurrentlyReadingWidgetDependencies>()
        val density = context.resources.displayMetrics.density

        val environment = dependencies.environment
        val appWidgetId = GlanceAppWidgetManager(context).getAppWidgetId(id)

        val openReading = environment.openTab(
            context = context,
            destination = TabDestination.READING,
        )

        val themeConfigurations = environment.getThemeConfiguration()
        val initialTheme = environment.initialTheme(themeConfigurations)

        val images = CurrentlyReadingImages(
            store = environment.imageStore,
            context = context,
            backdrops = BackdropImages(
                context = context,
                density = density,
                store = environment.imageStore,
            ),
            density = density,
        )

        val models = currentlyReadingWidgetModels(
            isSignedIn = environment.isSignedIn(),
            themeConfiguration = themeConfigurations,
            shelf = dependencies.getCurrentlyReadingUserBooks(),
            selectedBookId = dependencies.observeSelectedBookChanges()
                .onStart { emit(appWidgetId) }
                .filter { it == appWidgetId }
                .transform {
                    dependencies.getSelectedBookId(appWidgetId = appWidgetId)
                        .onSuccess { id -> emit(id) }
                        .onFailure { error -> AppLog.e("Could not read the selected book: $error") }
                },
            coverUri = images::coverUri,
            barWidthsDp = progressBarWidthsDp(),
            progressUris = images::progressUris,
            artworkUris = images::artworkUris,
        )

        val loadingModel = CurrentlyReadingWidgetUiModel(
            themeConfiguration = initialTheme,
            content = CurrentlyReadingWidgetUiModel.Content.Loading,
        )

        provideContent {
            val model by models.collectAsState(initial = loadingModel)

            CurrentlyReadingWidgetContent(
                model = model,
                openReading = openReading,
            )
        }
    }
}

/**
 * The tallest cover slot any declared size shows. A layout stays in use until the next declared size, so each
 * bucket is measured at its upper bound, and the 2:3 cover is also limited by the width the slot has.
 */
internal val COVER_HEIGHT_DP = run {
    val heights = PORTRAIT_HEIGHTS_DP.drop(1).map { it - 1 } + (TALLEST_WIDGET_HEIGHT_DP - 1)
    val widths = WIDGET_WIDTHS_DP.drop(1).map { it - 1 } + WIDEST_WIDGET_WIDTH_DP

    widths.maxOf { width ->
        heights.maxOf { height ->
            minOf(
                coverSlotHeightDp(height),
                (width - 2 * Spacing.xxs.value.toInt()) * 3 / 2,
            )
        }
    }
}

private fun coverSlotHeightDp(widgetHeightDp: Int): Int {
    val padding = Spacing.xxs.value.toInt() * 2
    val chrome = padding + EYEBROW_DP + PROGRESS_ROW_DP

    val text = if (widgetHeightDp >= TALL_HEIGHT_DP) {
        val titleLines = if (widgetHeightDp >= TALLEST_HEIGHT_DP) 2 else 1
        Spacing.xxs.value.toInt() + titleLines * TITLE_LINE_DP + AUTHOR_DP
    } else {
        0
    }

    return (widgetHeightDp - chrome - text).coerceAtLeast(0)
}

private const val TALLEST_WIDGET_HEIGHT_DP = 320
private const val WIDEST_WIDGET_WIDTH_DP = 480
private const val EYEBROW_DP = 28
private const val PROGRESS_ROW_DP = 40
private const val TITLE_LINE_DP = 20
private const val AUTHOR_DP = 16
