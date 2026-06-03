package nl.rhaydus.softcover.core.domain.logging

import co.touchlab.kermit.DefaultFormatter
import co.touchlab.kermit.Logger
import co.touchlab.kermit.Message
import co.touchlab.kermit.MessageStringFormatter
import co.touchlab.kermit.Severity
import co.touchlab.kermit.Tag
import co.touchlab.kermit.loggerConfigInit
import co.touchlab.kermit.platformLogWriter

/**
 * App-wide logging facade. Backed by Kermit so it logs on every Kotlin target (Logcat on Android,
 * os_log on iOS, stdout on JVM); the call sites stay platform-agnostic.
 *
 * Named `AppLog` rather than `Log` to avoid colliding with `android.util.Log` in reading and review.
 * Call [install] once at startup to enable output (debug builds only, mirroring the previous
 * Timber-debug-tree behaviour).
 */
object AppLog {
    private const val TAG = "Softcover"
    private const val PREFIX = "-=-"

    @Volatile
    private var logger: Logger = buildLogger(debug = false)

    /** Enables logging output. In release (`debug = false`) no writer is installed, so calls are no-ops. */
    fun install(debug: Boolean) {
        logger = buildLogger(debug = debug)
    }

    fun i(message: String) = logger.i { message }

    fun i(
        throwable: Throwable,
        message: String,
    ) = logger.i(throwable) { message }

    fun w(message: String) = logger.w { message }

    fun w(
        throwable: Throwable,
        message: String,
    ) = logger.w(throwable) { message }

    fun e(message: String) = logger.e { message }

    fun e(
        throwable: Throwable,
        message: String,
    ) = logger.e(throwable) { message }

    fun e(throwable: Throwable) = logger.e(throwable) { throwable.message ?: throwable.toString() }

    private fun buildLogger(debug: Boolean): Logger = Logger(
        config = if (debug) {
            loggerConfigInit(platformLogWriter(PrefixFormatter))
        } else {
            loggerConfigInit()
        },
        tag = TAG,
    )

    private object PrefixFormatter : MessageStringFormatter {
        override fun formatSeverity(severity: Severity): String = DefaultFormatter.formatSeverity(severity)

        override fun formatTag(tag: Tag): String = DefaultFormatter.formatTag(tag)

        override fun formatMessage(
            severity: Severity?,
            tag: Tag?,
            message: Message,
        ): String {
            val formatted = DefaultFormatter.formatMessage(
                severity,
                tag,
                message,
            )

            return formatted
                .lineSequence()
                .joinToString(separator = "\n") { line -> "$PREFIX $line" }
        }
    }
}
