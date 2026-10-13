package nl.rhaydus.softcover.core.component.sheet

import androidx.compose.runtime.Immutable

/**
 * One sheet's lifetime on the host's state. The model's [SheetModel.closeRequested] asks the sheet to hide;
 * the host moves to [Closed] once the sheet reports it is gone, and [dismissed] hands back the
 * [Open.pending] intent recorded by [requestClose] so the follow-up (leave the screen, log out) runs only
 * after the sheet has left.
 */
@Immutable
sealed interface SheetSession<out Model : SheetModel<out Model>, out Pending : Any> {
    data object Closed : SheetSession<Nothing, Nothing>

    data class Open<out Model : SheetModel<out Model>, out Pending : Any>(
        val model: Model,
        val pending: Pending? = null,
    ) : SheetSession<Model, Pending>
}

val SheetSession<*, *>.isOpen: Boolean get() = this is SheetSession.Open

val SheetSession<*, *>.isClosing: Boolean get() = (this as? SheetSession.Open)?.model?.closeRequested == true

val <Model : SheetModel<out Model>> SheetSession<Model, *>.modelOrNull: Model?
    get() = (this as? SheetSession.Open)?.model

// The receiver only fixes the Model and Pending type arguments.
@Suppress("UnusedReceiverParameter")
fun <Model : SheetModel<Model>, Pending : Any> SheetSession<Model, Pending>.open(
    model: Model,
): SheetSession<Model, Pending> = SheetSession.Open(model = model)

/** Asks the sheet to hide. A null [pending] keeps one already recorded, so a later plain close cannot wipe it. */
fun <Model : SheetModel<Model>, Pending : Any> SheetSession<Model, Pending>.requestClose(
    pending: Pending? = null,
): SheetSession<Model, Pending> = when (this) {
    is SheetSession.Open -> copy(
        model = model.requestingClose(),
        pending = pending ?: this.pending,
    )
    SheetSession.Closed -> this
}

/** A close already requested survives the rebuilt model, whose own flag starts false. */
fun <Model : SheetModel<Model>, Pending : Any> SheetSession<Model, Pending>.withModel(
    transform: (Model) -> Model,
): SheetSession<Model, Pending> = when (this) {
    is SheetSession.Open -> {
        val rebuilt = transform(model)

        copy(model = if (model.closeRequested && rebuilt.closeRequested.not()) rebuilt.requestingClose() else rebuilt)
    }
    SheetSession.Closed -> this
}

fun <Model : SheetModel<Model>, Pending : Any> SheetSession<Model, Pending>.openOrUpdate(
    model: Model,
): SheetSession<Model, Pending> = if (isOpen && isClosing.not()) withModel { model } else open(model = model)

/** The pending intent to run once the sheet has hidden, or null when none was recorded or the session is closed. */
fun <Pending : Any> SheetSession<*, Pending>.dismissed(): Pending? = (this as? SheetSession.Open)?.pending
