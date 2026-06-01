package nl.rhaydus.softcover.feature.lists.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import nl.rhaydus.softcover.core.presentation.component.EditorialSectionHeader
import nl.rhaydus.softcover.core.presentation.component.SoftcoverButton
import nl.rhaydus.softcover.core.presentation.component.SoftcoverTopBar
import nl.rhaydus.softcover.core.presentation.model.ButtonSize
import nl.rhaydus.softcover.core.presentation.model.ButtonStyle
import nl.rhaydus.softcover.core.presentation.util.ObserveAsEvents
import nl.rhaydus.softcover.core.presentation.util.SnackBarManager
import nl.rhaydus.softcover.feature.lists.presentation.action.CreateListAction
import nl.rhaydus.softcover.feature.lists.presentation.action.OnNameChangedAction
import nl.rhaydus.softcover.feature.lists.presentation.action.OnSubmitAction
import nl.rhaydus.softcover.feature.lists.presentation.event.ListCreatedEvent
import nl.rhaydus.softcover.feature.lists.presentation.event.ListCreationFailedEvent
import nl.rhaydus.softcover.feature.lists.presentation.event.ListNameTakenEvent
import nl.rhaydus.softcover.feature.lists.presentation.screenmodel.CreateListScreenModel
import nl.rhaydus.softcover.feature.lists.presentation.state.CreateListUiState

class CreateListScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        val screenModel = koinScreenModel<CreateListScreenModel>()

        val state by screenModel.state.collectAsStateWithLifecycle()

        ObserveAsEvents(flow = screenModel.events) { event ->
            when (event) {
                is ListCreatedEvent -> {
                    SnackBarManager.showSnackbar(title = "List “${event.name}” created")

                    navigator.pop()
                }

                is ListNameTakenEvent -> SnackBarManager.showSnackbar(
                    title = "You already have a list called “${event.name}”. Pick another name.",
                )

                is ListCreationFailedEvent -> SnackBarManager.showSnackbar(
                    title = "Could not create list. Try again.",
                )
            }
        }

        Screen(
            state = state,
            runAction = screenModel::runAction,
            onNavigateBack = navigator::pop,
        )
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Screen(
        state: CreateListUiState,
        runAction: (CreateListAction) -> Unit,
        onNavigateBack: () -> Unit,
    ) {
        val focusRequester = remember { FocusRequester() }
        val keyboardController = LocalSoftwareKeyboardController.current

        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
        }

        Scaffold(
            topBar = {
                SoftcoverTopBar(
                    title = "New list",
                    onNavigateBack = onNavigateBack,
                )
            },
            contentWindowInsets = WindowInsets.statusBars,
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .imePadding()
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.Top,
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                EditorialSectionHeader(
                    eyebrow = "Custom list",
                    headline = "Name your list.",
                    description = "Pick a short label — you can rename it later.",
                )

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = state.name,
                    onValueChange = { runAction(OnNameChangedAction(newName = it)) },
                    shape = RoundedCornerShape(8.dp),
                    placeholder = { Text(text = "e.g. Summer reads") },
                    enabled = state.isSubmitting.not(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (state.canSubmit) {
                                keyboardController?.hide()

                                runAction(OnSubmitAction())
                            }
                        },
                    ),
                    colors = OutlinedTextFieldDefaults.colors().copy(
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                )

                Spacer(modifier = Modifier.height(24.dp))

                SoftcoverButton(
                    label = if (state.isSubmitting) "Creating" else "Create list",
                    style = ButtonStyle.FILLED,
                    size = ButtonSize.M,
                    enabled = state.canSubmit,
                    onClick = {
                        keyboardController?.hide()

                        runAction(OnSubmitAction())
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}
