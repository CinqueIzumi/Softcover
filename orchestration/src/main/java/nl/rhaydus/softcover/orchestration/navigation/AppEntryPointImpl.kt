package nl.rhaydus.softcover.orchestration.navigation

import android.content.Context
import android.content.Intent
import nl.rhaydus.softcover.core.designsystem.presentation.navigation.AppEntryPoint
import nl.rhaydus.softcover.orchestration.presentation.MainActivity

/**
 * Orchestration-tier resolution of [AppEntryPoint]. The only place that references the launcher
 * [MainActivity] when building deep-link intents for features.
 */
class AppEntryPointImpl : AppEntryPoint {
    override fun focusModeIntent(context: Context): Intent =
        Intent(
            context,
            MainActivity::class.java,
        )
            .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            .putExtra(
                MainActivity.EXTRA_OPEN_FOCUS_MODE,
                true,
            )
}
