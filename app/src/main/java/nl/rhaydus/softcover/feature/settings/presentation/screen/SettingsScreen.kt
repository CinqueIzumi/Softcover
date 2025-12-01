package nl.rhaydus.softcover.feature.settings.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.hilt.getViewModel
import nl.rhaydus.softcover.core.presentation.component.ClickableText
import nl.rhaydus.softcover.core.presentation.component.SoftcoverButton
import nl.rhaydus.softcover.core.presentation.model.ButtonStyle
import nl.rhaydus.softcover.core.presentation.theme.SoftcoverTheme
import nl.rhaydus.softcover.core.presentation.theme.StandardPreview
import nl.rhaydus.softcover.feature.settings.presentation.event.SettingsScreenUiEvent
import nl.rhaydus.softcover.feature.settings.presentation.state.SettingsScreenUiState
import nl.rhaydus.softcover.feature.settings.presentation.viewmodel.SettingsScreenViewModel

object SettingsScreen : Screen {
    @Composable
    override fun Content() {
        val viewModel = getViewModel<SettingsScreenViewModel>()

        val state by viewModel.uiState.collectAsStateWithLifecycle()

        val uriHandler = LocalUriHandler.current

        val clipboardManager = LocalClipboard.current

        Screen(
            state = state,
            onEvent = viewModel::onEvent,
            openUrl = uriHandler::openUri,
            getCopiedText = {
                val text: String = try {
                    clipboardManager
                        .nativeClipboard
                        .primaryClip
                        ?.getItemAt(0)
                        ?.text
                        ?.toString() ?: ""
                } catch (_: Exception) {
                    ""
                }

                text
            }
        )
    }

    @Composable
    fun Screen(
        state: SettingsScreenUiState,
        onEvent: (SettingsScreenUiEvent) -> Unit,
        openUrl: (String) -> Unit,
        getCopiedText: () -> String,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(color = MaterialTheme.colorScheme.surface)
                .verticalScroll(rememberScrollState())
                .imePadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(all = 16.dp),
                horizontalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
            ) {
                Text(
                    text = "API CONFIGURATION",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 8.dp)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceContainer,
                ) {
                    Column {
                        Column(
                            modifier = Modifier.padding(all = 16.dp),
                        ) {
                            Text(
                                text = "API Key",
                                style = MaterialTheme.typography.titleMedium,
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = state.apiKey,
                                onValueChange = {
                                    onEvent(SettingsScreenUiEvent.OnApiKeyValueChanged(it))
                                },
                                shape = RoundedCornerShape(8.dp),
                                placeholder = { Text(text = "Enter your key") },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors().copy(
                                    focusedContainerColor = Color.Black,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                ),
                                singleLine = true,
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            val annotatedString = buildAnnotatedString {
                                append("For syncing your reading progress. This key can be found ")

                                pushStringAnnotation(
                                    tag = "url",
                                    annotation = "https://hardcover.app/account/api"
                                )

                                withStyle(
                                    style = SpanStyle(
                                        color = MaterialTheme.colorScheme.primary,
                                        textDecoration = TextDecoration.Underline
                                    )
                                ) {
                                    append("here")
                                }

                                pop()
                            }

                            ClickableText(
                                annotatedText = annotatedString,
                                style = MaterialTheme.typography.labelSmall,
                                handleUrlClick = openUrl,
                            )
                        }

                        HorizontalDivider()

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val newValue = getCopiedText()

                                    onEvent(SettingsScreenUiEvent.OnApiKeyValueChanged(newValue = newValue))

                                    onEvent(SettingsScreenUiEvent.OnSaveApiKeyClick)
                                }
                                .padding(all = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentPaste,
                                contentDescription = "Paste icon",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                text = "Paste from clipboard",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                        }

                        HorizontalDivider()

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onEvent(SettingsScreenUiEvent.OnApiKeyValueChanged(newValue = ""))

                                    onEvent(SettingsScreenUiEvent.OnSaveApiKeyClick)
                                }
                                .padding(all = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete icon",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                text = "Clear API key",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                SoftcoverButton(
                    label = "Save API key",
                    onClick = {},
                    modifier = Modifier.fillMaxWidth(),
                    style = ButtonStyle.FILLED,
                )
            }
        }
    }
}

@StandardPreview
@Composable
private fun SettingsScreenPreview() {
    SoftcoverTheme {
        SettingsScreen.Screen(
            onEvent = {},
            getCopiedText = { "" },
            openUrl = {},
            state = SettingsScreenUiState()
        )
    }
}