plugins {
    id("softcover.android.library")
}

android {
    namespace = "nl.rhaydus.softcover.core.identity"
}

dependencies {
    implementation(project(":core:domain"))
    implementation(project(":core:preferences"))
}
