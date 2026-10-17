package nl.rhaydus.softcover.feature.book_detail.presentation.state

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import nl.rhaydus.softcover.core.domain.model.UserTag

/**
 * Load state of the shelved book's own tags. Tag mutations upsert the whole set, so they run only
 * from [Loaded]; [Failed] keeps them blocked rather than overwriting the server's tags with an empty set.
 */
internal sealed interface UserTagsLoad {
    data object Loading : UserTagsLoad

    data class Loaded(val tags: ImmutableList<UserTag>) : UserTagsLoad

    data object Failed : UserTagsLoad
}

internal val UserTagsLoad.tagsOrEmpty: ImmutableList<UserTag>
    get() = when (this) {
        is UserTagsLoad.Loaded -> tags
        UserTagsLoad.Loading,
        UserTagsLoad.Failed,
        -> persistentListOf()
    }
