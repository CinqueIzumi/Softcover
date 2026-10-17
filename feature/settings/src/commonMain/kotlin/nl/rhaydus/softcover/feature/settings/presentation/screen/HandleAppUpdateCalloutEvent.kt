package nl.rhaydus.softcover.feature.settings.presentation.screen

import nl.rhaydus.softcover.core.component.callout.CalloutEvent

internal fun handleAppUpdateCalloutEvent(
    event: CalloutEvent,
    onStartAppUpdate: () -> Unit,
) {
    when (event) {
        CalloutEvent.ActionClicked -> onStartAppUpdate()
        CalloutEvent.DismissClicked -> Unit
    }
}
