package nl.rhaydus.softcover.feature.session.presentation.service

import android.content.Context
import nl.rhaydus.softcover.core.presentation.session.ReadingSessionLauncher

class ReadingSessionLauncherImpl(
    private val context: Context,
) : ReadingSessionLauncher {
    override fun start() {
        ReadingSessionService.start(context = context)
    }
}
