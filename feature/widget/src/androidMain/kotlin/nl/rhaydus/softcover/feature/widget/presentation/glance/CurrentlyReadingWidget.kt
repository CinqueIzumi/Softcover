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
import nl.rhaydus.softcover.core.presentation.navigation.TabDestination
import nl.rhaydus.softcover.feature.widget.presentation.mapper.currentlyReadingWidgetModels
import nl.rhaydus.softcover.feature.widget.presentation.model.CurrentlyReadingWidgetUiModel
import nl.rhaydus.softcover.feature.widget.presentation.render.BackdropArtworkRenderer
import nl.rhaydus.softcover.feature.widget.presentation.render.BookCoverLoader
import nl.rhaydus.softcover.feature.widget.presentation.render.renderWavyProgress
import nl.rhaydus.softcover.feature.widget.presentation.util.PROGRESS_BAR_HEIGHT_DP
import nl.rhaydus.softcover.feature.widget.presentation.util.WIDGET_WIDTHS_DP
import nl.rhaydus.softcover.feature.widget.presentation.util.portraitSizes
import nl.rhaydus.softcover.feature.widget.presentation.util.progressBarWidthsDp
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import kotlin.math.roundToInt

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

        val artwork = BackdropArtworkRenderer(density)

        val covers = BookCoverLoader(
            context = context,
            density = density,
        )

        val models = currentlyReadingWidgetModels(
            isSignedIn = environment.isSignedIn(),
            themeConfiguration = themeConfigurations,
            shelf = dependencies.getCurrentlyReadingUserBooks(),
            selectedBookId = dependencies.observeSelectedBookChanges()
                .onStart { emit(appWidgetId) }
                .filter { it == appWidgetId }
                .map { dependencies.getSelectedBookId(appWidgetId = appWidgetId).getOrThrow() },
            loadCover = covers::load,
            renderArtwork = artwork::render,
            barWidthsDp = progressBarWidthsDp(),
            renderProgress = { fraction, widthDp ->
                renderWavyProgress(
                    widthPx = (widthDp * density).roundToInt(),
                    heightPx = (PROGRESS_BAR_HEIGHT_DP * density).roundToInt(),
                    density = density,
                    fraction = fraction,
                )
            },
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
