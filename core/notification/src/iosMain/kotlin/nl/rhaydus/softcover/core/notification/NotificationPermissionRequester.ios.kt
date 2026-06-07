package nl.rhaydus.softcover.core.notification

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionBadge
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNUserNotificationCenter
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue

@Composable
actual fun rememberNotificationPermissionRequester(
    onResult: (Boolean) -> Unit,
): NotificationPermissionRequester {
    // onResult is typically a fresh lambda each recompose; capture the latest so the requester stays stable.
    val currentOnResult by rememberUpdatedState(onResult)

    return remember {
        NotificationPermissionRequester(
            launchRequest = {
                val options = UNAuthorizationOptionAlert or UNAuthorizationOptionSound or UNAuthorizationOptionBadge

                UNUserNotificationCenter.currentNotificationCenter()
                    .requestAuthorizationWithOptions(options) { granted, _ ->
                        // The completion handler runs off the main queue; Compose state must be touched on it.
                        dispatch_async(dispatch_get_main_queue()) {
                            currentOnResult(granted)
                        }
                    }
            },
            // iOS always requires a runtime authorization prompt — there is no install-time grant.
            isAlreadyGranted = false,
            onResult = { currentOnResult(it) },
        )
    }
}
