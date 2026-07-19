package nl.rhaydus.softcover.feature.onboarding.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import nl.rhaydus.designsystem.component.InlineErrorState
import nl.rhaydus.designsystem.component.RhaydusButton
import nl.rhaydus.designsystem.model.ButtonSize
import nl.rhaydus.designsystem.model.ButtonStyle
import nl.rhaydus.designsystem.modifier.pointerHandCursor
import nl.rhaydus.softcover.core.designsystem.presentation.component.ClickableText
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.icon.drawableIconResource
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.feature.onboarding.presentation.action.OnApiKeySaveClickAction
import nl.rhaydus.softcover.feature.onboarding.presentation.action.OnApiKeyValueChangeAction
import nl.rhaydus.softcover.feature.onboarding.presentation.action.OnboardingAction
import nl.rhaydus.softcover.feature.onboarding.presentation.state.OnboardingUiState

private const val HARDCOVER_API_KEY_URL = "https://hardcover.app/account/api"
private const val OPEN_IN_NEW_INLINE_CONTENT_ID = "openInNew"

/**
 * The Hardcover API-key entry form: a promoted paste-from-clipboard row, an "Or type it" divider, a
 * tonal key field, a helper link out to Hardcover, and a "where to find it" explainer, closing on the
 * Save action and its error slot. Shared verbatim by the mobile pager's final page and the desktop
 * single-panel layout — each caller renders its own opening copy above this (mobile: the fixed top bar
 * plus "Add your key." headline/body; desktop: the "Welcome" hero also used on mobile page 1), so this
 * section owns only the key-capture content and the commit action, never a page's opening copy.
 * [footerTrailingLabel] adds the small uppercase "Back" affordance beside the Save button — mobile
 * supplies it (returning to the Connect page), desktop leaves it `null` (no Back/Skip chrome on the
 * single-panel layout).
 *
 * Desktop's single scrolling panel wants the content and the commit footer to simply flow together, so
 * it calls this unified composable. Mobile's page 3 instead needs the footer **pinned** outside its own
 * scroll region (so "Save API key" never scrolls off-screen) and calls [ApiKeyEntryContent] /
 * [ApiKeyEntryFooter] separately, placing only the content half inside the scrolling column.
 */
@Composable
internal fun ApiKeyEntrySection(
    state: OnboardingUiState,
    runAction: (action: OnboardingAction) -> Unit,
    openUrl: (String) -> Unit,
    getCopiedText: () -> String,
    modifier: Modifier = Modifier,
    footerTrailingLabel: String? = null,
    onFooterTrailingLabelClick: () -> Unit = {},
) {
    Column(modifier = modifier) {
        ApiKeyEntryContent(
            state = state,
            runAction = runAction,
            openUrl = openUrl,
            getCopiedText = getCopiedText,
        )

        Spacer(modifier = Modifier.height(30.dp))

        ApiKeyEntryFooter(
            state = state,
            runAction = runAction,
            footerTrailingLabel = footerTrailingLabel,
            onFooterTrailingLabelClick = onFooterTrailingLabelClick,
        )
    }
}

/**
 * The scrolling half of [ApiKeyEntrySection]: paste row, "Or type it" divider, key field, helper line,
 * and the "where to find it" explainer — everything except the commit footer (see [ApiKeyEntryFooter]).
 */
@Composable
internal fun ApiKeyEntryContent(
    state: OnboardingUiState,
    runAction: (action: OnboardingAction) -> Unit,
    openUrl: (String) -> Unit,
    getCopiedText: () -> String,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        PasteFromClipboardRow(
            onClick = {
                val newValue = getCopiedText()

                runAction(OnApiKeyValueChangeAction(newValue = newValue))
            },
        )

        Spacer(modifier = Modifier.height(18.dp))

        OrTypeItDivider()

        Spacer(modifier = Modifier.height(18.dp))

        KeyField(
            value = state.apiKeyValue,
            onValueChange = { runAction(OnApiKeyValueChangeAction(newValue = it)) },
        )

        Spacer(modifier = Modifier.height(12.dp))

        HelperLine(openUrl = openUrl)

        Spacer(modifier = Modifier.height(26.dp))

        WhereToFindItExplainer()
    }
}

/**
 * The pinned half of [ApiKeyEntrySection]: the "Save API key" commit button (with the optional
 * [footerTrailingLabel] "Back" affordance) and the submission error slot. Kept outside the scrolling
 * content region on mobile's page 3 so the primary action is always visible, never scrolled past.
 */
@Composable
internal fun ApiKeyEntryFooter(
    state: OnboardingUiState,
    runAction: (action: OnboardingAction) -> Unit,
    modifier: Modifier = Modifier,
    footerTrailingLabel: String? = null,
    onFooterTrailingLabelClick: () -> Unit = {},
) {
    Column(modifier = modifier) {
        PrimaryActionFooterRow(
            buttonLabel = "Save API key",
            onButtonClick = { runAction(OnApiKeySaveClickAction) },
            buttonEnabled = state.saveApiKeyButtonEnabled,
            trailingLabel = footerTrailingLabel,
            onTrailingClick = onFooterTrailingLabelClick,
        )

        state.submissionError?.let { error ->
            Spacer(modifier = Modifier.height(16.dp))

            InlineErrorState(
                message = error,
                onRetry = { runAction(OnApiKeySaveClickAction) },
                textStyle = MaterialTheme.editorialTypography.bodySmall,
            )
        }
    }
}

@Composable
private fun PasteFromClipboardRow(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.primaryContainer)
            .pointerHandCursor()
            .clickable(onClick = onClick)
            .padding(
                horizontal = 18.dp,
                vertical = 16.dp,
            ),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val pasteIcon = drawableIconResource(
            icon = SoftcoverIcon.ContentPaste,
            contentDescription = "Paste icon",
        )

        Icon(
            painter = pasteIcon.getIconPainter(),
            contentDescription = pasteIcon.contentDescription,
            modifier = Modifier.size(22.dp),
            tint = MaterialTheme.colorScheme.onPrimaryContainer,
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Paste from clipboard",
                style = MaterialTheme.editorialTypography.titleSmall.copy(
                    fontSize = 14.5.sp,
                    lineHeight = 18.sp,
                ),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )

            Text(
                text = "The quick way in",
                style = MaterialTheme.editorialTypography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
            )
        }

        val arrowIcon = drawableIconResource(
            icon = SoftcoverIcon.KeyboardArrowRight,
            contentDescription = "",
        )

        Icon(
            painter = arrowIcon.getIconPainter(),
            contentDescription = arrowIcon.contentDescription,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.onPrimaryContainer,
        )
    }
}

@Composable
private fun OrTypeItDivider() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.outlineVariant,
        )

        Text(
            text = "Or type it".uppercase(),
            style = MaterialTheme.editorialTypography.eyebrowSmall.copy(
                fontSize = 10.sp,
                lineHeight = 14.sp,
                letterSpacing = 2.sp,
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 12.dp),
        )

        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.outlineVariant,
        )
    }
}

@Composable
private fun KeyField(
    value: String,
    onValueChange: (String) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(percent = 50))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .padding(horizontal = 18.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        if (value.isEmpty()) {
            // Matches the placeholder register EditorialSearchField uses (plain Material bodyLarge,
            // onSurfaceVariant) — this is key entry, not search, so there is no leading glyph or clear
            // button, just the tonal pill and the primary caret.
            Text(
                text = "Enter your key",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = LocalTextStyle.current.merge(
                MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onSurface,
                ),
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun HelperLine(openUrl: (String) -> Unit) {
    val linkColor = MaterialTheme.colorScheme.primary

    val annotatedString = buildAnnotatedString {
        append("Find your key on the Hardcover website ")

        pushStringAnnotation(
            tag = "url",
            annotation = HARDCOVER_API_KEY_URL,
        )

        withStyle(style = SpanStyle(color = linkColor)) {
            append("here")
            appendInlineContent(
                OPEN_IN_NEW_INLINE_CONTENT_ID,
                "[external link]",
            )
        }

        pop()

        append(".")
    }

    val openInNewIcon = drawableIconResource(
        icon = SoftcoverIcon.OpenInNew,
        contentDescription = "",
    )

    ClickableText(
        annotatedText = annotatedString,
        style = MaterialTheme.editorialTypography.bodySmall.copy(
            fontSize = 13.sp,
            lineHeight = 19.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
        handleUrlClick = openUrl,
        inlineContent = mapOf(
            OPEN_IN_NEW_INLINE_CONTENT_ID to InlineTextContent(
                placeholder = Placeholder(
                    width = 12.sp,
                    height = 12.sp,
                    placeholderVerticalAlign = PlaceholderVerticalAlign.TextCenter,
                ),
            ) {
                Icon(
                    painter = openInNewIcon.getIconPainter(),
                    contentDescription = openInNewIcon.contentDescription,
                    tint = linkColor,
                    modifier = Modifier.fillMaxSize(),
                )
            },
        ),
    )
}

@Composable
private fun WhereToFindItExplainer() {
    Column(modifier = Modifier.fillMaxWidth()) {
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Where to find it".uppercase(),
            style = MaterialTheme.editorialTypography.eyebrowSmall.copy(
                fontSize = 10.5.sp,
                lineHeight = 14.sp,
                letterSpacing = 2.sp,
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(14.dp))

        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            ExplainerStepRow(
                number = "01",
                label = "Sign in at hardcover.app",
                sub = "Free, a minute to set up.",
            )

            ExplainerStepRow(
                number = "02",
                label = "Open Settings → API",
                sub = "Your access token lives here.",
            )

            ExplainerStepRow(
                number = "03",
                label = "Copy it, come back",
                sub = "Then paste it above.",
            )
        }
    }
}

@Composable
private fun ExplainerStepRow(
    number: String,
    label: String,
    sub: String,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(15.dp)) {
        Text(
            text = number,
            style = MaterialTheme.editorialTypography.headlineSmall.copy(
                fontSize = 17.sp,
                lineHeight = 17.sp,
                fontFeatureSettings = "tnum",
            ),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .width(22.dp)
                .alignByBaseline(),
        )

        Column(modifier = Modifier.alignByBaseline()) {
            Text(
                text = label,
                style = MaterialTheme.editorialTypography.titleSmall.copy(
                    fontSize = 13.5.sp,
                    lineHeight = 18.sp,
                ),
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(modifier = Modifier.height(1.dp))

            Text(
                text = sub,
                style = MaterialTheme.editorialTypography.bodySmall.copy(
                    fontSize = 12.5.sp,
                    lineHeight = 17.sp,
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

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

            Text(
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
