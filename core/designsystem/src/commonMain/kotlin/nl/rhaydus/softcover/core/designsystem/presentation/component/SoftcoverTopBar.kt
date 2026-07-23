package nl.rhaydus.softcover.core.designsystem.presentation.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.modifier.noRippleClickable
import nl.rhaydus.designsystem.modifier.pointerHandCursor
import nl.rhaydus.designsystem.theme.StandardPreview
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.icon.drawableIconResource
import nl.rhaydus.softcover.core.designsystem.presentation.theme.SoftcoverTheme

/**
 * The Explore search chrome (explore-3a §4 "Search chrome"): a rounded pill (search glyph, inline
 * text field, and a trailing clear/loading affordance) beside a square barcode-scan button. [active]
 * — true whenever the caller's `searchPhase` is not the plain feed (focused, loading, or showing
 * results/a mood browse) — grows a 1.5dp primary border on the pill and reveals the clear (×); the
 * feed's resting pill carries neither.
 *
 * **The caller's state owns the focus, not the platform field.** [focused] is the caller's
 * search-chrome state and the field *follows* it: entering it requests focus and shows the
 * keyboard, leaving it clears focus and hides the keyboard. The callbacks are therefore intents,
 * not focus notifications — [onSearchActivated] fires on every tap of the pill (and on a focus gain
 * from elsewhere, e.g. a desktop Tab key), [onSearchDismissed] on a focus loss, [onClearSearch] on
 * the ×. The tap intent is what keeps the chrome recoverable: `onFocusChanged` is edge-triggered,
 * so a field that still holds platform focus while the caller's state says otherwise would never
 * report anything again — leaving a live cursor and an open keyboard over a screen that refuses to
 * open its search surface.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SoftcoverSearchTopBar(
    searchText: String,
    onSearchValueChange: (String) -> Unit,
    onScanClick: () -> Unit,
    isLoading: Boolean,
    active: Boolean,
    focused: Boolean,
    onSearchActivated: () -> Unit,
    onSearchDismissed: () -> Unit,
    onClearSearch: () -> Unit,
    placeholder: String = "Search books, authors…",
) {
    Surface(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(
                    horizontal = 16.dp,
                    vertical = 12.dp,
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            SearchChromePill(
                query = searchText,
                onQueryChange = onSearchValueChange,
                focused = focused,
                onSearchActivated = onSearchActivated,
                onSearchDismissed = onSearchDismissed,
                onClearSearch = onClearSearch,
                isLoading = isLoading,
                active = active,
                placeholder = placeholder,
                modifier = Modifier.weight(1f),
            )

            SearchChromeBarcodeButton(onClick = onScanClick)
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun SearchChromePill(
    query: String,
    onQueryChange: (String) -> Unit,
    focused: Boolean,
    onSearchActivated: () -> Unit,
    onSearchDismissed: () -> Unit,
    onClearSearch: () -> Unit,
    isLoading: Boolean,
    active: Boolean,
    placeholder: String,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }

    var fieldHoldsFocus by remember { mutableStateOf(false) }

    // Only *transitions* of [focused] drive the platform field. The caller's state outlives this
    // composition (it lives in a ScreenModel) while the field's platform focus dies with it, so
    // re-entering the screen with a search still active must not yank the keyboard open unbidden —
    // only a fresh activation may, and that one is handled by the tap itself (see
    // SearchChromeInputArea). The explicit show/hide is the belt to that braces: a field can hold
    // focus with its input session already torn down, and re-showing is what reconnects the
    // keyboard that would otherwise sit there swallowing keystrokes.
    var appliedFocus by remember { mutableStateOf(focused) }

    LaunchedEffect(focused) {
        if (focused == appliedFocus) return@LaunchedEffect

        appliedFocus = focused

        if (focused) {
            focusRequester.requestFocus()
            keyboardController?.show()
        } else if (fieldHoldsFocus) {
            focusManager.clearFocus()
            keyboardController?.hide()
        }
    }

    Surface(
        modifier = modifier.height(46.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = if (active) BorderStroke(
            1.5.dp,
            MaterialTheme.colorScheme.primary,
        ) else null,
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            SearchChromeInputArea(
                query = query,
                onQueryChange = onQueryChange,
                onSearchActivated = onSearchActivated,
                onSearchDismissed = onSearchDismissed,
                onFieldFocusChange = { fieldHoldsFocus = it },
                focusRequester = focusRequester,
                placeholder = placeholder,
                modifier = Modifier.weight(1f),
            )

            when {
                isLoading -> CircularWavyProgressIndicator(modifier = Modifier.size(18.dp))

                active -> {
                    val clearIcon = drawableIconResource(
                        icon = SoftcoverIcon.Close,
                        contentDescription = "Clear search",
                    )

                    Icon(
                        painter = clearIcon.getIconPainter(),
                        contentDescription = clearIcon.contentDescription,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .size(18.dp)
                            .noRippleClickable { onClearSearch() },
                    )
                }
            }
        }
    }
}

/**
 * The pill's tap-to-search region: the leading glyph and the text field. The whole region — not
 * just the field's own hit box — handles the tap, observed on the initial pass and consuming
 * nothing, so it never interferes with the field's own tap handling. Two things ride on that: the
 * glyph and the padding around the field stop being dead zones, and a tap on an *already-focused*
 * field is still an activation, which the edge-triggered [Modifier.onFocusChanged] cannot see. The
 * trailing clear/loading slot sits deliberately outside this region — tapping × is a dismissal, not
 * an activation.
 *
 * A tap drives the platform side (focus, keyboard) here rather than only reporting
 * [onSearchActivated] and waiting for the state to come back, because "activate" is a level, not a
 * pulse: re-activating while the caller already holds `focused = true` writes an equal state that a
 * `StateFlow` drops, so nothing downstream would fire. That is not hypothetical — the system back
 * button hides the keyboard without touching the field's focus, and every tap after that would be
 * swallowed. Both calls are no-ops when the field is already focused with its keyboard up.
 */
@Composable
private fun SearchChromeInputArea(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearchActivated: () -> Unit,
    onSearchDismissed: () -> Unit,
    onFieldFocusChange: (Boolean) -> Unit,
    focusRequester: FocusRequester,
    placeholder: String,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    Row(
        modifier = modifier.pointerInput(Unit) {
            awaitEachGesture {
                awaitFirstDown(
                    requireUnconsumed = false,
                    pass = PointerEventPass.Initial,
                )

                onSearchActivated()

                focusRequester.requestFocus()
                keyboardController?.show()
            }
        },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        val searchIcon = drawableIconResource(
            icon = SoftcoverIcon.Search,
            contentDescription = "Search",
        )

        Icon(
            painter = searchIcon.getIconPainter(),
            contentDescription = searchIcon.contentDescription,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp),
        )

        Box(modifier = Modifier.weight(1f)) {
            if (query.isEmpty()) {
                Text(
                    text = placeholder,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                singleLine = true,
                textStyle = LocalTextStyle.current.merge(
                    MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onSurface,
                    ),
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                // The results already stream in on a debounce, so the IME's search key has nothing
                // left to submit: it just puts the keyboard away over the results it produced.
                keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
                    .onFocusChanged { focusState ->
                        onFieldFocusChange(focusState.isFocused)

                        if (focusState.isFocused) {
                            onSearchActivated()
                        } else {
                            onSearchDismissed()
                        }
                    },
            )
        }
    }
}

@Composable
private fun SearchChromeBarcodeButton(onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .size(46.dp)
            .pointerHandCursor(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            val scanIcon = drawableIconResource(
                icon = SoftcoverIcon.BarcodeScanner,
                contentDescription = "Scan a book's barcode",
            )

            Icon(
                painter = scanIcon.getIconPainter(),
                contentDescription = scanIcon.contentDescription,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SoftcoverTopBar(
    title: String,
    subTitle: String? = null,
    actions: List<SoftcoverTopBarAction> = emptyList(),
    titleAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
    onNavigateBack: (() -> Unit)? = null,
    colors: TopAppBarColors = TopAppBarDefaults.topAppBarColors(),
    scrollBehavior: TopAppBarScrollBehavior? = null,
    navigateBackButton: @Composable () -> Unit = {
        onNavigateBack?.let {
            IconButton(onClick = onNavigateBack) {
                val icon = drawableIconResource(
                    icon = SoftcoverIcon.ArrowBack,
                    contentDescription = "Navigate back icon",
                )

                Icon(
                    painter = icon.getIconPainter(),
                    contentDescription = icon.contentDescription,
                )
            }
        }
    },
    additionalActions: @Composable RowScope.() -> Unit = {},
) {
    val givenSubtitle: @Composable () -> Unit = { subTitle?.let { Text(text = subTitle) } }

    TopAppBar(
        title = {
            Text(
                text = title,
                autoSize = TextAutoSize.StepBased(maxFontSize = MaterialTheme.typography.titleLarge.fontSize),
                maxLines = 2,
            )
        },
        subtitle = givenSubtitle,
        scrollBehavior = scrollBehavior,
        titleHorizontalAlignment = titleAlignment,
        colors = colors,
        actions = {
            additionalActions()

            actions.forEach { action ->
                IconButton(onClick = action.onClick) {
                    val resource = action.iconResource

                    Icon(
                        painter = resource.getIconPainter(),
                        contentDescription = resource.contentDescription,
                    )
                }
            }
        },
        navigationIcon = navigateBackButton,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@StandardPreview
@Composable
private fun SoftcoverTopBarPreview() {
    SoftcoverTheme() {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            SoftcoverTopBar(
                title = "given title",
                titleAlignment = Alignment.Start,
            )

            SoftcoverTopBar(
                title = "given title",
                onNavigateBack = {},
                subTitle = "subtitle",
                actions = List(2) {
                    SoftcoverTopBarAction(
                        iconResource = drawableIconResource(
                            icon = SoftcoverIcon.Palette,
                            contentDescription = "",
                        ),
                        onClick = {},
                    )
                },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@StandardPreview
@Composable
private fun SoftcoverSearchTopBarPreview() {
    SoftcoverTheme() {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            SoftcoverSearchTopBar(
                searchText = "",
                onSearchValueChange = {},
                onScanClick = {},
                onSearchActivated = {},
                onSearchDismissed = {},
                onClearSearch = {},
                focused = false,
                active = false,
                isLoading = false,
            )

            SoftcoverSearchTopBar(
                searchText = "",
                onSearchValueChange = {},
                onScanClick = {},
                onSearchActivated = {},
                onSearchDismissed = {},
                onClearSearch = {},
                focused = true,
                active = true,
                isLoading = false,
            )

            SoftcoverSearchTopBar(
                searchText = "Piranesi",
                onSearchValueChange = {},
                onScanClick = {},
                onSearchActivated = {},
                onSearchDismissed = {},
                onClearSearch = {},
                focused = true,
                active = true,
                isLoading = true,
            )
        }
    }
}
