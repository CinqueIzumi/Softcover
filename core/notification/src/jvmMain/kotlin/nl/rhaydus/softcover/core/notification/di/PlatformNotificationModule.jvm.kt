package nl.rhaydus.softcover.core.notification.di

import org.koin.dsl.binds
import org.koin.dsl.module
import nl.rhaydus.softcover.core.notification.JvmSoftcoverNotifier
import nl.rhaydus.softcover.core.notification.SoftcoverNotifier

actual val platformNotificationModule = module {
    // Also bound as AutoCloseable so the desktop shutdown teardown removes the tray icon (which pins
    // AWT's non-daemon threads) when one was installed.
    single { JvmSoftcoverNotifier() } binds arrayOf(SoftcoverNotifier::class, AutoCloseable::class)
}
