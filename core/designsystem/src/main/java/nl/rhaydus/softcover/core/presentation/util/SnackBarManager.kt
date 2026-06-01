package nl.rhaydus.softcover.core.presentation.util

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

object SnackBarManager {
    private val _snackBarState = MutableStateFlow(SnackbarHostState())
    val snackBarState = _snackBarState.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val isShowing = AtomicBoolean(false)

    fun showSnackbar(
        title: String,
        duration: SnackbarDuration = SnackbarDuration.Short,
    ) {
        if (isShowing.compareAndSet(false, true).not()) return

        scope.launch {
            try {
                snackBarState.value.showSnackbar(
                    message = title,
                    duration = duration,
                )
            } finally {
                isShowing.set(false)
            }
        }
    }

    fun showSnackBar(
        title: String,
        actionLabel: String,
        duration: SnackbarDuration = SnackbarDuration.Long,
        onActionClick: () -> Unit,
        onDismiss: () -> Unit = {},
    ) {
        if (isShowing.compareAndSet(false, true).not()) return

        scope.launch {
            try {
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
            } finally {
                isShowing.set(false)
            }
        }
    }
}
