package nl.rhaydus.softcover.core.platform.logging

import timber.log.Timber

class PrefixedDebugTree(
    private val prefix: String,
) : Timber.DebugTree() {
    override fun log(
        priority: Int,
        tag: String?,
        message: String,
        t: Throwable?,
    ) {
        val prefixed = message
            .lineSequence()
            .joinToString(separator = "\n") { line -> "$prefix $line" }

        super.log(
            priority,
            tag,
            prefixed,
            t,
        )
    }
}
