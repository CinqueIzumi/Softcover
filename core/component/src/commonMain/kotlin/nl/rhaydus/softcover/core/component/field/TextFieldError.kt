package nl.rhaydus.softcover.core.component.field

import androidx.compose.runtime.Immutable

/**
 * An error shown on a [TextField]: the outline or ink turns to the error role and [message] appears below.
 *
 * @property shaking True from the moment the error is set until the field answers
 * [TextFieldEvent.ShakeEnded]. The shake also plays the reject haptic, so a screen does not fire its own for
 * this error.
 */
@Immutable
data class TextFieldError(
    val message: String,
    val shaking: Boolean = true,
)
