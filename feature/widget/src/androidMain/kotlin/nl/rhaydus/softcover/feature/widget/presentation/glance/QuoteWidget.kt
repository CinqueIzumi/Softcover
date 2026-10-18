package nl.rhaydus.softcover.feature.widget.presentation.glance

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.SizeMode
import androidx.glance.state.PreferencesGlanceStateDefinition
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import nl.rhaydus.common.AppLog
import nl.rhaydus.softcover.core.presentation.navigation.TabDestination
import nl.rhaydus.softcover.feature.widget.presentation.mapper.quoteWidgetModels
import nl.rhaydus.softcover.feature.widget.presentation.model.QuoteWidgetUiModel
import nl.rhaydus.softcover.feature.widget.presentation.render.BackdropImages
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

internal class QuoteWidget : GlanceAppWidget(), KoinComponent {
    override val stateDefinition = PreferencesGlanceStateDefinition

    override val sizeMode = SizeMode.Exact

    override suspend fun provideGlance(
        context: Context,
        id: GlanceId,
    ) {
        val dependencies = get<QuoteWidgetDependencies>()

        val environment = dependencies.environment
        val appWidgetId = GlanceAppWidgetManager(context).getAppWidgetId(id)

        val openApp = environment.openTab(
            context = context,
            destination = TabDestination.READING,
        )

        val openBook = environment.openBook(context)

        val themeConfigurations = environment.getThemeConfiguration()
        val initialTheme = environment.initialTheme(themeConfigurations)

        val images = QuoteImages(
            backdrops = BackdropImages(
                context = context,
                density = context.resources.displayMetrics.density,
                store = environment.imageStore,
            ),
        )

        val models = quoteWidgetModels(
            isSignedIn = environment.isSignedIn(),
            themeConfiguration = themeConfigurations,
            quotes = dependencies.observeAllQuotes(),
            selection = dependencies.observeQuoteSelectionChanges()
                .onStart { emit(appWidgetId) }
                .filter { it == appWidgetId }
                .map { dependencies.getQuoteSelection(appWidgetId = appWidgetId).getOrNull() },
            fetchState = dependencies.fetchTracker.state,
            artworkUris = images::artworkUris,
        ).catch { error ->
            AppLog.e(
                error,
                "Quote widget model flow failed",
            )
        }

        val loadingModel = QuoteWidgetUiModel(
            themeConfiguration = initialTheme,
            content = QuoteWidgetUiModel.Content.Loading,
        )

        provideSharedContent(
            models = models,
            initial = loadingModel,
        ) { model ->
            QuoteWidgetContent(
                model = model,
                openApp = openApp,
                openBook = openBook,
            )
        }
    }
}
