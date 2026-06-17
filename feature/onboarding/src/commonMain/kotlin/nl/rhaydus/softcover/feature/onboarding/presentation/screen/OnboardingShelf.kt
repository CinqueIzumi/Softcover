package nl.rhaydus.softcover.feature.onboarding.presentation.screen

import nl.rhaydus.designsystem.modifier.pointerHandCursor
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import nl.rhaydus.softcover.core.designsystem.presentation.component.ClickableText
import nl.rhaydus.softcover.core.designsystem.presentation.component.EditorialSectionHeader
import nl.rhaydus.softcover.core.designsystem.presentation.component.SoftcoverButton
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.icon.drawableIconResource
import nl.rhaydus.softcover.core.designsystem.presentation.model.ButtonSize
import nl.rhaydus.softcover.core.designsystem.presentation.model.ButtonStyle
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.feature.onboarding.presentation.action.OnApiKeySaveClickAction
import nl.rhaydus.softcover.feature.onboarding.presentation.action.OnApiKeyValueChangeAction
import nl.rhaydus.softcover.feature.onboarding.presentation.action.OnboardingAction
import nl.rhaydus.softcover.feature.onboarding.presentation.state.OnboardingUiState

/**
 * The Hardcover API-key entry form, shared verbatim by both the mobile pager's final page and the
 * desktop single-panel onboarding layout. Outer padding is the caller's responsibility (mobile sets
 * the page margins, desktop the editorial measure). Hover/cursor affordances are baked into the
 * desktop-only modifiers — inert on touch, so mobile renders identically.
 */
@Composable
internal fun ApiKeyEntrySection(
    state: OnboardingUiState,
    runAction: (action: OnboardingAction) -> Unit,
    openUrl: (String) -> Unit,
    getCopiedText: () -> String,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        EditorialSectionHeader(
            eyebrow = "API key",
            headline = "Add your key.",
            description = "Softcover uses your Hardcover API key to sync reading progress.",
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = state.apiKeyValue,
            onValueChange = {
                runAction(OnApiKeyValueChangeAction(newValue = it))
            },
            shape = RoundedCornerShape(8.dp),
            placeholder = { Text(text = "Enter your key") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors().copy(
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
            ),
            singleLine = true,
        )

        Spacer(modifier = Modifier.height(16.dp))

        val annotatedString = buildAnnotatedString {
            append("Find your key on the Hardcover website ")

            pushStringAnnotation(
                tag = "url",
                annotation = "https://hardcover.app/account/api",
            )

            withStyle(
                style = SpanStyle(
                    color = MaterialTheme.colorScheme.primary,
                    textDecoration = TextDecoration.Underline,
                ),
            ) {
                append("here")
            }

            append(".")

            pop()
        }

        ClickableText(
            annotatedText = annotatedString,
            style = MaterialTheme.editorialTypography.bodySmall.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
            handleUrlClick = openUrl,
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "If you already have the Hardcover app installed, this key can also be found " +
                "there: Profile → Settings → Hardcover API.",
            style = MaterialTheme.editorialTypography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(20.dp))

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .pointerHandCursor()
                .clickable {
                    val newValue = getCopiedText()

                    runAction(OnApiKeyValueChangeAction(newValue = newValue))
                }
                .padding(all = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val pasteIcon = drawableIconResource(
                icon = SoftcoverIcon.ContentPaste,
                contentDescription = "Paste icon",
            )

            Icon(
                painter = pasteIcon.getIconPainter(),
                contentDescription = pasteIcon.contentDescription,
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.primary,
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = "Paste from clipboard",
                style = MaterialTheme.editorialTypography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        SoftcoverButton(
            label = "Save API key",
            onClick = { runAction(OnApiKeySaveClickAction()) },
            modifier = Modifier
                .fillMaxWidth()
                .pointerHandCursor(),
            style = ButtonStyle.FILLED,
            size = ButtonSize.M,
            enabled = state.saveApiKeyButtonEnabled,
        )
    }
}
