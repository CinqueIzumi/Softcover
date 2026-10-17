package nl.rhaydus.softcover.orchestration.navigation

import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import nl.rhaydus.softcover.core.presentation.navigation.AppEntryPoint
import nl.rhaydus.softcover.core.presentation.navigation.ExternalNavigationRequest
import nl.rhaydus.softcover.orchestration.presentation.MainActivity

/**
 * Orchestration-tier resolution of [AppEntryPoint]. The only place that references the launcher
 * [MainActivity] when building deep-link intents for features.
 *
 * Each request is encoded as the intent's data URI rather than an extra: `PendingIntent` matching
 * ignores extras, so two requests that differ only in an extra (two books) would collapse into one.
 */
internal class AppEntryPointImpl : AppEntryPoint {
    override fun intent(
        context: Context,
        request: ExternalNavigationRequest,
    ): Intent =
        Intent(
            context,
            MainActivity::class.java,
        )
            .setAction(Intent.ACTION_VIEW)
            .setData(request.toUriString().toUri())
            .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
}
