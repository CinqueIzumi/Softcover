package nl.rhaydus.softcover.core.presentation.navigation

import androidx.compose.runtime.compositionLocalOf

/**
 * Cross-feature seam for opening the create-list sheet. Library (bulk add-to-list), book detail
 * (choose-lists), and Settings all offer a "create a new list" entry point, but none of them may
 * import `feature:lists` directly — only the orchestration tier may depend on a feature module. Each
 * hands the request to the **app shell** instead; the shell hosts the one instance of the sheet and
 * owns its visibility, so no caller needs its own copy of the surface or its `ScreenModel`.
 *
 * This mirrors [BookDetailPresenter]: both let a feature reach a cross-feature surface it cannot
 * import by resolving the request through a shell-provided seam rather than a pushed
 * [ScreenDestination].
 */
fun interface CreateListPresenter {
    /**
     * Opens the sheet. [onListCreated] receives the new list once it exists, and is how a caller that
     * opened the sheet *mid-task* resumes that task — the add-to-list surfaces pass a callback that
     * puts the books they were already acting on onto the new list and reopens their own selector, so
     * creating a list from "add these books to a list" returns to where it started with the new list
     * on screen and ticked, instead of dead-ending on an empty shelf.
     *
     * The name is handed over alongside the id deliberately: a caller that needs it for messaging must
     * not have to look the list up in its own state, which a freshly created list has not necessarily
     * reached yet (the list caches to the database first and reaches screen state through a `Flow`).
     *
     * Pass `null` when creating a list is the whole intent (Settings), which also leaves the sheet's
     * own "list created" confirmation in place; a caller that supplies a callback reports the outcome
     * itself.
     */
    fun open(onListCreated: ((listId: Int, listName: String) -> Unit)?)
}

/**
 * The shell-provided [CreateListPresenter]. `null` when no shell is present (previews, tests) — call
 * sites should treat a `null` presenter as "no entry point here" and no-op rather than crash.
 */
val LocalCreateListPresenter = compositionLocalOf<CreateListPresenter?> { null }
