package nl.rhaydus.softcover.core.presentation.util

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object SnackBarManager {
    private val _snackBarState = MutableStateFlow(SnackbarHostState())
    val snackBarState = _snackBarState.asStateFlow()

    suspend fun showSnackbar(
        title: String,
        duration: SnackbarDuration = SnackbarDuration.Short,
    ) {
        snackBarState.value.showSnackbar(
            message = title,
            duration = duration
        )
    }

    suspend fun showSnackBar(
        title: String,
        actionLabel: String,
        duration: SnackbarDuration = SnackbarDuration.Long,
        onActionClick: () -> Unit,
        onDismiss: () -> Unit = {},
    ) {
        val result = snackBarState.value.showSnackbar(
            message = title,
            actionLabel = actionLabel,
            duration = duration,
            withDismissAction = true,
        )

        when (result) {
            SnackbarResult.Dismissed -> onDismiss()
            SnackbarResult.ActionPerformed -> onActionClick()
        }
    }
}