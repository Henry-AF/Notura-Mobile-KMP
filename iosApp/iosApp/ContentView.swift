import SwiftUI
import ComposeApp

// Minimal shell: hosts the shared Compose UI exported by :composeApp.
struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController(
            apiBaseUrl: Self.setting("NoturaApiBaseUrl"),
            supabaseUrl: Self.setting("SupabaseUrl"),
            supabaseAnonKey: Self.setting("SupabaseAnonKey")
        )
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}

    /// Values come from Info.plist, filled from an xcconfig kept out of git (Secrets.xcconfig).
    private static func setting(_ key: String) -> String {
        Bundle.main.object(forInfoDictionaryKey: key) as? String ?? ""
    }
}

struct ContentView: View {
    var body: some View {
        ComposeView().ignoresSafeArea()
    }
}
