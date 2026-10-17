package nl.rhaydus.softcover.feature.widget.presentation.glance

import android.content.Context
import androidx.glance.action.Action
import androidx.glance.appwidget.action.actionStartActivity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import nl.rhaydus.softcover.core.domain.model.NO_USER_ID
import nl.rhaydus.softcover.core.domain.model.ThemeConfiguration
import nl.rhaydus.softcover.core.identity.domain.usecase.GetUserIdAsFlowUseCase
import nl.rhaydus.softcover.core.preferences.domain.usecase.GetThemeConfigurationUseCase
import nl.rhaydus.softcover.core.presentation.navigation.AppEntryPoint
import nl.rhaydus.softcover.core.presentation.navigation.ExternalNavigationRequest
import nl.rhaydus.softcover.core.presentation.navigation.TabDestination

internal class WidgetEnvironment(
    val getUserIdAsFlow: GetUserIdAsFlowUseCase,
    val getThemeConfiguration: GetThemeConfigurationUseCase,
    private val appEntryPoint: AppEntryPoint,
) {
    fun isSignedIn(): Flow<Boolean> = getUserIdAsFlow().map { it != NO_USER_ID }

    suspend fun initialTheme(themeConfigurations: Flow<ThemeConfiguration>): ThemeConfiguration =
        themeConfigurations.firstOrNull() ?: ThemeConfiguration()

    fun openTab(
        context: Context,
        destination: TabDestination,
    ): Action = actionStartActivity(
        appEntryPoint.intent(
            context = context,
            request = ExternalNavigationRequest.OpenTab(destination = destination),
        ),
    )

    fun openBook(context: Context): (Int) -> Action = { bookId ->
        actionStartActivity(
            appEntryPoint.intent(
                context = context,
                request = ExternalNavigationRequest.OpenBook(bookId = bookId),
            ),
        )
    }
}
