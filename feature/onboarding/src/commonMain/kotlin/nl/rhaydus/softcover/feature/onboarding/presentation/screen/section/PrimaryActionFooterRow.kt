package nl.rhaydus.softcover.feature.onboarding.presentation.screen.section

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import nl.rhaydus.designsystem.component.RhaydusButton
import nl.rhaydus.designsystem.model.ButtonSize
import nl.rhaydus.designsystem.model.ButtonStyle
import nl.rhaydus.designsystem.modifier.pointerHandCursor
import nl.rhaydus.softcover.core.component.text.SoftcoverText
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.icon.drawableIconResource
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography

/**
 * A full-width filled pill (button spec: height 54/`ButtonSize.M`, Inter 600 15.5) with an optional
 * uppercase [trailingLabel] beside it ("Skip"/"Back") and an optional trailing arrow overlaid on the
 * button. `RhaydusButton` has no trailing-icon slot, so the arrow is a second, non-interactive [Icon]
 * layered on top via [Box] rather than a hand-rolled Material `Button` — the button stays the single
 * real tap target. Shared by both mobile pager pages (with the arrow, for forward motion) and the
 * API-key commit (no arrow — a commit, not a step).
 */
@Composable
internal fun PrimaryActionFooterRow(
    buttonLabel: String,
    onButtonClick: () -> Unit,
    modifier: Modifier = Modifier,
    buttonEnabled: Boolean = true,
    showTrailingArrow: Boolean = false,
    trailingLabel: String? = null,
    onTrailingClick: () -> Unit = {},
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier.weight(1f)) {
            RhaydusButton(
                label = buttonLabel,
                onClick = onButtonClick,
                style = ButtonStyle.FILLED,
                size = ButtonSize.M,
                enabled = buttonEnabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .pointerHandCursor(),
            )

            if (showTrailingArrow) {
                val arrowIcon = drawableIconResource(
                    icon = SoftcoverIcon.KeyboardArrowRight,
                    contentDescription = "",
                )

                Icon(
                    painter = arrowIcon.getIconPainter(),
                    contentDescription = arrowIcon.contentDescription,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 24.dp),
                )
            }
        }

        if (trailingLabel != null) {
            Spacer(modifier = Modifier.width(16.dp))

            SoftcoverText(
                text = trailingLabel.uppercase(),
                style = MaterialTheme.editorialTypography.eyebrowSmall.copy(
                    fontSize = 11.sp,
                    lineHeight = 14.sp,
                    letterSpacing = 1.6.sp,
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .pointerHandCursor()
                    .clickable(
                        onClickLabel = trailingLabel,
                        role = Role.Button,
                        onClick = onTrailingClick,
                    ),
            )
        }
    }
}
