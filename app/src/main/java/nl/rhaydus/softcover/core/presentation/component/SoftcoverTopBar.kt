package nl.rhaydus.softcover.core.presentation.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SoftcoverTopBar(
    title: String,
    navigateUp: (() -> Unit)? = null,
) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
            )
        },
        navigationIcon = {
            navigateUp?.let {
                IconButton(onClick = navigateUp) {
                    val icon = Icons.AutoMirrored.Outlined.ArrowBack

                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                    )
                }
            }
        },
    )
}