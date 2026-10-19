package nl.rhaydus.softcover.feature.widget.presentation.glance

import android.content.Context
import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.glance.LocalContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import nl.rhaydus.common.AppLog
import nl.rhaydus.softcover.core.personal.domain.usecase.ObserveAllQuotesUseCase
import nl.rhaydus.softcover.core.presentation.navigation.TabDestination
import nl.rhaydus.softcover.feature.widget.domain.model.QuoteFetchTracker
import nl.rhaydus.softcover.feature.widget.domain.usecase.GetQuoteSelectionUseCase
import nl.rhaydus.softcover.feature.widget.domain.usecase.ObserveQuoteSelectionChangesUseCase
import nl.rhaydus.softcover.feature.widget.presentation.mapper.quoteWidgetModels
import nl.rhaydus.softcover.feature.widget.presentation.model.QuoteWidgetUiModel
import nl.rhaydus.softcover.feature.widget.presentation.render.BackdropImages

internal class QuoteWidgetModelSource(
    context: Context,
    private val environment: WidgetEnvironment,
    private val observeAllQuotes: ObserveAllQuotesUseCase,
    private val getQuoteSelection: GetQuoteSelectionUseCase,
    private val observeQuoteSelectionChanges: ObserveQuoteSelectionChangesUseCase,
    private val fetchTracker: QuoteFetchTracker,
) : WidgetModelSource<QuoteWidgetUiModel> {
    private val context: Context = context.applicationContext

    override suspend fun loadingModel(): QuoteWidgetUiModel = QuoteWidgetUiModel(
        themeConfiguration = environment.initialTheme(environment.getThemeConfiguration()),
        content = QuoteWidgetUiModel.Content.Loading,
    )

    override fun models(
        appWidgetId: Int,
        options: Flow<Bundle>,
    ): Flow<QuoteWidgetUiModel> {
        val images = QuoteImages(
            backdrops = BackdropImages(
                context = context,
                density = context.resources.displayMetrics.density,
                store = environment.imageStore,
            ),
        )

        return quoteWidgetModels(
            isSignedIn = environment.isSignedIn(),
            themeConfiguration = environment.getThemeConfiguration(),
            quotes = observeAllQuotes(),
            selection = observeQuoteSelectionChanges()
                .onStart { emit(appWidgetId) }
                .filter { it == appWidgetId }
                .map { getQuoteSelection(appWidgetId = appWidgetId).getOrNull() },
            fetchState = fetchTracker.state,
            artworkUris = images::artworkUris,
        ).catch { error ->
            AppLog.e(
                error,
                "Quote widget model flow failed",
            )
        }
    }

    override fun isLoading(model: QuoteWidgetUiModel): Boolean =
        model.content is QuoteWidgetUiModel.Content.Loading

    override fun isSettled(model: QuoteWidgetUiModel): Boolean = when (val content = model.content) {
        is QuoteWidgetUiModel.Content.Loading -> false

        is QuoteWidgetUiModel.Content.Shown -> content.imagesPending.not()

        is QuoteWidgetUiModel.Content.SignedOut,
        is QuoteWidgetUiModel.Content.Empty,
        is QuoteWidgetUiModel.Content.FetchFailed,
        -> true
    }

    @Composable
    override fun Content(
        appWidgetId: Int,
        model: QuoteWidgetUiModel,
    ) {
        val context = LocalContext.current

        val openApp = remember {
            environment.openTab(
                context = context,
                destination = TabDestination.READING,
            )
        }

        val openBook = remember { environment.openBook(context) }

        QuoteWidgetContent(
            model = model,
            openApp = openApp,
            openBook = openBook,
        )
    }
}
