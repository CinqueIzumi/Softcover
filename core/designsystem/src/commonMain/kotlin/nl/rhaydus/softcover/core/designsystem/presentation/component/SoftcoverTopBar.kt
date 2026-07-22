package nl.rhaydus.softcover.core.designsystem.presentation.component

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
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
 * feed's resting pill carries neither. [onFocusChange] threads the field's platform focus state back
 * to `OnSearchFocusChangedAction` so the caller can swap the feed for the focus/results surface.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SoftcoverSearchTopBar(
    searchText: String,
    onSearchValueChange: (String) -> Unit,
    onScanClick: () -> Unit,
    isLoading: Boolean,
    active: Boolean,
    onFocusChange: (Boolean) -> Unit,
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
                onFocusChange = onFocusChange,
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
    onFocusChange: (Boolean) -> Unit,
    isLoading: Boolean,
    active: Boolean,
    placeholder: String,
    modifier: Modifier = Modifier,
) {
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { onFocusChange(it.isFocused) },
                )
            }

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
                            .noRippleClickable {
                                onQueryChange("")
                                onFocusChange(false)
                            },
                    )
                }
            }
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
                onFocusChange = {},
                active = false,
                isLoading = false,
            )

            SoftcoverSearchTopBar(
                searchText = "",
                onSearchValueChange = {},
                onScanClick = {},
                onFocusChange = {},
                active = true,
                isLoading = false,
            )

            SoftcoverSearchTopBar(
                searchText = "Piranesi",
                onSearchValueChange = {},
                onScanClick = {},
                onFocusChange = {},
                active = true,
                isLoading = true,
            )
        }
    }
}
