package nl.rhaydus.softcover.feature.widget.randompick

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import kotlinx.coroutines.flow.firstOrNull
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

internal class ShuffleCallback : ActionCallback, KoinComponent {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters,
    ) {
        val dependencies = get<RandomPickWidgetDependencies>()

        val wantToRead = dependencies.getWantToReadUserBooks().firstOrNull() ?: return
        val lists = dependencies.getAllUserLists().firstOrNull() ?: return

        dependencies.store.resolve(
            glanceId = glanceId,
            wantToRead = wantToRead,
            lists = lists,
            shuffle = true,
        )

        dependencies.store.changes.tryEmit(glanceId)

        RandomPickWidget().update(
            context = context,
            id = glanceId,
        )
    }
}
