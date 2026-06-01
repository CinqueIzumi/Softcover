package nl.rhaydus.softcover.core.presentation.component

/**
 * Membership of a list relative to the set of books currently being acted on.
 *
 * - [NONE] — none of the books are in the list.
 * - [PARTIAL] — some but not all of the books are in the list. Tapping a partial row commits
 *   the membership *toward* the larger end (i.e. it adds the missing books so the list ends in
 *   [ALL]), matching the standard tristate-checkbox UX.
 * - [ALL] — every book is in the list. Tapping clears membership.
 */
enum class ListMembership {
    NONE,
    PARTIAL,
    ALL,
}
