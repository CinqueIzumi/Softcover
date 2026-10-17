package nl.rhaydus.softcover.feature.widget.presentation.glance

import android.content.Context
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.provideContent
import androidx.glance.state.PreferencesGlanceStateDefinition
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import nl.rhaydus.common.AppLog
import nl.rhaydus.softcover.core.domain.model.TrendingPeriod
import nl.rhaydus.softcover.core.presentation.navigation.TabDestination
import nl.rhaydus.softcover.core.uibinding.cover.loadCoverBitmap
import nl.rhaydus.softcover.core.uibinding.cover.resolveCoverSource
import nl.rhaydus.softcover.feature.widget.R
import nl.rhaydus.softcover.feature.widget.presentation.activity.TrendingSettingsActivity
import nl.rhaydus.softcover.feature.widget.presentation.activity.widgetSettingsIntent
import nl.rhaydus.softcover.feature.widget.presentation.mapper.trendingWidgetModels
import nl.rhaydus.softcover.feature.widget.presentation.model.TrendingWidgetUiModel
import nl.rhaydus.softcover.feature.widget.presentation.util.trendingCoverHeightDp
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import kotlin.math.roundToInt

internal class TrendingWidget : GlanceAppWidget(), KoinComponent {
    override val stateDefinition = PreferencesGlanceStateDefinition

    override val sizeMode = SizeMode.Responsive(TRENDING_SIZES)

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

        val coverHeightPx = (COVER_BITMAP_HEIGHT_DP * density).roundToInt()

        val models = trendingWidgetModels(
            isSignedIn = environment.isSignedIn(),
            themeConfiguration = themeConfigurations,
            position = dependencies.observeTrendingChanges()
                .onStart { emit(appWidgetId) }
                .filter { it == appWidgetId }
                .map {
                    dependencies.getTrendingPosition(appWidgetId = appWidgetId)
                        .onFailure { error -> AppLog.e(error, "Trending position unavailable") }
                        .getOrNull()
                }
                .filterNotNull(),
            observeTrending = { period -> dependencies.observeTrendingBooks(period = period) },
            eyebrowFor = { period -> context.getString(period.eyebrowRes()) },
            coverUri = { book ->
                val source = resolveCoverSource(
                    edition = book.currentEdition,
                    defaultEdition = book.defaultEdition,
                    fallbackCoverUrl = book.coverUrl,
                )

                source?.let {
                    environment.imageStore.uriFor("trending-cover:$it:$coverHeightPx") {
                        loadCoverBitmap(
                            context = context,
                            source = it,
                            sizePx = coverHeightPx,
                        )
                    }
                }
            },
        )

        val loadingModel = TrendingWidgetUiModel(
            themeConfiguration = initialTheme,
            content = TrendingWidgetUiModel.Content.Loading,
        )

        provideContent {
            val model by models.collectAsState(initial = loadingModel)

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

private val TRENDING_SIZES = setOf(
    DpSize(
        width = 250.dp,
        height = 110.dp,
    ),
    DpSize(
        width = 320.dp,
        height = 110.dp,
    ),
    DpSize(
        width = 250.dp,
        height = 180.dp,
    ),
    DpSize(
        width = 320.dp,
        height = 180.dp,
    ),
    DpSize(
        width = 250.dp,
        height = 250.dp,
    ),
    DpSize(
        width = 250.dp,
        height = 320.dp,
    ),
    DpSize(
        width = 320.dp,
        height = 250.dp,
    ),
    DpSize(
        width = 320.dp,
        height = 320.dp,
    ),
)

private val COVER_BITMAP_HEIGHT_DP = TRENDING_SIZES.maxOf { size ->
    trendingCoverHeightDp(
        widgetHeightDp = size.height.value.toInt(),
        widgetWidthDp = size.width.value.toInt(),
        showsPageRow = false,
    )
}

private fun TrendingPeriod.eyebrowRes(): Int = when (this) {
    TrendingPeriod.Week -> R.string.trending_eyebrow_week
    TrendingPeriod.Month -> R.string.trending_eyebrow_month
    TrendingPeriod.ThreeMonths -> R.string.trending_eyebrow_three_months
    TrendingPeriod.Year -> R.string.trending_eyebrow_year
    TrendingPeriod.AllTime -> R.string.trending_eyebrow_all_time
}
