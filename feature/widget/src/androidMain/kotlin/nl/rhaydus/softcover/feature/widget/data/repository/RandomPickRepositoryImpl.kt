package nl.rhaydus.softcover.feature.widget.data.repository

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.getAppWidgetState
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.datetime.TimeZone
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookList
import nl.rhaydus.softcover.feature.widget.data.mapper.toRandomPickSettings
import nl.rhaydus.softcover.feature.widget.data.mapper.toRandomPickState
import nl.rhaydus.softcover.feature.widget.data.mapper.write
import nl.rhaydus.softcover.feature.widget.domain.model.RandomPickResolution
import nl.rhaydus.softcover.feature.widget.domain.model.RandomPickSettings
import nl.rhaydus.softcover.feature.widget.domain.model.RandomPickSettingsChange
import nl.rhaydus.softcover.feature.widget.domain.model.RandomPickState
import nl.rhaydus.softcover.feature.widget.domain.repository.RandomPickRepository
import nl.rhaydus.softcover.feature.widget.domain.util.buildPool
import nl.rhaydus.softcover.feature.widget.domain.util.isSourceAvailable
import nl.rhaydus.softcover.feature.widget.domain.util.resolvePick
import nl.rhaydus.softcover.feature.widget.domain.util.shufflePick
import kotlin.random.Random
import kotlin.time.Clock

internal class RandomPickRepositoryImpl(private val context: Context) : RandomPickRepository {
    override val changes = MutableSharedFlow<Int>(
        extraBufferCapacity = 16,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    override val settingsChanges = MutableSharedFlow<RandomPickSettingsChange>(
        extraBufferCapacity = 16,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    override suspend fun getSettings(appWidgetId: Int): RandomPickSettings = getAppWidgetState(
        context = context,
        definition = PreferencesGlanceStateDefinition,
        glanceId = GlanceAppWidgetManager(context).getGlanceIdBy(appWidgetId),
    ).toRandomPickSettings()

    override suspend fun getState(appWidgetId: Int): RandomPickState = getAppWidgetState(
        context = context,
        definition = PreferencesGlanceStateDefinition,
        glanceId = GlanceAppWidgetManager(context).getGlanceIdBy(appWidgetId),
    ).toRandomPickState()

    override suspend fun saveSettings(
        appWidgetId: Int,
        settings: RandomPickSettings,
    ) {
        var changed = false

        updateAppWidgetState(
            context = context,
            glanceId = GlanceAppWidgetManager(context).getGlanceIdBy(appWidgetId),
        ) { preferences ->
            if (preferences.toRandomPickSettings() != settings) {
                changed = true
                preferences.write(settings)
                preferences.write(RandomPickState())
            }
        }

        if (changed) {
            settingsChanges.tryEmit(
                RandomPickSettingsChange(
                    appWidgetId = appWidgetId,
                    settings = settings,
                ),
            )
        }

        changes.tryEmit(appWidgetId)
    }

    override suspend fun resolve(
        appWidgetId: Int,
        wantToRead: List<Book>,
        lists: List<BookList>,
    ): RandomPickResolution = pick(
        appWidgetId = appWidgetId,
        wantToRead = wantToRead,
        lists = lists,
        shuffle = false,
    )

    override suspend fun shuffle(
        appWidgetId: Int,
        wantToRead: List<Book>,
        lists: List<BookList>,
    ) {
        pick(
            appWidgetId = appWidgetId,
            wantToRead = wantToRead,
            lists = lists,
            shuffle = true,
        )

        changes.tryEmit(appWidgetId)
    }

    /** Reads, re-picks and writes in one state edit, so concurrent callers cannot each pick a different book. */
    private suspend fun pick(
        appWidgetId: Int,
        wantToRead: List<Book>,
        lists: List<BookList>,
        shuffle: Boolean,
    ): RandomPickResolution {
        var resolution: RandomPickResolution = RandomPickResolution.SourceUnavailable

        updateAppWidgetState(
            context = context,
            glanceId = GlanceAppWidgetManager(context).getGlanceIdBy(appWidgetId),
        ) { preferences ->
            val settings = preferences.toRandomPickSettings()
            val state = preferences.toRandomPickState()
            val now = Clock.System.now()

            val sourceAvailable = isSourceAvailable(
                source = settings.source,
                lists = lists,
            )

            if (sourceAvailable.not()) {
                resolution = RandomPickResolution.SourceUnavailable
                return@updateAppWidgetState
            }

            val pool = buildPool(
                source = settings.source,
                wantToRead = wantToRead,
                lists = lists,
            )

            val next = if (shuffle) {
                shufflePick(
                    state = state,
                    pool = pool,
                    now = now,
                    random = Random.Default,
                )
            } else {
                resolvePick(
                    state = state,
                    cadence = settings.cadence,
                    pool = pool,
                    now = now,
                    timeZone = TimeZone.currentSystemDefault(),
                    random = Random.Default,
                )
            }

            if (next != state) preferences.write(next)

            resolution = RandomPickResolution.Resolved(
                pick = pool.firstOrNull { it.book.id == next.pickedBookId },
                source = settings.source,
            )
        }

        return resolution
    }
}
