package nl.rhaydus.softcover.core.component.sheet

/** Everything the host can be told by [LoadingSheet], behind a single `onEvent` lambda. */
sealed interface LoadingSheetEvent {
    /** The determinate progress indicator reached 100% and settled. Today's `onLoaderFinished`. */
    data object LoaderFinished : LoadingSheetEvent

    /** The sheet finished hiding; the host clears [LoadingSheetUiModel.isLoading] and the close request. */
    data object Dismissed : LoadingSheetEvent
}
