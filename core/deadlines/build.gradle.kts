plugins {
    id("softcover.android.library")
}

android {
    namespace = "nl.rhaydus.softcover.core.deadlines"
}

dependencies {
    api(project(":core:domain"))
    implementation(project(":core:database"))
    implementation(project(":core:network"))
}
