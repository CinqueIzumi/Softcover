package nl.rhaydus.softcover.core.notification

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState

@Composable
actual fun rememberNotificationPermissionRequester(
    onResult: (Boolean) -> Unit,
): NotificationPermissionRequester {
    // onResult is typically a fresh lambda each recompose; capture the latest so the requester stays stable.
    val currentOnResult by rememberUpdatedState(onResult)

    return remember {
        NotificationPermissionRequester(
            // iOS has no reading-session lock-screen surface yet (the launcher is a no-op), so a notification
            // authorization prompt would ask the user to grant a capability that does nothing. Skip the prompt and
            // report granted so the session still starts; wire up the real request when the iOS surface lands.
            launchRequest = {},
            isAlreadyGranted = true,
            onResult = { currentOnResult(it) },
        )
    }
}
