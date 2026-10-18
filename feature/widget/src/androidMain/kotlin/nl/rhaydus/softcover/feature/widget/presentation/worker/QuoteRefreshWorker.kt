package nl.rhaydus.softcover.feature.widget.presentation.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.flow.firstOrNull
import nl.rhaydus.softcover.core.domain.model.NO_USER_ID
import nl.rhaydus.softcover.feature.widget.presentation.glance.QuoteWidgetDependencies
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.context.GlobalContext

private const val MAX_RETRIES = 3

internal class QuoteRefreshWorker(
    appContext: Context,
    workerParameters: WorkerParameters,
) : CoroutineWorker(appContext, workerParameters), KoinComponent {
    override suspend fun doWork(): Result {
        if (GlobalContext.getOrNull() == null) return Result.retry()

        val dependencies = get<QuoteWidgetDependencies>()

        val userId = dependencies.environment.getUserIdAsFlow().firstOrNull()

        if (userId == null || userId == NO_USER_ID) return Result.success()

        val failed = dependencies.fetchTracker.track { dependencies.refreshAllQuotes() }.isFailure

        return if (failed && runAttemptCount < MAX_RETRIES) Result.retry() else Result.success()
    }
}
