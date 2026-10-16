package nl.rhaydus.softcover.feature.widget.progress

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.glance.appwidget.updateAll
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import nl.rhaydus.softcover.core.book.domain.usecase.GetCurrentlyReadingUserBooksUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.MarkBookAsReadUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.RecordBookProgressUseCase
import nl.rhaydus.softcover.core.identity.domain.usecase.GetUserIdAsFlowUseCase
import nl.rhaydus.softcover.core.preferences.domain.usecase.GetLastUsedProgressUnitAsFlowUseCase
import nl.rhaydus.softcover.core.preferences.domain.usecase.GetThemeConfigurationUseCase
import nl.rhaydus.softcover.core.preferences.domain.usecase.SetLastUsedProgressUnitUseCase
import nl.rhaydus.softcover.core.presentation.navigation.AppEntryPoint
import nl.rhaydus.softcover.core.presentation.navigation.ExternalNavigationRequest
import nl.rhaydus.softcover.feature.widget.R
import nl.rhaydus.softcover.feature.widget.currentlyreading.CurrentlyReadingWidget
import nl.rhaydus.toad.ActionDependencies

internal class UpdateProgressDependencies(
    val context: Context,
    val getUserIdAsFlow: GetUserIdAsFlowUseCase,
    val getCurrentlyReadingUserBooks: GetCurrentlyReadingUserBooksUseCase,
    val recordBookProgress: RecordBookProgressUseCase,
    val markBookAsRead: MarkBookAsReadUseCase,
    val getLastUsedProgressUnitAsFlow: GetLastUsedProgressUnitAsFlowUseCase,
    val setLastUsedProgressUnit: SetLastUsedProgressUnitUseCase,
    val getThemeConfiguration: GetThemeConfigurationUseCase,
    val appEntryPoint: AppEntryPoint,
    override val coroutineScope: CoroutineScope,
    override val mainDispatcher: CoroutineDispatcher,
) : ActionDependencies() {
    fun openVerdict(bookId: Int) {
        context.startActivity(
            appEntryPoint
                .intent(
                    context = context,
                    request = ExternalNavigationRequest.OpenBookVerdict(bookId = bookId),
                )
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        )
    }

    fun showSaveFailed() {
        Toast.makeText(
            context,
            R.string.update_progress_save_failed,
            Toast.LENGTH_SHORT,
        ).show()
    }

    suspend fun refreshWidget() = CurrentlyReadingWidget().updateAll(context)
}
