package nl.rhaydus.softcover.feature.widget.presentation.glance

import android.content.Context
import android.os.Bundle
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.state.PreferencesGlanceStateDefinition
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import nl.rhaydus.common.AppLog
import nl.rhaydus.softcover.core.domain.model.TrendingPeriod
import nl.rhaydus.softcover.core.domain.model.UpcomingWindow
import nl.rhaydus.softcover.core.presentation.navigation.TabDestination
import nl.rhaydus.softcover.core.uibinding.cover.loadCoverBitmap
import nl.rhaydus.softcover.core.uibinding.cover.resolveCoverSource
import nl.rhaydus.softcover.feature.widget.R
import nl.rhaydus.softcover.feature.widget.domain.model.TrendingFeed
import nl.rhaydus.softcover.feature.widget.presentation.activity.TrendingSettingsActivity
import nl.rhaydus.softcover.feature.widget.presentation.activity.widgetSettingsIntent
import nl.rhaydus.softcover.feature.widget.presentation.image.WidgetImageFormat
import nl.rhaydus.softcover.feature.widget.presentation.mapper.trendingWidgetModels
import nl.rhaydus.softcover.feature.widget.presentation.model.TrendingWidgetUiModel
import nl.rhaydus.softcover.feature.widget.presentation.util.providerMinSize
import nl.rhaydus.softcover.feature.widget.presentation.util.reportedWidgetSizes
import nl.rhaydus.softcover.feature.widget.presentation.util.trendingCoverHeightDp
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import kotlin.math.roundToInt

internal class TrendingWidget : GlanceAppWidget(), KoinComponent {
    override val stateDefinition = PreferencesGlanceStateDefinition

    override val sizeMode = SizeMode.Exact

    override suspend fun provideGlance(
        context: Context,
        id: GlanceId,
    ) {
        val dependencies = get<TrendingWidgetDependencies>()
        val density = context.resources.displayMetrics.density

        val environment = dependencies.environment
        val appWidgetId = GlanceAppWidgetManager(context).getAppWidgetId(id)

        val openSettings = actionStartActivity(
            widgetSettingsIntent(
                context = context,
                activityClass = TrendingSettingsActivity::class.java,
                appWidgetId = appWidgetId,
            ),
        )

        val openExplore = environment.openTab(
            context = context,
            destination = TabDestination.EXPLORE,
        )

        val openApp = environment.openTab(
            context = context,
            destination = TabDestination.READING,
        )

        val openBook = environment.openBook(context)

        val themeConfigurations = environment.getThemeConfiguration()
        val initialTheme = environment.initialTheme(themeConfigurations)

        val models = { options: Flow<Bundle> ->
            trendingWidgetModels(
                isSignedIn = environment.isSignedIn(),
                themeConfiguration = themeConfigurations,
                position = dependencies.observeTrendingChanges()
                    .onStart { emit(appWidgetId) }
                    .filter { it == appWidgetId }
                    .map {
                        dependencies.getTrendingPosition(appWidgetId = appWidgetId)
                            .onFailure { error ->
                                AppLog.e(
                                    error,
                                    "Trending position unavailable",
                                )
                            }
                            .getOrNull()
                    }
                    .filterNotNull(),
                settingsChanges = dependencies.observeTrendingSettingsChanges()
                    .filter { it.appWidgetId == appWidgetId },
                observeTrending = { feed -> dependencies.observeTrendingFeed(feed = feed) },
                fetchStatus = { feed -> dependencies.observeTrendingFeedFetching(feed = feed) },
                eyebrowFor = { feed -> context.getString(feed.eyebrowRes()) },
                emptyHeadlineFor = { feed -> context.getString(feed.emptyHeadlineRes()) },
                coverUri = { book, coverHeightDp ->
                    val coverHeightPx = (coverHeightDp * density).roundToInt()

                    val source = resolveCoverSource(
                        edition = book.currentEdition,
                        defaultEdition = book.defaultEdition,
                        fallbackCoverUrl = book.coverUrl,
                    )

                    source?.let {
                        environment.imageStore.uriFor("trending-cover:v2:$it:$coverHeightPx", WidgetImageFormat.Photo) {
                            loadCoverBitmap(
                                context = context,
                                source = it,
                                sizePx = coverHeightPx,
                            )
                        }
                    }
                },
                coverHeightDp = options
                    .map { bundle ->
                        trendingCoverHeightDp(
                            bundle.reportedWidgetSizes { context.providerMinSize(appWidgetId) },
                        )
                    }
                    .distinctUntilChanged(),
            )
        }

        val loadingModel = TrendingWidgetUiModel(
            themeConfiguration = initialTheme,
            content = TrendingWidgetUiModel.Content.Loading,
        )

        provideSharedContent(
            models = models,
            initial = loadingModel,
        ) { model ->
            TrendingWidgetContent(
                model = model,
                openApp = openApp,
                openExplore = openExplore,
                openSettings = openSettings,
                openBook = openBook,
            )
        }
    }
}

private fun TrendingFeed.emptyHeadlineRes(): Int = when (this) {
    is TrendingFeed.Trending -> R.string.trending_empty_headline
    is TrendingFeed.Upcoming -> R.string.upcoming_empty_headline
}

internal fun TrendingFeed.eyebrowRes(): Int = when (this) {
    is TrendingFeed.Trending -> period.eyebrowRes()
    is TrendingFeed.Upcoming -> window.eyebrowRes()
}

private fun UpcomingWindow.eyebrowRes(): Int = when (this) {
    UpcomingWindow.Month -> R.string.upcoming_eyebrow_month
    UpcomingWindow.ThreeMonths -> R.string.upcoming_eyebrow_three_months
    UpcomingWindow.SixMonths -> R.string.upcoming_eyebrow_six_months
    UpcomingWindow.Year -> R.string.upcoming_eyebrow_year
}

private fun TrendingPeriod.eyebrowRes(): Int = when (this) {
    TrendingPeriod.Week -> R.string.trending_eyebrow_week
    TrendingPeriod.Month -> R.string.trending_eyebrow_month
    TrendingPeriod.ThreeMonths -> R.string.trending_eyebrow_three_months
    TrendingPeriod.Year -> R.string.trending_eyebrow_year
    TrendingPeriod.AllTime -> R.string.trending_eyebrow_all_time
}
