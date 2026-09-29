package nl.rhaydus.softcover.feature.settings.presentation.screen.section

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import nl.rhaydus.designsystem.haptics.rememberHaptics
import nl.rhaydus.designsystem.modifier.noRippleClickable
import nl.rhaydus.softcover.core.component.divider.Divider
import nl.rhaydus.softcover.core.component.divider.DividerUiModel
import nl.rhaydus.softcover.feature.settings.presentation.util.SecretTapCounter
import kotlin.time.Clock

/**
 * The version rule, and the hidden entry to the Component Gallery (`component-contract.md` § 7.5).
 *
 * It uses [noRippleClickable] rather than
 * [pressScaleClickable][nl.rhaydus.designsystem.modifier.pressScaleClickable] or a plain `clickable` on
 * purpose: no ripple, hand cursor or press scale may hint that the footer is tappable.
 */
@Composable
internal fun VersionFooter(
    divider: DividerUiModel,
    onSecretUnlocked: () -> Unit,
) {
    val tapCounter = remember { SecretTapCounter() }
    val haptics = rememberHaptics()

    Divider(
        model = divider,
        modifier = Modifier
            .fillMaxWidth()
            .noRippleClickable {
                if (tapCounter.registerTap(at = Clock.System.now())) {
                    haptics.milestone()
                    onSecretUnlocked()
                }
            },
    )
}
