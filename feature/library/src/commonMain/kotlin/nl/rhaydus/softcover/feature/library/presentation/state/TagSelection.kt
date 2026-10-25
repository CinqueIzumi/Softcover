package nl.rhaydus.softcover.feature.library.presentation.state

import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.collections.immutable.toImmutableSet

/** The tags of one source a book must carry ([included]) or must not carry ([excluded]); never both. */
internal data class TagSelection<K : TagKey>(
    val included: ImmutableSet<K> = persistentSetOf(),
    val excluded: ImmutableSet<K> = persistentSetOf(),
) {
    val isEmpty: Boolean
        get() = included.isEmpty() && excluded.isEmpty()

    fun matches(carried: Set<K>): Boolean =
        included.all { it in carried } && excluded.none { it in carried }

    fun entries(): List<Pair<K, TagDirection>> =
        included.map { it to TagDirection.INCLUDE } + excluded.map { it to TagDirection.EXCLUDE }

    fun contains(
        key: K,
        direction: TagDirection,
    ): Boolean = when (direction) {
        TagDirection.INCLUDE -> key in included
        TagDirection.EXCLUDE -> key in excluded
    }

    fun toggled(
        key: K,
        direction: TagDirection,
    ): TagSelection<K> = when (direction) {
        TagDirection.INCLUDE -> TagSelection(
            included = if (key in included) (included - key).toImmutableSet() else (included + key).toImmutableSet(),
            excluded = (excluded - key).toImmutableSet(),
        )

        TagDirection.EXCLUDE -> TagSelection(
            included = (included - key).toImmutableSet(),
            excluded = if (key in excluded) (excluded - key).toImmutableSet() else (excluded + key).toImmutableSet(),
        )
    }
}
