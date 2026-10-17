package nl.rhaydus.softcover.feature.widget.presentation.glance

import android.content.Context
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.provideContent
import androidx.glance.state.PreferencesGlanceStateDefinition
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import nl.rhaydus.softcover.core.presentation.navigation.TabDestination
import nl.rhaydus.softcover.feature.widget.presentation.activity.RandomPickSettingsActivity
import nl.rhaydus.softcover.feature.widget.presentation.activity.widgetSettingsIntent
import nl.rhaydus.softcover.feature.widget.presentation.mapper.randomPickWidgetModels
import nl.rhaydus.softcover.feature.widget.presentation.model.RandomPickWidgetUiModel
import nl.rhaydus.softcover.feature.widget.presentation.render.RandomPickImages
import nl.rhaydus.softcover.feature.widget.presentation.util.WIDGET_WIDTHS_DP
import nl.rhaydus.softcover.feature.widget.presentation.util.portraitSizes
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

internal class RandomPickWidget : GlanceAppWidget(), KoinComponent {
    override val stateDefinition = PreferencesGlanceStateDefinition

    override val sizeMode = SizeMode.Responsive(
        portraitSizes(listOf(WIDGET_WIDTHS_DP.first())),
    )

    override suspend fun provideGlance(
        context: Context,
        id: GlanceId,
    ) {
        val dependencies = get<RandomPickWidgetDependencies>()
        val density = context.resources.displayMetrics.density

        val environment = dependencies.environment
        val appWidgetId = GlanceAppWidgetManager(context).getAppWidgetId(id)

        val openSettings = actionStartActivity(
            widgetSettingsIntent(
                context = context,
                activityClass = RandomPickSettingsActivity::class.java,
                appWidgetId = appWidgetId,
            ),
        )

        val openApp = environment.openTab(
            context = context,
            destination = TabDestination.READING,
        )

        val openBook = environment.openBook(context)

        val themeConfigurations = environment.getThemeConfiguration()
        val initialTheme = environment.initialTheme(themeConfigurations)

        val images = RandomPickImages(
            context = context,
            density = density,
            store = environment.imageStore,
        )

        val models = randomPickWidgetModels(
            isSignedIn = environment.isSignedIn(),
            themeConfiguration = themeConfigurations,
            wantToRead = dependencies.getWantToReadUserBooks(),
            lists = dependencies.getAllUserLists(),
            changes = dependencies.observeRandomPickChanges()
                .onStart { emit(appWidgetId) }
                .filter { it == appWidgetId }
                .map { },
            settingsChanges = dependencies.observeRandomPickSettingsChanges()
                .filter { it.appWidgetId == appWidgetId },
            resolve = { wantToRead, lists ->
                dependencies.resolveRandomPick(
                    appWidgetId = appWidgetId,
                    wantToRead = wantToRead,
                    lists = lists,
                )
            },
            coverUri = images::coverUri,
            artwork = images::artwork,
        )

        val loadingModel = RandomPickWidgetUiModel(
            themeConfiguration = initialTheme,
            content = RandomPickWidgetUiModel.Content.Loading,
        )

        provideContent {
            val model by models.collectAsState(initial = loadingModel)

            RandomPickWidgetContent(
                model = model,
                openApp = openApp,
                openSettings = openSettings,
                openBook = openBook,
            )
        }
    }
}
