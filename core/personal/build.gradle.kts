plugins {
    id("softcover.android.library")
}

android {
    namespace = "nl.rhaydus.softcover.core.personal"
}

dependencies {
    api(project(":core:domain"))
    api(project(":core:database"))
}
