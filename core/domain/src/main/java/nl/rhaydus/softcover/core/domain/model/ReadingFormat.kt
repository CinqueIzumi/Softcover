package nl.rhaydus.softcover.core.domain.model

enum class ReadingFormat(val id: Int, val label: String) {
    Physical(id = 1, label = "Physical"),
    Audio(id = 2, label = "Audiobook"),
    Both(id = 3, label = "Physical & Audio"),
    Ebook(id = 4, label = "E-book");

    companion object {
        fun fromId(id: Int?): ReadingFormat? = entries.firstOrNull { it.id == id }
    }
}
