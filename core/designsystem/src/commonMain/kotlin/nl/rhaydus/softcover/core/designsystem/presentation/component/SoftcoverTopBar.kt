package nl.rhaydus.softcover.core.designsystem.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.AppBarWithSearch
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBarState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberSearchBarState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.model.SoftcoverIconResource
import nl.rhaydus.softcover.core.designsystem.presentation.theme.SoftcoverTheme
import nl.rhaydus.softcover.core.designsystem.presentation.theme.StandardPreview

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SoftcoverSearchTopBar(
    searchText: String,
    onSearchValueChange: (String) -> Unit,
    placeHolder: String = "Search",
    isLoading: Boolean,
    searchBarState: SearchBarState = rememberSearchBarState(),
    onNavigateBack: (() -> Unit)? = null,
    actions: List<SoftcoverTopBarAction> = emptyList(),
    trailingFieldAction: SoftcoverTopBarAction? = null,
) {
    val trailingIcon: (@Composable () -> Unit)? = when {
        searchText.isNotEmpty() -> {
            {
                IconButton(
                    onClick = { onSearchValueChange("") },
                ) {
                    val icon = SoftcoverIconResource.Drawable(
                        icon = SoftcoverIcon.Close,
                        contentDescription = "Clear search icon",
                    )

                    Icon(
                        painter = icon.getIconPainter(),
                        contentDescription = icon.contentDescription,
                    )
                }
            }
        }

        trailingFieldAction != null -> {
            {
                IconButton(
                    onClick = trailingFieldAction.onClick,
                ) {
                    Icon(
                        painter = trailingFieldAction.iconResource.getIconPainter(),
                        contentDescription = trailingFieldAction.iconResource.contentDescription,
                    )
                }
            }
        }

        else -> null
    }

    AppBarWithSearch(
        state = searchBarState,
        inputField = {
            TextField(
                value = searchText,
                singleLine = true,
                trailingIcon = trailingIcon,
                leadingIcon = {
                    if (isLoading) {
                        CircularWavyProgressIndicator(modifier = Modifier.size(32.dp))
                    } else {
                        val icon = SoftcoverIconResource.Drawable(
                            icon = SoftcoverIcon.Search,
                            contentDescription = "Search",
                        )

                        Icon(
                            painter = icon.getIconPainter(),
                            contentDescription = icon.contentDescription,
                        )
                    }
                },
                onValueChange = onSearchValueChange,
                placeholder = { Text(text = placeHolder) },
                colors = TextFieldDefaults.colors().copy(
                    errorIndicatorColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                ),
            )
        },
        actions = {
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
        navigationIcon = {
            onNavigateBack?.let {
                IconButton(onClick = onNavigateBack) {
                    val icon = SoftcoverIconResource.Drawable(
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
    )
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
                val icon = SoftcoverIconResource.Drawable(
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
                        iconResource = SoftcoverIconResource.Drawable(
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
                placeHolder = "Search",
                isLoading = false,
            )

            SoftcoverSearchTopBar(
                onNavigateBack = {},
                searchText = "test",
                onSearchValueChange = {},
                isLoading = true,
                placeHolder = "Search",
                actions = List(2) {
                    SoftcoverTopBarAction(
                        iconResource = SoftcoverIconResource.Drawable(
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
