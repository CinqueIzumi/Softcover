package nl.rhaydus.softcover.feature.widget.currentlyreading

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
import kotlin.math.roundToInt
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import nl.rhaydus.softcover.core.domain.model.NO_USER_ID
import nl.rhaydus.softcover.core.domain.model.ThemeConfiguration
import nl.rhaydus.softcover.core.presentation.navigation.ExternalNavigationRequest
import nl.rhaydus.softcover.core.presentation.navigation.TabDestination
import nl.rhaydus.softcover.feature.widget.backdrop.BackdropArtworkRenderer
import nl.rhaydus.softcover.feature.widget.backdrop.BookCoverLoader
import nl.rhaydus.softcover.feature.widget.progress.WavyProgressBitmaps
import nl.rhaydus.softcover.feature.widget.progress.renderWavyProgress
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

internal class CurrentlyReadingWidget : GlanceAppWidget(), KoinComponent {
    override val stateDefinition = PreferencesGlanceStateDefinition

    override val sizeMode = SizeMode.Responsive(
        setOf(
            DpSize(
                width = 110.dp,
                height = 110.dp,
            ),
            DpSize(
                width = 110.dp,
                height = 180.dp,
            ),
        ),
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

        var lastProgress: Pair<Float, WavyProgressBitmaps>? = null

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
            renderProgress = { fraction ->
                lastProgress?.takeIf { it.first == fraction }?.second
                    ?: renderWavyProgress(
                        widthPx = (BAR_WIDTH_DP * density).roundToInt(),
                        heightPx = (12 * density).roundToInt(),
                        density = density,
                        fraction = fraction,
                    ).also { lastProgress = fraction to it }
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

/** The wavy bar is drawn at exactly this width, never scaled; it fits a two-cell column in both layouts. */
internal const val BAR_WIDTH_DP = 94
