package nl.rhaydus.softcover.core.domain.model

private const val OWNED_LIST_SLUG: String = "owned"

data class BookList(
    val id: Int,
    val name: String,
    val slug: String,
    val ranked: Boolean = false,
    val privacy: PrivacySetting = PrivacySetting.PUBLIC,
    val books: List<ListBook>,
    val signature: String? = null,
) {
    val isOwned: Boolean
        get() = slug == OWNED_LIST_SLUG
}
