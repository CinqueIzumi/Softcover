package nl.rhaydus.softcover.core.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Search
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
import nl.rhaydus.softcover.core.presentation.model.SoftcoverIconResource
import nl.rhaydus.softcover.core.presentation.theme.SoftcoverTheme
import nl.rhaydus.softcover.core.presentation.theme.StandardPreview

data class SoftcoverTopBarAction(
    val iconResource: SoftcoverIconResource,
    val onClick: () -> Unit,
)

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
) {
    val trailingIcon: (@Composable () -> Unit)? = if (searchText.isNotEmpty()) {
        {
            IconButton(
                onClick = { onSearchValueChange("") }
            ) {
                Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = "Clear search icon"
                )
            }
        }
    } else {
        null
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
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
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
                )
            )
        },
        actions = {
            actions.forEach { action ->
                IconButton(onClick = action.onClick) {
                    val resource = action.iconResource

                    Icon(
                        painter = resource.getIconPainter(),
                        contentDescription = resource.contentDescription
                    )
                }
            }
        },
        navigationIcon = {
            onNavigateBack?.let {
                IconButton(onClick = onNavigateBack) {
                    val icon = Icons.AutoMirrored.Default.ArrowBack

                    Icon(imageVector = icon, contentDescription = "Navigate back icon")
                }
            }
        }
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
                val icon = Icons.AutoMirrored.Default.ArrowBack

                Icon(imageVector = icon, contentDescription = "Navigate back icon")
            }
        }
    },
) {
    val givenSubtitle: @Composable () -> Unit = { subTitle?.let { Text(text = subTitle) } }

    TopAppBar(
        title = {
            Text(
                text = title,
                autoSize = TextAutoSize.StepBased(maxFontSize = MaterialTheme.typography.titleLarge.fontSize),
                maxLines = 2
            )
        },
        subtitle = givenSubtitle,
        scrollBehavior = scrollBehavior,
        titleHorizontalAlignment = titleAlignment,
        colors = colors,
        actions = {
            actions.forEach { action ->
                IconButton(onClick = action.onClick) {
                    val resource = action.iconResource

                    Icon(
                        painter = resource.getIconPainter(),
                        contentDescription = resource.contentDescription
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
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SoftcoverTopBar(title = "given title", titleAlignment = Alignment.Start)

            SoftcoverTopBar(
                title = "given title",
                onNavigateBack = {},
                subTitle = "subtitle",
                actions = List(2) {
                    SoftcoverTopBarAction(
                        iconResource = SoftcoverIconResource.Vector(
                            vector = Icons.Default.Favorite,
                            contentDescription = ""
                        ),
                        onClick = {}
                    )
                }
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
            verticalArrangement = Arrangement.spacedBy(8.dp)
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
                        iconResource = SoftcoverIconResource.Vector(
                            vector = Icons.Default.Favorite,
                            contentDescription = ""
                        ),
                        onClick = {}
                    )
                }
            )
        }
    }
}