package nl.rhaydus.softcover.feature.widget.trending

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
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import nl.rhaydus.softcover.core.domain.model.NO_USER_ID
import nl.rhaydus.softcover.core.domain.model.ThemeConfiguration
import nl.rhaydus.softcover.core.presentation.navigation.ExternalNavigationRequest
import nl.rhaydus.softcover.core.presentation.navigation.TabDestination
import nl.rhaydus.softcover.feature.widget.backdrop.BookCoverLoader
import nl.rhaydus.softcover.feature.widget.settings.TrendingSettingsActivity
import nl.rhaydus.softcover.feature.widget.settings.widgetSettingsIntent
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
            coverHeightDp = 60,
            capacity = MAX_TRENDING_ITEMS,
        )

        val models = trendingWidgetModels(
            isSignedIn = dependencies.getUserIdAsFlow().map { it != NO_USER_ID },
            themeConfiguration = themeConfigurations,
            trending = dependencies.observeTrendingBooks(),
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
