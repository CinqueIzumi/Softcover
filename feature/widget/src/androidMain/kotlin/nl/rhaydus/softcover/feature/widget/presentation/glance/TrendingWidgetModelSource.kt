package nl.rhaydus.softcover.feature.widget.presentation.glance

import android.content.Context
import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.glance.LocalContext
import androidx.glance.appwidget.action.actionStartActivity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import nl.rhaydus.common.AppLog
import nl.rhaydus.softcover.core.presentation.navigation.TabDestination
import nl.rhaydus.softcover.core.uibinding.cover.loadCoverBitmap
import nl.rhaydus.softcover.core.uibinding.cover.resolveCoverSource
import nl.rhaydus.softcover.feature.widget.R
import nl.rhaydus.softcover.feature.widget.domain.model.TrendingFeed
import nl.rhaydus.softcover.feature.widget.domain.usecase.GetTrendingPositionUseCase
import nl.rhaydus.softcover.feature.widget.domain.usecase.ObserveTrendingChangesUseCase
import nl.rhaydus.softcover.feature.widget.domain.usecase.ObserveTrendingFeedFetchingUseCase
import nl.rhaydus.softcover.feature.widget.domain.usecase.ObserveTrendingFeedUseCase
import nl.rhaydus.softcover.feature.widget.domain.usecase.ObserveTrendingSettingsChangesUseCase
import nl.rhaydus.softcover.feature.widget.presentation.activity.TrendingSettingsActivity
import nl.rhaydus.softcover.feature.widget.presentation.activity.widgetSettingsIntent
import nl.rhaydus.softcover.feature.widget.presentation.image.WidgetImageFormat
import nl.rhaydus.softcover.feature.widget.presentation.mapper.trendingWidgetModels
import nl.rhaydus.softcover.feature.widget.presentation.model.TrendingWidgetUiModel
import nl.rhaydus.softcover.feature.widget.presentation.util.providerMinSize
import nl.rhaydus.softcover.feature.widget.presentation.util.reportedWidgetSizes
import nl.rhaydus.softcover.feature.widget.presentation.util.trendingCoverHeightDp
import kotlin.math.roundToInt

internal class TrendingWidgetModelSource(
    context: Context,
    private val environment: WidgetEnvironment,
    private val observeTrendingFeed: ObserveTrendingFeedUseCase,
    private val observeTrendingFeedFetching: ObserveTrendingFeedFetchingUseCase,
    private val getTrendingPosition: GetTrendingPositionUseCase,
    private val observeTrendingChanges: ObserveTrendingChangesUseCase,
    private val observeTrendingSettingsChanges: ObserveTrendingSettingsChangesUseCase,
) : WidgetModelSource<TrendingWidgetUiModel> {
    private val context: Context = context.applicationContext

    override suspend fun loadingModel(): TrendingWidgetUiModel = TrendingWidgetUiModel(
        themeConfiguration = environment.initialTheme(environment.getThemeConfiguration()),
        content = TrendingWidgetUiModel.Content.Loading,
    )

    override fun models(
        appWidgetId: Int,
        options: Flow<Bundle>,
    ): Flow<TrendingWidgetUiModel> {
        val density = context.resources.displayMetrics.density

        return trendingWidgetModels(
            isSignedIn = environment.isSignedIn(),
            themeConfiguration = environment.getThemeConfiguration(),
            position = observeTrendingChanges()
                .onStart { emit(appWidgetId) }
                .filter { it == appWidgetId }
                .map {
                    getTrendingPosition(appWidgetId = appWidgetId)
                        .onFailure { error ->
                            AppLog.e(
                                error,
                                "Trending position unavailable",
                            )
                        }
                        .getOrNull()
                }
                .filterNotNull(),
            settingsChanges = observeTrendingSettingsChanges()
                .filter { it.appWidgetId == appWidgetId },
            observeTrending = { feed -> observeTrendingFeed(feed = feed) },
            fetchStatus = { feed -> observeTrendingFeedFetching(feed = feed) },
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
                        sizes = bundle.reportedWidgetSizes { context.providerMinSize(appWidgetId) },
                        fontScale = context.resources.configuration.fontScale,
                    )
                }
                .distinctUntilChanged(),
        )
    }

    override fun isLoading(model: TrendingWidgetUiModel): Boolean =
        model.content is TrendingWidgetUiModel.Content.Loading

    override fun isSettled(model: TrendingWidgetUiModel): Boolean = when (val content = model.content) {
        is TrendingWidgetUiModel.Content.Loading,
        is TrendingWidgetUiModel.Content.Switching,
        -> false

        is TrendingWidgetUiModel.Content.Ranked -> content.items.none {
            it.cover is TrendingWidgetUiModel.Cover.Pending
        }

        is TrendingWidgetUiModel.Content.SignedOut,
        is TrendingWidgetUiModel.Content.Empty,
        is TrendingWidgetUiModel.Content.FetchFailed,
        -> true
    }

    @Composable
    override fun Content(
        appWidgetId: Int,
        model: TrendingWidgetUiModel,
    ) {
        val context = LocalContext.current

        val openSettings = remember(appWidgetId) {
            actionStartActivity(
                widgetSettingsIntent(
                    context = context,
                    activityClass = TrendingSettingsActivity::class.java,
                    appWidgetId = appWidgetId,
                ),
            )
        }

        val openExplore = remember {
            environment.openTab(
                context = context,
                destination = TabDestination.EXPLORE,
            )
        }

        val openApp = remember {
            environment.openTab(
                context = context,
                destination = TabDestination.READING,
            )
        }

        val openBook = remember { environment.openBook(context) }

        TrendingWidgetContent(
            model = model,
            openApp = openApp,
            openExplore = openExplore,
            openSettings = openSettings,
            openBook = openBook,
        )
    }
}

private fun TrendingFeed.emptyHeadlineRes(): Int = when (this) {
    is TrendingFeed.Trending -> R.string.trending_empty_headline
    is TrendingFeed.Upcoming -> R.string.upcoming_empty_headline
}
