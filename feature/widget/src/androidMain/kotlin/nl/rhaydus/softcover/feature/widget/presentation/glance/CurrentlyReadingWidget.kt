package nl.rhaydus.softcover.feature.widget.presentation.glance

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.SizeMode
import androidx.glance.state.PreferencesGlanceStateDefinition
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.transform
import nl.rhaydus.common.AppLog
import nl.rhaydus.softcover.core.presentation.navigation.TabDestination
import nl.rhaydus.softcover.feature.widget.presentation.component.CYCLE_EYEBROW_HEIGHT_DP
import nl.rhaydus.softcover.feature.widget.presentation.mapper.currentlyReadingWidgetModels
import nl.rhaydus.softcover.feature.widget.presentation.model.CurrentlyReadingWidgetUiModel
import nl.rhaydus.softcover.feature.widget.presentation.render.BackdropImages
import nl.rhaydus.softcover.feature.widget.presentation.util.PROGRESS_ROW_HEIGHT_DP
import nl.rhaydus.softcover.feature.widget.presentation.util.progressBarWidthsDp
import nl.rhaydus.softcover.feature.widget.presentation.util.providerMinSize
import nl.rhaydus.softcover.feature.widget.presentation.util.reportedWidgetSizes
import nl.rhaydus.softcover.feature.widget.presentation.util.tallestCoverHeightDp
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

internal class CurrentlyReadingWidget : GlanceAppWidget(), KoinComponent {
    override val stateDefinition = PreferencesGlanceStateDefinition

    override val sizeMode = SizeMode.Exact

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

        val images = CurrentlyReadingImages(
            store = environment.imageStore,
            context = context,
            backdrops = BackdropImages(
                context = context,
                density = density,
                store = environment.imageStore,
            ),
            density = density,
        )

        val shelf = dependencies.getCurrentlyReadingUserBooks()

        val selectedBookId = dependencies.observeSelectedBookChanges()
            .onStart { emit(appWidgetId) }
            .filter { it == appWidgetId }
            .transform {
                dependencies.getSelectedBookId(appWidgetId = appWidgetId)
                    .onSuccess { id -> emit(id) }
                    .onFailure { error -> AppLog.e("Could not read the selected book: $error") }
            }

        val loadingModel = CurrentlyReadingWidgetUiModel(
            themeConfiguration = initialTheme,
            content = CurrentlyReadingWidgetUiModel.Content.Loading,
        )

        provideSharedContent(
            models = { options ->
                val reportedSizes = options.map { bundle ->
                    bundle.reportedWidgetSizes { context.providerMinSize(appWidgetId) }
                }

                currentlyReadingWidgetModels(
                    isSignedIn = environment.isSignedIn(),
                    themeConfiguration = themeConfigurations,
                    shelf = shelf,
                    selectedBookId = selectedBookId,
                    coverUri = images::coverUri,
                    coverHeightDp = reportedSizes
                        .map { sizes ->
                            tallestCoverHeightDp(
                                sizes = sizes,
                                eyebrowDp = { CYCLE_EYEBROW_HEIGHT_DP },
                                footDp = { PROGRESS_ROW_HEIGHT_DP },
                            )
                        }
                        .distinctUntilChanged(),
                    barWidthsDp = reportedSizes
                        .map { progressBarWidthsDp(it) }
                        .distinctUntilChanged(),
                    progressUris = images::progressUris,
                    artworkUris = images::artworkUris,
                )
            },
            initial = loadingModel,
        ) { model ->
            CurrentlyReadingWidgetContent(
                model = model,
                openReading = openReading,
            )
        }
    }
}
