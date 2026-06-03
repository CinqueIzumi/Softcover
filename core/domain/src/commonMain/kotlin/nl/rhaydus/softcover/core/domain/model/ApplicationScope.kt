package nl.rhaydus.softcover.core.domain.model

import kotlinx.coroutines.CoroutineScope
import kotlin.jvm.JvmInline

@JvmInline
value class ApplicationScope(val scope: CoroutineScope)
