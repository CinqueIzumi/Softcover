package nl.rhaydus.softcover.core.component.lists

/**
 * Everything the reader can do in `ChooseListsBottomSheet`, behind a single `onEvent` lambda (R1).
 */
sealed interface ChooseListsEvent {
    /**
     * A list row was tapped. Carries the row's [membership] as well as its [listId] because that is
     * what the host branches on: `ALL` clears the list, `NONE` and `PARTIAL` both commit toward
     * `ALL` (the standard tristate-checkbox direction), and the host cannot re-derive it without the
     * data the mapping already consumed.
     */
    data class MembershipToggled(
        val listId: Int,
        val membership: ListMembership,
    ) : ChooseListsEvent

    /**
     * "New list" was tapped. The sheet starts its own slide-down right after emitting this, so the
     * host only records the intent and acts on [Dismissed]; removing the sheet here would cut the
     * animation short.
     */
    data object NewListRequested : ChooseListsEvent

    data object Dismissed : ChooseListsEvent
}
