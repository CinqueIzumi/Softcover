package nl.rhaydus.softcover.core.component.haptics

import nl.rhaydus.designsystem.haptics.Haptics

internal class RecordingHaptics : Haptics {
    val calls = mutableListOf<String>()

    override fun commit() {
        calls += "commit"
    }

    override fun reject() {
        calls += "reject"
    }

    override fun select() {
        calls += "select"
    }

    override fun threshold() {
        calls += "threshold"
    }

    override fun tickle() {
        calls += "tickle"
    }

    override fun lift() {
        calls += "lift"
    }

    override fun drop() {
        calls += "drop"
    }

    override fun milestone() {
        calls += "milestone"
    }
}
