package nl.rhaydus.softcover.feature.deadlines.domain.model

enum class DeadlineStatus(val label: String) {
    OnTrack(label = "On track"),
    Behind(label = "Behind"),
    Expired(label = "Expired"),
}
