package nl.rhaydus.softcover.core.domain.model

enum class Mood(val storageKey: String) {
    Tense("tense"),
    Hopeful("hopeful"),
    Funny("funny"),
    Sad("sad"),
    Reflective("reflective"),
    Dark("dark"),
    Cosy("cosy"),
    Melancholy("melancholy"),
    Mysterious("mysterious"),
    Curious("curious"),
    Wrecked("wrecked"),
    Bored("bored"),
    Gripped("gripped"),
    ;

    companion object {
        val HowItFelt: List<Mood> = listOf(
            Tense,
            Hopeful,
            Funny,
            Sad,
            Reflective,
            Dark,
            Cosy,
            Melancholy,
            Mysterious,
        )

        val Moment: List<Mood> = listOf(
            Curious,
            Tense,
            Wrecked,
            Hopeful,
            Bored,
            Gripped,
        )

        fun fromStorageKey(key: String): Mood? = entries.firstOrNull { it.storageKey == key }
    }
}
