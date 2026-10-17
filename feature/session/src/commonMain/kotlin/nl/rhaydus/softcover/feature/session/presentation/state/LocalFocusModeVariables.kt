package nl.rhaydus.softcover.feature.session.presentation.state

import nl.rhaydus.toad.LocalVariables

/**
 * @property isFirstFocusEvent True from a focus reset until the first text callback after focus returns,
 * which carries only the selection the field restored and must not be read as typing.
 */
internal data class LocalFocusModeVariables(
    val isFirstFocusEvent: Boolean = true,
) : LocalVariables
