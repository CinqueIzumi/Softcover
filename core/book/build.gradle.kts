plugins {
    id("softcover.android.library")
}

android {
    namespace = "nl.rhaydus.softcover.core.book"
}

dependencies {
    api(project(":core:domain"))
    implementation(project(":core:database"))
    api(project(":core:network"))
}
