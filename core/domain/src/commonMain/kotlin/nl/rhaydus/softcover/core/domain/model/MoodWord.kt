package nl.rhaydus.softcover.core.domain.model

sealed interface MoodWord {
    data class Preset(val mood: Mood) : MoodWord

    data class Own(val text: String) : MoodWord
}
