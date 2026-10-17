package nl.rhaydus.softcover.core.component.bookcard

import androidx.compose.runtime.Immutable

@Immutable
data class BookCardEyebrow(
    val text: String,
    val tone: Tone = Tone.Accent,
) {
    enum class Tone {
        Accent,
        Error,
    }
}
