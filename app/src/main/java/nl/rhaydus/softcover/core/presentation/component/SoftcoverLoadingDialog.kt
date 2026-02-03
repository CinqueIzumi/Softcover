package nl.rhaydus.softcover.core.presentation.component

import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.window.Dialog

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SoftcoverLoadingDialog(isLoading: Boolean) {
    if (isLoading) {
        Dialog(onDismissRequest = {}) {
            ContainedLoadingIndicator()
        }
    }
}