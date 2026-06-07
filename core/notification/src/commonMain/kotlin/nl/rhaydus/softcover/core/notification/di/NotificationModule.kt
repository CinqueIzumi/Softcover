package nl.rhaydus.softcover.core.notification.di

import org.koin.dsl.module

val notificationModule = module {
    includes(platformNotificationModule)
}
