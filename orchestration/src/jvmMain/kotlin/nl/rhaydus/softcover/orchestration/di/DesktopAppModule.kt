package nl.rhaydus.softcover.orchestration.di

import androidx.compose.runtime.Composable
import org.koin.dsl.module
import nl.rhaydus.softcover.core.designsystem.presentation.debug.DebugRoutesContent
import nl.rhaydus.softcover.core.domain.app.AppVersionProvider
import nl.rhaydus.softcover.core.notification.NotificationAccentColor
import nl.rhaydus.softcover.core.notification.NotificationAppearance
import nl.rhaydus.softcover.core.notification.NotificationIcon
import nl.rhaydus.softcover.orchestration.JvmAppVersionProvider

/**
 * Desktop counterpart of `:app`'s `appModule` + `debugRoutesModule` (and the iOS `iosAppModule`) —
 * the app-supplied bindings the shared modules don't carry.
 *
 * - [AppVersionProvider] reads the jar manifest instead of Android's `BuildConfig`.
 * - [NotificationAppearance] is built from the desktop appearance tokens, which are empty (desktop
 *   notifications are not yet wired).
 * - [DebugRoutesContent] is the no-op binding (matches the Android *release* variant — no debug
 *   routes on desktop).
 */
internal val desktopAppModule = module {
    single<AppVersionProvider> { JvmAppVersionProvider() }

    single {
        NotificationAppearance(
            smallIcon = NotificationIcon(),
            accentColor = NotificationAccentColor(),
        )
    }

    single<DebugRoutesContent> {
        object : DebugRoutesContent {
            @Composable
            override fun Render() = Unit
        }
    }
}
