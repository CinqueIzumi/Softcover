class ReaderFixture : Runnable {
    constructor()

    constructor(label: String) {
        this.label = label
    }

    private var label: String = ""

    override fun run() = Unit

    companion object {
        const val BIG = 123_456_789_012L
        const val RATIO = 3.14159
        const val TEXT = "after the wide constants"
    }
}
