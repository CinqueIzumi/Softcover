package nl.rhaydus.softcover.feature.onboarding.presentation.screen.section

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import nl.rhaydus.designsystem.component.InlineErrorState
import nl.rhaydus.designsystem.modifier.pointerHandCursor
import nl.rhaydus.softcover.core.component.divider.Divider
import nl.rhaydus.softcover.core.component.divider.DividerCenter
import nl.rhaydus.softcover.core.component.field.TextField
import nl.rhaydus.softcover.core.component.field.TextFieldEvent
import nl.rhaydus.softcover.core.component.text.SoftcoverText
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.icon.drawableIconResource
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Corners
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.core.designsystem.presentation.theme.tabularFigures
import nl.rhaydus.softcover.feature.onboarding.presentation.action.OnApiKeyPasteAction
import nl.rhaydus.softcover.feature.onboarding.presentation.action.OnApiKeySaveClickAction
import nl.rhaydus.softcover.feature.onboarding.presentation.action.OnApiKeyShakeEndedAction
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

        Spacer(modifier = Modifier.height(Spacing.xxxl))

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
                runAction(OnApiKeyPasteAction(pastedText = getCopiedText()))
            },
        )

        Spacer(modifier = Modifier.height(Spacing.lg))

        Divider(center = DividerCenter.Label(text = "Or type it"))

        Spacer(modifier = Modifier.height(Spacing.lg))

        TextField(
            model = state.apiKeyField,
            onEvent = { event ->
                when (event) {
                    is TextFieldEvent.ValueChanged -> runAction(OnApiKeyValueChangeAction(newValue = event.text))

                    is TextFieldEvent.Submitted -> runAction(OnApiKeySaveClickAction)

                    is TextFieldEvent.TrailingClicked -> Unit

                    is TextFieldEvent.ShakeEnded -> runAction(OnApiKeyShakeEndedAction)
                }
            },
        )

        Spacer(modifier = Modifier.height(Spacing.sm))

        HelperLine(openUrl = openUrl)

        Spacer(modifier = Modifier.height(Spacing.xxl))

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
            Spacer(modifier = Modifier.height(Spacing.md))

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
            .clip(Corners.md)
            .background(MaterialTheme.colorScheme.primaryContainer)
            .pointerHandCursor()
            .clickable(onClick = onClick)
            .padding(
                horizontal = Spacing.md,
                vertical = Spacing.md,
            ),
        horizontalArrangement = Arrangement.spacedBy(Spacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val pasteIcon = drawableIconResource(
            icon = SoftcoverIcon.ContentPaste,
            contentDescription = "",
        )

        Icon(
            painter = pasteIcon.getIconPainter(),
            contentDescription = pasteIcon.contentDescription,
            modifier = Modifier.size(22.dp),
            tint = MaterialTheme.colorScheme.onPrimaryContainer,
        )

        Column(modifier = Modifier.weight(1f)) {
            SoftcoverText(
                text = "Paste from clipboard",
                style = MaterialTheme.editorialTypography.titleSmall.copy(
                    fontSize = 14.5.sp,
                    lineHeight = 18.sp,
                ),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )

            SoftcoverText(
                text = "Paste it from your clipboard",
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
private fun HelperLine(openUrl: (String) -> Unit) {
    val linkColor = MaterialTheme.colorScheme.primary

    val annotatedString = buildAnnotatedString {
        append("Find your key on the Hardcover website ")

        val link = LinkAnnotation.Url(
            url = HARDCOVER_API_KEY_URL,
            linkInteractionListener = { openUrl(HARDCOVER_API_KEY_URL) },
        )

        withLink(link = link) {
            withStyle(style = SpanStyle(color = linkColor)) {
                append("here")
                appendInlineContent(
                    OPEN_IN_NEW_INLINE_CONTENT_ID,
                    "[external link]",
                )
            }
        }

        append(".")
    }

    val openInNewIcon = drawableIconResource(
        icon = SoftcoverIcon.OpenInNew,
        contentDescription = "",
    )

    SoftcoverText(
        text = annotatedString,
        style = MaterialTheme.editorialTypography.bodySmall.copy(
            fontSize = 13.sp,
            lineHeight = 19.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
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
        Divider()

        Spacer(modifier = Modifier.height(Spacing.md))

        SoftcoverText(
            text = "Where to find it".uppercase(),
            style = MaterialTheme.editorialTypography.eyebrowSmall.copy(
                fontSize = 10.5.sp,
                lineHeight = 14.sp,
                letterSpacing = 2.sp,
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(Spacing.md))

        Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
            ExplainerStepRow(
                number = "01",
                label = "Sign in at hardcover.app",
                sub = "Free, a minute to set up.",
            )

            ExplainerStepRow(
                number = "02",
                label = "Open Settings → API",
                sub = "Your API key is on this page.",
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
    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
        SoftcoverText(
            text = number,
            style = MaterialTheme.editorialTypography.headlineSmall.copy(
                fontSize = 17.sp,
                lineHeight = 17.sp,
            ).tabularFigures(),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .width(22.dp)
                .alignByBaseline(),
        )

        Column(modifier = Modifier.alignByBaseline()) {
            SoftcoverText(
                text = label,
                style = MaterialTheme.editorialTypography.titleSmall.copy(
                    fontSize = 13.5.sp,
                    lineHeight = 18.sp,
                ),
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(modifier = Modifier.height(Spacing.xxxs))

            SoftcoverText(
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
