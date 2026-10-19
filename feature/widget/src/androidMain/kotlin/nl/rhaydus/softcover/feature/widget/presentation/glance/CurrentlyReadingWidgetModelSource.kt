package nl.rhaydus.softcover.feature.widget.presentation.glance

import android.content.Context
import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.glance.LocalContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.transform
import nl.rhaydus.common.AppLog
import nl.rhaydus.softcover.core.book.domain.usecase.GetCurrentlyReadingUserBooksUseCase
import nl.rhaydus.softcover.core.presentation.navigation.TabDestination
import nl.rhaydus.softcover.feature.widget.domain.usecase.GetSelectedBookIdUseCase
import nl.rhaydus.softcover.feature.widget.domain.usecase.ObserveSelectedBookChangesUseCase
import nl.rhaydus.softcover.feature.widget.presentation.component.CYCLE_EYEBROW_HEIGHT_DP
import nl.rhaydus.softcover.feature.widget.presentation.mapper.currentlyReadingWidgetModels
import nl.rhaydus.softcover.feature.widget.presentation.model.CurrentlyReadingWidgetUiModel
import nl.rhaydus.softcover.feature.widget.presentation.render.BackdropImages
import nl.rhaydus.softcover.feature.widget.presentation.util.PROGRESS_ROW_HEIGHT_DP
import nl.rhaydus.softcover.feature.widget.presentation.util.progressBarWidthsDp
import nl.rhaydus.softcover.feature.widget.presentation.util.providerMinSize
import nl.rhaydus.softcover.feature.widget.presentation.util.reportedWidgetSizes
import nl.rhaydus.softcover.feature.widget.presentation.util.tallestCoverHeightDp

internal class CurrentlyReadingWidgetModelSource(
    context: Context,
    private val environment: WidgetEnvironment,
    private val getCurrentlyReadingUserBooks: GetCurrentlyReadingUserBooksUseCase,
    private val getSelectedBookId: GetSelectedBookIdUseCase,
    private val observeSelectedBookChanges: ObserveSelectedBookChangesUseCase,
) : WidgetModelSource<CurrentlyReadingWidgetUiModel> {
    private val context: Context = context.applicationContext

    override suspend fun loadingModel(): CurrentlyReadingWidgetUiModel = CurrentlyReadingWidgetUiModel(
        themeConfiguration = environment.initialTheme(environment.getThemeConfiguration()),
        content = CurrentlyReadingWidgetUiModel.Content.Loading,
    )

    override fun models(
        appWidgetId: Int,
        options: Flow<Bundle>,
    ): Flow<CurrentlyReadingWidgetUiModel> {
        val density = context.resources.displayMetrics.density

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

        val selectedBookId = observeSelectedBookChanges()
            .onStart { emit(appWidgetId) }
            .filter { it == appWidgetId }
            .transform {
                getSelectedBookId(appWidgetId = appWidgetId)
                    .onSuccess { id -> emit(id) }
                    .onFailure { error -> AppLog.e("Could not read the selected book: $error") }
            }

        val reportedSizes = options.map { bundle ->
            bundle.reportedWidgetSizes { context.providerMinSize(appWidgetId) }
        }

        return currentlyReadingWidgetModels(
            isSignedIn = environment.isSignedIn(),
            themeConfiguration = environment.getThemeConfiguration(),
            shelf = getCurrentlyReadingUserBooks(),
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
    }

    override fun isLoading(model: CurrentlyReadingWidgetUiModel): Boolean =
        model.content is CurrentlyReadingWidgetUiModel.Content.Loading

    override fun isSettled(model: CurrentlyReadingWidgetUiModel): Boolean = when (val content = model.content) {
        is CurrentlyReadingWidgetUiModel.Content.Loading -> false

        is CurrentlyReadingWidgetUiModel.Content.Reading -> content.imagesPending.not()

        is CurrentlyReadingWidgetUiModel.Content.SignedOut,
        is CurrentlyReadingWidgetUiModel.Content.Empty,
        -> true
    }

    @Composable
    override fun Content(
        appWidgetId: Int,
        model: CurrentlyReadingWidgetUiModel,
    ) {
        val context = LocalContext.current

        val openReading = remember {
            environment.openTab(
                context = context,
                destination = TabDestination.READING,
            )
        }

        CurrentlyReadingWidgetContent(
            model = model,
            openReading = openReading,
        )
    }
}
