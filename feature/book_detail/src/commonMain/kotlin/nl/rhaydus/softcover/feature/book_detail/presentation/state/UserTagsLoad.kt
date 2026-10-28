package nl.rhaydus.softcover.feature.book_detail.presentation.state

/**
 * Whether this open has refreshed the shelved book's own tags from the server. A save replaces the
 * whole server set, so editing is allowed only from [Loaded]; from a stale or empty local copy it could
 * delete tags added elsewhere. The rows themselves render from the local store regardless.
 */
internal enum class UserTagsLoad {
    Loading,
    Loaded,
    Failed,
}
