package nl.rhaydus.softcover.orchestration.di

import androidx.compose.runtime.Composable
import org.koin.dsl.module
import nl.rhaydus.softcover.core.designsystem.presentation.debug.DebugRoutesContent
import nl.rhaydus.softcover.core.domain.app.AppVersionProvider
import nl.rhaydus.softcover.core.notification.NotificationAccentColor
import nl.rhaydus.softcover.core.notification.NotificationAppearance
import nl.rhaydus.softcover.core.notification.NotificationIcon
import nl.rhaydus.softcover.orchestration.IosAppVersionProvider

/**
 * iOS counterpart of `:app`'s `appModule` + `debugRoutesModule` — the app-supplied bindings the
 * shared modules don't carry. On Android these live in `:app` because they reach for `R.*` and the
 * debug/release build-type split; iOS supplies its own.
 *
 * - [AppVersionProvider] reads the iOS bundle instead of Android's `BuildConfig`.
 * - [NotificationAppearance] is built from the iOS appearance tokens, which are empty (iOS uses the
 *   app icon and does not tint notifications).
 * - [DebugRoutesContent] is the no-op binding (matches the Android *release* variant — no debug
 *   routes on iOS).
 */
internal val iosAppModule = module {
    single<AppVersionProvider> { IosAppVersionProvider() }

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
