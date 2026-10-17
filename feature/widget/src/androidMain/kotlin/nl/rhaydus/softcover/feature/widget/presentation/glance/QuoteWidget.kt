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
import androidx.glance.appwidget.provideContent
import androidx.glance.state.PreferencesGlanceStateDefinition
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import nl.rhaydus.softcover.core.presentation.navigation.TabDestination
import nl.rhaydus.softcover.feature.widget.presentation.mapper.quoteWidgetModels
import nl.rhaydus.softcover.feature.widget.presentation.model.QuoteWidgetUiModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

private val QUOTE_WIDTHS_DP = listOf(250, 320, 390)
private val QUOTE_HEIGHTS_DP = listOf(110, 180, 250)

private val QUOTE_SIZES = QUOTE_WIDTHS_DP
    .flatMap { width ->
        QUOTE_HEIGHTS_DP.map { height ->
            DpSize(
                width = width.dp,
                height = height.dp,
            )
        }
    }
    .toSet()

internal class QuoteWidget : GlanceAppWidget(), KoinComponent {
    override val stateDefinition = PreferencesGlanceStateDefinition

    override val sizeMode = SizeMode.Responsive(QUOTE_SIZES)

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

        val models = quoteWidgetModels(
            isSignedIn = environment.isSignedIn(),
            themeConfiguration = themeConfigurations,
            quotes = dependencies.observeAllQuotes(),
            selection = dependencies.observeQuoteSelectionChanges()
                .onStart { emit(appWidgetId) }
                .filter { it == appWidgetId }
                .map { dependencies.getQuoteSelection(appWidgetId = appWidgetId).getOrNull() },
        )

        val loadingModel = QuoteWidgetUiModel(
            themeConfiguration = initialTheme,
            content = QuoteWidgetUiModel.Content.Loading,
        )

        provideContent {
            val model by models.collectAsState(initial = loadingModel)

            QuoteWidgetContent(
                model = model,
                openApp = openApp,
                openBook = openBook,
            )
        }
    }
}
