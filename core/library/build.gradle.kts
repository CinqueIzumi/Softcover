plugins {
    id("softcover.android.library")
}

android {
    namespace = "nl.rhaydus.softcover.core.library"
}

dependencies {
    api(project(":core:domain"))
    api(project(":core:book"))
    api(project(":core:lists"))
    api(project(":core:preferences"))
    api(project(":core:identity"))
}
