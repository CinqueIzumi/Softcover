package nl.rhaydus.softcover.core.notification.di

import org.koin.dsl.module
import nl.rhaydus.softcover.core.notification.SoftcoverNotifier
import nl.rhaydus.softcover.core.notification.SoftcoverNotifierImpl

actual val platformNotificationModule = module {
    single<SoftcoverNotifier> { SoftcoverNotifierImpl() }
}
