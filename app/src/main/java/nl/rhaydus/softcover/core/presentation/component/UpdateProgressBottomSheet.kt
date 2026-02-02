package nl.rhaydus.softcover.core.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import nl.rhaydus.softcover.core.PreviewData
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.presentation.model.ButtonSize
import nl.rhaydus.softcover.core.presentation.model.ButtonStyle
import nl.rhaydus.softcover.core.presentation.modifier.conditional
import nl.rhaydus.softcover.core.presentation.theme.SoftcoverTheme
import nl.rhaydus.softcover.core.presentation.theme.StandardPreview
import nl.rhaydus.softcover.feature.reading.presentation.enums.ProgressSheetTab
import kotlin.math.min
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateProgressBottomSheet(
    bookToUpdate: Book,
    selectedTab: ProgressSheetTab,
    onProgressTabClick: (ProgressSheetTab) -> Unit,
    onUpdatePercentageClick: (String) -> Unit,
    onUpdatePageProgressClick: (String) -> Unit,
    onDismissRequest: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        ProgressBottomSheetContent(
            book = bookToUpdate,
            progressSheetTab = selectedTab,
            onProgressTabClick = onProgressTabClick,
            onUpdatePercentageClick = onUpdatePercentageClick,
            onUpdatePageProgressClick = onUpdatePageProgressClick,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProgressBottomSheetContent(
    progressSheetTab: ProgressSheetTab,
    book: Book,
    onProgressTabClick: (ProgressSheetTab) -> Unit,
    onUpdatePercentageClick: (String) -> Unit,
    onUpdatePageProgressClick: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(horizontal = 8.dp)
            .padding(bottom = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
        ) {
            Text(
                text = "Update progress",
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleLarge
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
        ) {
            Text(
                text = book.title,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyLarge
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    shape = RoundedCornerShape(4.dp)
                )
                .padding(all = 4.dp)
        ) {
            ProgressSheetTab.entries.forEach { tab ->
                val isSelected = tab == progressSheetTab

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            onProgressTabClick(tab)
                        }
                        .conditional(
                            condition = isSelected,
                            ifTrue = {
                                Modifier.background(
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = RoundedCornerShape(4.dp),
                                )
                            }
                        )
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = tab.tabName,
                        style = MaterialTheme.typography.titleMedium,
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (progressSheetTab) {
            ProgressSheetTab.PAGE -> {
                ProgressBottomSheetPageContent(
                    book = book,
                    onUpdatePageProgressClick = onUpdatePageProgressClick,
                )
            }

            ProgressSheetTab.PERCENTAGE -> {
                ProgressBottomSheetPercentageContent(
                    book = book,
                    onUpdatePercentageClick = onUpdatePercentageClick,
                )
            }
        }
    }
}

@Composable
private fun ColumnScope.ProgressBottomSheetPageContent(
    book: Book,
    onUpdatePageProgressClick: (String) -> Unit,
) {
    var number by remember {
        val currentPageString = book.currentPage.toString()

        mutableStateOf(TextFieldValue(text = currentPageString))
    }

    var firstTimeFocusedGained by remember { mutableStateOf(true) }

    val density = LocalDensity.current

    val textFieldTextStyle = MaterialTheme.typography.bodyLarge

    val textFieldWidth = remember(density) {
        with(density) {
            val fontSizeInPx = textFieldTextStyle.fontSize.toPx()
            val charCount = 4
            val padding = 32.dp.toPx()

            ((charCount * fontSizeInPx * 0.6f) + padding).toDp()
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Spacer(modifier = Modifier.weight(1f))

        OutlinedTextField(
            value = number,
            onValueChange = { newValue ->
                if (firstTimeFocusedGained.not()) {
                    number = number.copy(selection = newValue.selection)
                } else {
                    firstTimeFocusedGained = false
                }

                if (newValue.text == number.text) return@OutlinedTextField

                if (newValue.text.isEmpty()) {
                    number = newValue
                    return@OutlinedTextField
                }

                val newNumber = newValue.text.toIntOrNull() ?: run {
                    number = number.copy(text = "", selection = newValue.selection)
                    return@OutlinedTextField
                }

                val updatedNumber = min(newNumber, book.currentEdition.pages ?: 0)

                number = newValue.copy(text = updatedNumber.toString())
            },
            singleLine = true,
            textStyle = textFieldTextStyle.copy(textAlign = TextAlign.Center),
            modifier = Modifier
                .width(textFieldWidth)
                .onFocusChanged {
                    if (it.hasFocus.not()) {
                        firstTimeFocusedGained = true

                        number = number.copy(selection = TextRange.Zero)

                        return@onFocusChanged
                    }

                    number = number.copy(
                        selection = TextRange(start = 0, end = number.text.length),
                    )
                },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
        )

        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.CenterStart,
        ) {
            Text(
                text = "/ ${book.currentEdition.pages}",
                modifier = Modifier.padding(start = 8.dp),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    SoftcoverButton(
        label = "Update Progress",
        onClick = {
            onUpdatePageProgressClick(number.text)
        },
        modifier = Modifier.fillMaxWidth(),
        style = ButtonStyle.FILLED,
        size = ButtonSize.M,
    )

    Spacer(modifier = Modifier.height(4.dp))
}

@Composable
private fun ColumnScope.ProgressBottomSheetPercentageContent(
    book: Book,
    onUpdatePercentageClick: (String) -> Unit,
) {
    var number by remember {
        val currentPageString = book.progress?.roundToInt()

        mutableStateOf(TextFieldValue(text = currentPageString.toString()))
    }

    var firstTimeFocusedGained by remember { mutableStateOf(true) }

    val density = LocalDensity.current

    val textFieldTextStyle = MaterialTheme.typography.bodyLarge

    val textFieldWidth = remember(density) {
        with(density) {
            val fontSizeInPx = textFieldTextStyle.fontSize.toPx()
            val charCount = 4
            val padding = 32.dp.toPx()

            ((charCount * fontSizeInPx * 0.6f) + padding).toDp()
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Spacer(modifier = Modifier.weight(1f))

        OutlinedTextField(
            value = number,
            onValueChange = { newValue ->
                if (firstTimeFocusedGained.not()) {
                    number = number.copy(selection = newValue.selection)
                } else {
                    firstTimeFocusedGained = false
                }

                if (newValue.text == number.text) return@OutlinedTextField

                if (newValue.text.isEmpty()) {
                    number = newValue
                    return@OutlinedTextField
                }

                val newNumber = newValue.text.toIntOrNull() ?: run {
                    number = number.copy(text = "", selection = newValue.selection)
                    return@OutlinedTextField
                }

                val updatedNumber = min(newNumber, 100)

                number = newValue.copy(text = updatedNumber.toString())
            },
            singleLine = true,
            textStyle = textFieldTextStyle.copy(textAlign = TextAlign.Center),
            modifier = Modifier
                .width(textFieldWidth)
                .onFocusChanged {
                    if (it.hasFocus.not()) {
                        firstTimeFocusedGained = true

                        number = number.copy(selection = TextRange.Zero)

                        return@onFocusChanged
                    }

                    number = number.copy(
                        selection = TextRange(start = 0, end = number.text.length),
                    )
                },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
        )

        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.CenterStart,
        ) {
            Text(
                text = "%",
                modifier = Modifier.padding(start = 8.dp),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    SoftcoverButton(
        label = "Update Progress",
        modifier = Modifier.fillMaxWidth(),
        style = ButtonStyle.FILLED,
        size = ButtonSize.M,
        onClick = {
            onUpdatePercentageClick(number.text)
        }
    )

    Spacer(modifier = Modifier.height(4.dp))
}

@StandardPreview
@Composable
private fun ProgressSheetContentPagePreview() {
    SoftcoverTheme {
        ProgressBottomSheetContent(
            onUpdatePageProgressClick = {},
            onProgressTabClick = {},
            onUpdatePercentageClick = {},
            progressSheetTab = ProgressSheetTab.PAGE,
            book = PreviewData.baseBook.copy(
                title = "The Dungeon Anarchist's Cookbook",
                editions = listOf(
                    PreviewData.baseEdition.copy(
                        pages = 534,
                    )
                ),
                currentPage = 80,
            ),
        )
    }
}

@StandardPreview
@Composable
private fun ProgressSheetContentPercentagePreview() {
    SoftcoverTheme {
        ProgressBottomSheetContent(
            onUpdatePageProgressClick = {},
            onProgressTabClick = {},
            onUpdatePercentageClick = {},
            progressSheetTab = ProgressSheetTab.PERCENTAGE,
            book = PreviewData.baseBook.copy(
                title = "The Dungeon Anarchist's Cookbook",
                editions = listOf(
                    PreviewData.baseEdition.copy(
                        pages = 534,
                    )
                ),
                currentPage = 80,
                progress = 80f
            ),
        )
    }
}