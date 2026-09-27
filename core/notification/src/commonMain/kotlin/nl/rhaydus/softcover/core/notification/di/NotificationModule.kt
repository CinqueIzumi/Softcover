package nl.rhaydus.softcover.core.notification.di

import nl.rhaydus.softcover.core.domain.di.dispatcherModule
import org.koin.dsl.module

val notificationModule = module {
    includes(
        platformNotificationModule,
        dispatcherModule,
    )
}
