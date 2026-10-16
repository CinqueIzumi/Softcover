package nl.rhaydus.softcover.feature.widget.presentation.glance

import android.content.Context
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.action.Action
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.provideContent
import androidx.glance.state.PreferencesGlanceStateDefinition
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import nl.rhaydus.softcover.core.domain.model.NO_USER_ID
import nl.rhaydus.softcover.core.domain.model.ThemeConfiguration
import nl.rhaydus.softcover.core.domain.model.TrendingPeriod
import nl.rhaydus.softcover.core.presentation.navigation.ExternalNavigationRequest
import nl.rhaydus.softcover.core.presentation.navigation.TabDestination
import nl.rhaydus.softcover.feature.widget.R
import nl.rhaydus.softcover.feature.widget.presentation.activity.TrendingSettingsActivity
import nl.rhaydus.softcover.feature.widget.presentation.activity.widgetSettingsIntent
import nl.rhaydus.softcover.feature.widget.presentation.mapper.trendingWidgetModels
import nl.rhaydus.softcover.feature.widget.presentation.model.TrendingWidgetUiModel
import nl.rhaydus.softcover.feature.widget.presentation.render.BookCoverLoader
import nl.rhaydus.softcover.feature.widget.presentation.util.trendingCoverHeightDp
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

internal class TrendingWidget : GlanceAppWidget(), KoinComponent {
    override val stateDefinition = PreferencesGlanceStateDefinition

    override val sizeMode = SizeMode.Responsive(
        setOf(
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
        ),
    )

    override suspend fun provideGlance(
        context: Context,
        id: GlanceId,
    ) {
        val dependencies = get<TrendingWidgetDependencies>()
        val density = context.resources.displayMetrics.density

        val openSettings = actionStartActivity(
            widgetSettingsIntent(
                context = context,
                activityClass = TrendingSettingsActivity::class.java,
                appWidgetId = GlanceAppWidgetManager(context).getAppWidgetId(id),
            ),
        )

        val openExplore = actionStartActivity(
            dependencies.appEntryPoint.intent(
                context = context,
                request = ExternalNavigationRequest.OpenTab(destination = TabDestination.EXPLORE),
            ),
        )

        val openApp = actionStartActivity(
            dependencies.appEntryPoint.intent(
                context = context,
                request = ExternalNavigationRequest.OpenTab(destination = TabDestination.READING),
            ),
        )

        val openBook: (Int) -> Action = { bookId ->
            actionStartActivity(
                dependencies.appEntryPoint.intent(
                    context = context,
                    request = ExternalNavigationRequest.OpenBook(bookId = bookId),
                ),
            )
        }

        val themeConfigurations = dependencies.getThemeConfiguration()
        val initialTheme = themeConfigurations.firstOrNull() ?: ThemeConfiguration()

        val covers = BookCoverLoader(
            context = context,
            density = density,
            coverHeightDp = trendingCoverHeightDp(
                widgetHeightDp = 180,
                showsPageRow = false,
            ),
            capacity = COVER_CACHE_CAPACITY,
        )

        val models = trendingWidgetModels(
            isSignedIn = dependencies.getUserIdAsFlow().map { it != NO_USER_ID },
            themeConfiguration = themeConfigurations,
            position = dependencies.store.changes
                .onStart { emit(id) }
                .filter { it == id }
                .map { dependencies.store.position(id) },
            observeTrending = { period -> dependencies.observeTrendingBooks(period = period) },
            eyebrowFor = { period -> context.getString(period.eyebrowRes()) },
            loadCover = covers::load,
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

private const val COVER_CACHE_CAPACITY = 4

private fun TrendingPeriod.eyebrowRes(): Int = when (this) {
    TrendingPeriod.Week -> R.string.trending_eyebrow_week
    TrendingPeriod.Month -> R.string.trending_eyebrow_month
    TrendingPeriod.ThreeMonths -> R.string.trending_eyebrow_three_months
    TrendingPeriod.Year -> R.string.trending_eyebrow_year
    TrendingPeriod.AllTime -> R.string.trending_eyebrow_all_time
}
