package nl.rhaydus.softcover.feature.book_detail.presentation.state

import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.persistentSetOf

/**
 * The how-it-read answers the reader is editing. Its presence on the state is what keeps the sheet open.
 *
 * Every value is a chip key, so the draft holds no domain type; [balance] runs 0f at the plot end to 1f at
 * the character end, and null is unanswered.
 */
internal data class HowItReadDraft(
    val paceKey: String? = null,
    val balance: Float? = null,
    val characters: PersistentMap<String, HowItReadAnswer> = persistentMapOf(),
    val writingStyleKey: String? = null,
    val flagKeys: PersistentSet<String> = persistentSetOf(),
)
