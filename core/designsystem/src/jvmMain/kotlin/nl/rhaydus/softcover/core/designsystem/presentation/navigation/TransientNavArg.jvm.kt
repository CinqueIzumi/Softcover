package nl.rhaydus.softcover.core.designsystem.presentation.navigation

import kotlin.jvm.Transient

// Desktop, like Android, runs on the JVM where Voyager's `Screen` is `java.io.Serializable`, so a
// non-serializable nav argument must be dropped from the back-stack's serialization — same as the
// Android actual.
actual typealias TransientNavArg = Transient
