package nl.rhaydus.softcover.core.designsystem.presentation.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.editorial.component.EditorialSearchField
import nl.rhaydus.designsystem.theme.StandardPreview
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.icon.drawableIconResource
import nl.rhaydus.softcover.core.designsystem.presentation.theme.SoftcoverTheme

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SoftcoverSearchTopBar(
    searchText: String,
    onSearchValueChange: (String) -> Unit,
    onScanClick: () -> Unit,
    isLoading: Boolean,
    placeholder: String = "Search",
) {
    Surface(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(
                    horizontal = 16.dp,
                    vertical = 8.dp,
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            EditorialSearchField(
                modifier = Modifier.weight(1f),
                query = searchText,
                onQueryChange = onSearchValueChange,
                onClearClick = { onSearchValueChange("") },
                searchIcon = drawableIconResource(
                    icon = SoftcoverIcon.Search,
                    contentDescription = "Search",
                ),
                clearIcon = drawableIconResource(
                    icon = SoftcoverIcon.Close,
                    contentDescription = "Clear search",
                ),
                placeholder = placeholder,
            )

            AnimatedVisibility(visible = isLoading) {
                CircularWavyProgressIndicator(modifier = Modifier.size(24.dp))
            }

            IconButton(onClick = onScanClick) {
                val icon = drawableIconResource(
                    icon = SoftcoverIcon.BarcodeScanner,
                    contentDescription = "Scan a book's barcode",
                )

                Icon(
                    painter = icon.getIconPainter(),
                    contentDescription = icon.contentDescription,
                )
            }
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
                placeholder = "Search",
                isLoading = false,
            )

            SoftcoverSearchTopBar(
                searchText = "Piranesi",
                onSearchValueChange = {},
                onScanClick = {},
                placeholder = "Search",
                isLoading = true,
            )
        }
    }
}
