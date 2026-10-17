package nl.rhaydus.softcover.feature.session.presentation.service

import android.content.Context
import nl.rhaydus.softcover.core.presentation.session.ReadingSessionLauncher

internal class ReadingSessionLauncherImpl(
    private val context: Context,
) : ReadingSessionLauncher {
    override fun start() {
        ReadingSessionService.start(context = context)
    }
}
