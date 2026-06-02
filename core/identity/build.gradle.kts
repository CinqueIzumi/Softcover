plugins {
    id("softcover.android.library")
}

android {
    namespace = "nl.rhaydus.softcover.core.identity"
}

dependencies {
    implementation(project(":core:domain"))
    api(project(":core:preferences"))
}
