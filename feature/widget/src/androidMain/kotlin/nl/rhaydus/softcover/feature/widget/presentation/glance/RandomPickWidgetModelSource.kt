package nl.rhaydus.softcover.feature.widget.presentation.glance

import android.content.Context
import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.glance.LocalContext
import androidx.glance.appwidget.action.actionStartActivity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import nl.rhaydus.softcover.core.book.domain.usecase.GetWantToReadUserBooksUseCase
import nl.rhaydus.softcover.core.lists.domain.usecase.GetAllUserListsUseCase
import nl.rhaydus.softcover.core.presentation.navigation.TabDestination
import nl.rhaydus.softcover.feature.widget.domain.usecase.ObserveRandomPickChangesUseCase
import nl.rhaydus.softcover.feature.widget.domain.usecase.ObserveRandomPickSettingsChangesUseCase
import nl.rhaydus.softcover.feature.widget.domain.usecase.ResolveRandomPickUseCase
import nl.rhaydus.softcover.feature.widget.presentation.activity.RandomPickSettingsActivity
import nl.rhaydus.softcover.feature.widget.presentation.activity.widgetSettingsIntent
import nl.rhaydus.softcover.feature.widget.presentation.mapper.randomPickWidgetModels
import nl.rhaydus.softcover.feature.widget.presentation.model.RandomPickWidgetUiModel
import nl.rhaydus.softcover.feature.widget.presentation.render.RandomPickImages
import nl.rhaydus.softcover.feature.widget.presentation.util.providerMinSize
import nl.rhaydus.softcover.feature.widget.presentation.util.randomPickCoverHeightDp
import nl.rhaydus.softcover.feature.widget.presentation.util.reportedWidgetSizes

internal class RandomPickWidgetModelSource(
    context: Context,
    private val environment: WidgetEnvironment,
    private val getWantToReadUserBooks: GetWantToReadUserBooksUseCase,
    private val getAllUserLists: GetAllUserListsUseCase,
    private val resolveRandomPick: ResolveRandomPickUseCase,
    private val observeRandomPickChanges: ObserveRandomPickChangesUseCase,
    private val observeRandomPickSettingsChanges: ObserveRandomPickSettingsChangesUseCase,
) : WidgetModelSource<RandomPickWidgetUiModel> {
    private val context: Context = context.applicationContext

    override suspend fun loadingModel(): RandomPickWidgetUiModel = RandomPickWidgetUiModel(
        themeConfiguration = environment.initialTheme(environment.getThemeConfiguration()),
        content = RandomPickWidgetUiModel.Content.Loading,
    )

    override fun models(
        appWidgetId: Int,
        options: Flow<Bundle>,
    ): Flow<RandomPickWidgetUiModel> {
        val images = RandomPickImages(
            context = context,
            density = context.resources.displayMetrics.density,
            store = environment.imageStore,
        )

        return randomPickWidgetModels(
            isSignedIn = environment.isSignedIn(),
            themeConfiguration = environment.getThemeConfiguration(),
            wantToRead = getWantToReadUserBooks(),
            lists = getAllUserLists(),
            changes = observeRandomPickChanges()
                .onStart { emit(appWidgetId) }
                .filter { it == appWidgetId }
                .map { },
            settingsChanges = observeRandomPickSettingsChanges()
                .filter { it.appWidgetId == appWidgetId },
            resolve = { wantToRead, lists ->
                resolveRandomPick(
                    appWidgetId = appWidgetId,
                    wantToRead = wantToRead,
                    lists = lists,
                )
            },
            coverUri = images::coverUri,
            coverHeightDp = options
                .map { bundle ->
                    randomPickCoverHeightDp(
                        bundle.reportedWidgetSizes { context.providerMinSize(appWidgetId) },
                    )
                }
                .distinctUntilChanged(),
            artwork = images::artwork,
        )
    }

    override fun isLoading(model: RandomPickWidgetUiModel): Boolean =
        model.content is RandomPickWidgetUiModel.Content.Loading

    override fun isSettled(model: RandomPickWidgetUiModel): Boolean = when (val content = model.content) {
        is RandomPickWidgetUiModel.Content.Loading,
        is RandomPickWidgetUiModel.Content.Switching,
        -> false

        is RandomPickWidgetUiModel.Content.Picked -> content.imagesPending.not()

        is RandomPickWidgetUiModel.Content.SignedOut,
        is RandomPickWidgetUiModel.Content.SourceUnavailable,
        is RandomPickWidgetUiModel.Content.EmptyPool,
        -> true
    }

    @Composable
    override fun Content(
        appWidgetId: Int,
        model: RandomPickWidgetUiModel,
    ) {
        val context = LocalContext.current

        val openSettings = remember(appWidgetId) {
            actionStartActivity(
                widgetSettingsIntent(
                    context = context,
                    activityClass = RandomPickSettingsActivity::class.java,
                    appWidgetId = appWidgetId,
                ),
            )
        }

        val openApp = remember {
            environment.openTab(
                context = context,
                destination = TabDestination.READING,
            )
        }

        val openBook = remember { environment.openBook(context) }

        RandomPickWidgetContent(
            model = model,
            openApp = openApp,
            openSettings = openSettings,
            openBook = openBook,
        )
    }
}
