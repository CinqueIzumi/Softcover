package nl.rhaydus.softcover.core.notification.di

import org.koin.dsl.module
import nl.rhaydus.softcover.core.domain.di.dispatcherModule

val notificationModule = module {
    includes(
        platformNotificationModule,
        dispatcherModule,
    )
}
