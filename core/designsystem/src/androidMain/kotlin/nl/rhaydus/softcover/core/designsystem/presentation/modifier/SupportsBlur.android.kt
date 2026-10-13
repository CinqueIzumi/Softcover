package nl.rhaydus.softcover.core.designsystem.presentation.modifier

import android.os.Build
import androidx.annotation.ChecksSdkIntAtLeast

@ChecksSdkIntAtLeast(api = Build.VERSION_CODES.S)
actual fun supportsBlur(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
