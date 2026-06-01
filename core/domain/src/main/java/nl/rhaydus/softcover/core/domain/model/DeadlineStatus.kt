package nl.rhaydus.softcover.core.domain.model

enum class DeadlineStatus(val label: String) {
    OnTrack(label = "On track"),
    Behind(label = "Behind"),
    Expired(label = "Expired"),
}
