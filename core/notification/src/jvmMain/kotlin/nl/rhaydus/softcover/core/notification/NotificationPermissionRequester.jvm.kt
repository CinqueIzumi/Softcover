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
            // Desktop has no runtime notification-permission prompt (and the notifier is a no-op for
            // now), so report granted without showing a dialog so dependent flows still proceed.
            launchRequest = {},
            isAlreadyGranted = true,
            onResult = { currentOnResult(it) },
        )
    }
}
