package nl.rhaydus.softcover.feature.widget.data.datastore

import android.content.Context
import androidx.glance.GlanceId
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
import nl.rhaydus.softcover.feature.widget.domain.model.RandomPickSource
import nl.rhaydus.softcover.feature.widget.domain.model.RandomPickState
import nl.rhaydus.softcover.feature.widget.domain.util.buildPool
import nl.rhaydus.softcover.feature.widget.domain.util.resolvePick
import nl.rhaydus.softcover.feature.widget.domain.util.resolveSource
import nl.rhaydus.softcover.feature.widget.domain.util.shufflePick
import nl.rhaydus.softcover.feature.widget.presentation.glance.RandomPickWidget
import kotlin.random.Random
import kotlin.time.Clock

internal class RandomPickStore(private val context: Context) {
    /** Emits the instance whose pick or settings changed; a live Glance session does not re-run `provideGlance` on `update`. */
    val changes = MutableSharedFlow<GlanceId>(
        extraBufferCapacity = 16,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    suspend fun settings(appWidgetId: Int): RandomPickSettings = getAppWidgetState(
        context = context,
        definition = PreferencesGlanceStateDefinition,
        glanceId = GlanceAppWidgetManager(context).getGlanceIdBy(appWidgetId),
    ).toRandomPickSettings()

    suspend fun saveSettings(
        appWidgetId: Int,
        settings: RandomPickSettings,
    ) {
        val glanceId = GlanceAppWidgetManager(context).getGlanceIdBy(appWidgetId)

        updateAppWidgetState(
            context = context,
            glanceId = glanceId,
        ) { preferences ->
            if (preferences.toRandomPickSettings() != settings) {
                preferences.write(settings)
                preferences.write(RandomPickState())
            }
        }

        changes.tryEmit(glanceId)

        RandomPickWidget().update(
            context = context,
            id = glanceId,
        )
    }

    /** Reads, re-picks and writes in one state edit, so concurrent callers cannot each pick a different book. */
    suspend fun resolve(
        glanceId: GlanceId,
        wantToRead: List<Book>,
        lists: List<BookList>,
        shuffle: Boolean,
    ): RandomPickResolution {
        var resolution = RandomPickResolution(
            book = null,
            source = RandomPickSource.WantToRead,
        )

        updateAppWidgetState(
            context = context,
            glanceId = glanceId,
        ) { preferences ->
            val settings = preferences.toRandomPickSettings()
            val state = preferences.toRandomPickState()
            val now = Clock.System.now()

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

            resolution = RandomPickResolution(
                book = pool.firstOrNull { it.id == next.pickedBookId },
                source = resolveSource(
                    source = settings.source,
                    lists = lists,
                ),
            )
        }

        return resolution
    }
}
