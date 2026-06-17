import SwiftUI
import OrchestrationKit

/// Bridges the Compose Multiplatform UI (a UIViewController produced by the shared Kotlin code) into
/// SwiftUI. `MainViewController()` lives in the Kotlin file MainViewController.kt and returns a
/// `ComposeUIViewController { App() }`.
struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

struct ContentView: View {
    var body: some View {
        ComposeView()
            .ignoresSafeArea(.all) // Compose draws edge-to-edge and owns its own insets.
    }
}
