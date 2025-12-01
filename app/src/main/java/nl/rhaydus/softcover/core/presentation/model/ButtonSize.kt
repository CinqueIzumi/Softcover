package nl.rhaydus.softcover.core.presentation.model

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
enum class ButtonSize(val height: Dp) {
    XS(height = 32.dp),
    S(height = 40.dp),
    M(height = 56.dp),
    L(height = 96.dp),
    XL(height = 136.dp);

    val textStyle: TextStyle
        @Composable
        get() = ButtonDefaults.textStyleFor(buttonHeight = height)

    val contentPadding: PaddingValues
        @Composable
        get() = ButtonDefaults.contentPaddingFor(buttonHeight = height)

    val iconSize: Dp
        @Composable
        get() = ButtonDefaults.iconSizeFor(buttonHeight = height)

    val iconSpacing: Dp
        @Composable
        get() = ButtonDefaults.iconSpacingFor(buttonHeight = height)
}