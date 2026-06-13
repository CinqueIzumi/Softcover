import SwiftUI
import OrchestrationKit

@main
struct iOSApp: App {
    init() {
        // Start Koin once, before the first Compose view is created. The shared App() composable
        // resolves everything it needs from Koin, so this must run first. Swift name comes from the
        // Kotlin file InitKoinIos.kt; `initKoinIos()` is exposed as `doInitKoinIos()` because Kotlin/
        // Native prefixes `init`-named functions with `do` to avoid clashing with Swift initializers.
        InitKoinIosKt.doInitKoinIos()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
                .ignoresSafeArea(.all)
                .ignoresSafeArea(.keyboard) // Compose handles IME insets itself.
        }
    }
}
