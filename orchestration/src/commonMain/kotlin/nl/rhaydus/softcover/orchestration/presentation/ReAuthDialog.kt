package nl.rhaydus.softcover.orchestration.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import nl.rhaydus.designsystem.component.RhaydusButton
import nl.rhaydus.designsystem.model.ButtonStyle
import nl.rhaydus.designsystem.modifier.pointerHandCursor
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography

/**
 * App-root overlay shown when the stored Hardcover token is rejected mid-session. The dialog is
 * intentionally non-dismissible: the only two exits are a successful re-authentication (auto-closes
 * when [ReAuthState] resets) and an explicit log-out via [onLogOut].
 *
 * Local state for the text field lives here with [rememberSaveable] so it survives configuration
 * change; it is NOT part of the external state contract.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun ReAuthDialog(
    isSubmitting: Boolean,
    errorMessage: String?,
    onSubmit: (apiKey: String) -> Unit,
    onLogOut: () -> Unit,
) {
    var apiKey by rememberSaveable { mutableStateOf("") }

    // Non-dismissible: no back-press/outside-tap exit. The only exits are submit-success and log-out.
    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
        ),
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            // surfaceContainerLowest is reserved for overlays that sit above the page (design-system §2.1).
            color = MaterialTheme.colorScheme.surfaceContainerLowest,
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = "Session expired",
                    style = MaterialTheme.editorialTypography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Your Hardcover API token was rejected. " +
                        "Paste a valid token below to continue without losing your local data.",
                    style = MaterialTheme.editorialTypography.body,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(modifier = Modifier.height(20.dp))

                OutlinedTextField(
                    value = apiKey,
                    onValueChange = { apiKey = it },
                    label = { Text(text = "API token") },
                    placeholder = { Text(text = "Paste your Hardcover API token") },
                    enabled = isSubmitting.not(),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors().copy(
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = errorMessage,
                        style = MaterialTheme.editorialTypography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(
                        space = 8.dp,
                        alignment = Alignment.End,
                    ),
                ) {
                    RhaydusButton(
                        label = "Log out",
                        style = ButtonStyle.TEXT,
                        onClick = onLogOut,
                        enabled = isSubmitting.not(),
                        modifier = Modifier.pointerHandCursor(),
                    )

                    Box(contentAlignment = Alignment.Center) {
                        if (isSubmitting) {
                            CircularWavyProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.primary,
                            )
                        } else {
                            RhaydusButton(
                                label = "Re-authenticate",
                                style = ButtonStyle.FILLED,
                                enabled = apiKey.isNotBlank(),
                                onClick = { onSubmit(apiKey) },
                                modifier = Modifier.pointerHandCursor(),
                            )
                        }
                    }
                }
            }
        }
    }
}
