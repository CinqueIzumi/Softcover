package nl.rhaydus.softcover.feature.widget.refresh

import org.koin.core.Koin

fun startWidgetRefresher(koin: Koin) {
    koin.get<WidgetRefresher>().start()
}
