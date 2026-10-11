package nl.rhaydus.softcover.core.component.sheet

/**
 * A sheet's model, which owns the close flag. A composite model delegates both members to its [SheetScaffoldUiModel].
 */
interface SheetModel<Self : SheetModel<Self>> {
    val closeRequested: Boolean

    fun requestingClose(): Self
}
