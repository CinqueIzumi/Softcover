package nl.rhaydus.softcover.feature.widget.randompick

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.glance.GlanceId
import androidx.glance.action.Action
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
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
import nl.rhaydus.softcover.feature.widget.backdrop.BackdropArtworkRenderer
import nl.rhaydus.softcover.feature.widget.backdrop.BookCoverLoader
import nl.rhaydus.softcover.feature.widget.settings.RandomPickSettingsActivity
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

internal class RandomPickWidget : GlanceAppWidget(), KoinComponent {
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
        val dependencies = get<RandomPickWidgetDependencies>()
        val density = context.resources.displayMetrics.density

        val openSettings = actionStartActivity(
            settingsIntent(
                context = context,
                appWidgetId = GlanceAppWidgetManager(context).getAppWidgetId(id),
            ),
        )

        val openApp = actionStartActivity(
            dependencies.appEntryPoint.intent(
                context = context,
                request = ExternalNavigationRequest.OpenTab(destination = TabDestination.READING),
            ),
        )

        val openBook: (Int) -> Action = { bookId ->
            actionStartActivity(
                dependencies.appEntryPoint.intent(
                    context = context,
                    request = ExternalNavigationRequest.OpenBook(bookId = bookId),
                ),
            )
        }

        val themeConfigurations = dependencies.getThemeConfiguration()
        val initialTheme = themeConfigurations.firstOrNull() ?: ThemeConfiguration()

        val artwork = BackdropArtworkRenderer(density)

        val covers = BookCoverLoader(
            context = context,
            density = density,
        )

        val models = randomPickWidgetModels(
            isSignedIn = dependencies.getUserIdAsFlow().map { it != NO_USER_ID },
            themeConfiguration = themeConfigurations,
            wantToRead = dependencies.getWantToReadUserBooks(),
            lists = dependencies.getAllUserLists(),
            changes = dependencies.store.changes
                .onStart { emit(id) }
                .filter { it == id }
                .map { },
            resolve = { wantToRead, lists ->
                dependencies.store.resolve(
                    glanceId = id,
                    wantToRead = wantToRead,
                    lists = lists,
                    shuffle = false,
                )
            },
            loadCover = covers::load,
            renderArtwork = artwork::render,
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

/** The data URI keeps each instance's PendingIntent distinct, since Intents differing only in extras share one. */
private fun settingsIntent(
    context: Context,
    appWidgetId: Int,
): Intent = Intent(
    context,
    RandomPickSettingsActivity::class.java,
).apply {
    action = AppWidgetManager.ACTION_APPWIDGET_CONFIGURE
    data = "softcover://widget/random-pick/$appWidgetId".toUri()
    putExtra(
        AppWidgetManager.EXTRA_APPWIDGET_ID,
        appWidgetId,
    )
}
