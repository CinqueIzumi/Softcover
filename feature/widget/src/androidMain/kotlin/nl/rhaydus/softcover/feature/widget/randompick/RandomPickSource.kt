package nl.rhaydus.softcover.feature.widget.randompick

internal sealed interface RandomPickSource {
    data object WantToRead : RandomPickSource

    data object Owned : RandomPickSource

    data class UserList(val listId: Int) : RandomPickSource
}
