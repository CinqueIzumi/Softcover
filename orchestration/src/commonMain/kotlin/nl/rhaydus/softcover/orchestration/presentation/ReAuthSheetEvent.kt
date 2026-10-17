package nl.rhaydus.softcover.orchestration.presentation

internal sealed interface ReAuthSheetEvent {
    data class ApiKeyChanged(val value: String) : ReAuthSheetEvent

    data object Submitted : ReAuthSheetEvent

    data object LoggedOut : ReAuthSheetEvent

    data object Dismissed : ReAuthSheetEvent
}
