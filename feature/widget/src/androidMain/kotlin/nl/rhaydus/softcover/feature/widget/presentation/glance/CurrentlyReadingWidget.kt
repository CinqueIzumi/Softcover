package nl.rhaydus.softcover.feature.widget.presentation.glance

import android.content.Context
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
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
import nl.rhaydus.softcover.core.presentation.navigation.ExternalNavigationRequest
import nl.rhaydus.softcover.core.presentation.navigation.TabDestination
import nl.rhaydus.softcover.feature.widget.data.datastore.readSelectedBookId
import nl.rhaydus.softcover.feature.widget.presentation.mapper.currentlyReadingWidgetModels
import nl.rhaydus.softcover.feature.widget.presentation.model.CurrentlyReadingWidgetUiModel
import nl.rhaydus.softcover.feature.widget.presentation.render.BackdropArtworkRenderer
import nl.rhaydus.softcover.feature.widget.presentation.render.BookCoverLoader
import nl.rhaydus.softcover.feature.widget.presentation.render.renderWavyProgress
import nl.rhaydus.softcover.feature.widget.presentation.util.WIDGET_WIDTHS_DP
import nl.rhaydus.softcover.feature.widget.presentation.util.progressBarWidthsDp
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import kotlin.math.roundToInt

internal class CurrentlyReadingWidget : GlanceAppWidget(), KoinComponent {
    override val stateDefinition = PreferencesGlanceStateDefinition

    override val sizeMode = SizeMode.Responsive(
        WIDGET_WIDTHS_DP
            .flatMap { width -> listOf(110, 180).map { height ->
                DpSize(
                    width = width.dp,
                    height = height.dp,
                )
            } }
            .toSet(),
    )

    override suspend fun provideGlance(
        context: Context,
        id: GlanceId,
    ) {
        val dependencies = get<CurrentlyReadingWidgetDependencies>()
        val density = context.resources.displayMetrics.density

        val openReading = actionStartActivity(
            dependencies.appEntryPoint.intent(
                context = context,
                request = ExternalNavigationRequest.OpenTab(destination = TabDestination.READING),
            ),
        )

        val themeConfigurations = dependencies.getThemeConfiguration()
        val initialTheme = themeConfigurations.firstOrNull() ?: ThemeConfiguration()

        val artwork = BackdropArtworkRenderer(density)

        val covers = BookCoverLoader(
            context = context,
            density = density,
        )

        val models = currentlyReadingWidgetModels(
            isSignedIn = dependencies.getUserIdAsFlow().map { it != NO_USER_ID },
            themeConfiguration = themeConfigurations,
            shelf = dependencies.getCurrentlyReadingUserBooks(),
            selectedBookId = dependencies.selectionChanges
                .onStart { emit(id) }
                .filter { it == id }
                .map { context.readSelectedBookId(id) },
            loadCover = covers::load,
            renderArtwork = artwork::render,
            barWidthsDp = progressBarWidthsDp(),
            renderProgress = { fraction, widthDp ->
                renderWavyProgress(
                    widthPx = (widthDp * density).roundToInt(),
                    heightPx = (12 * density).roundToInt(),
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
