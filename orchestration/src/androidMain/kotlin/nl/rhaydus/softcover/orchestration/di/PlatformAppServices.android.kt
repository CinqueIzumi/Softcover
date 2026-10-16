package nl.rhaydus.softcover.orchestration.di

import nl.rhaydus.softcover.feature.widget.presentation.refresh.startWidgetRefresher
import org.koin.core.Koin

internal actual fun startPlatformAppServices(koin: Koin) {
    startWidgetRefresher(koin)
}
