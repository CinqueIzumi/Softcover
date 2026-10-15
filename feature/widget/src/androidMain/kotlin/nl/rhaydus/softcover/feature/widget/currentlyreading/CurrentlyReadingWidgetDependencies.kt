package nl.rhaydus.softcover.feature.widget.currentlyreading

import androidx.glance.GlanceId
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import nl.rhaydus.softcover.core.book.domain.usecase.GetCurrentlyReadingUserBooksUseCase
import nl.rhaydus.softcover.core.identity.domain.usecase.GetUserIdAsFlowUseCase
import nl.rhaydus.softcover.core.preferences.domain.usecase.GetThemeConfigurationUseCase
import nl.rhaydus.softcover.core.presentation.navigation.AppEntryPoint

internal class CurrentlyReadingWidgetDependencies(
    val getUserIdAsFlow: GetUserIdAsFlowUseCase,
    val getThemeConfiguration: GetThemeConfigurationUseCase,
    val getCurrentlyReadingUserBooks: GetCurrentlyReadingUserBooksUseCase,
    val appEntryPoint: AppEntryPoint,
) {
    /**
     * Emits the instance whose cycle callback has stored a new book. A live Glance session does not re-run
     * `provideGlance` on `update`, so the model flow listens here to reload the shown cover.
     */
    val selectionChanges = MutableSharedFlow<GlanceId>(
        extraBufferCapacity = 16,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
}
