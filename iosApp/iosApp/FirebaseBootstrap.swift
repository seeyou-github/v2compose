import FirebaseAnalytics
import FirebaseCore
import FirebaseCrashlytics
import Foundation

enum FirebaseBootstrap {
    private static var didConfigure = false

    static func configureIfNeeded() {
        guard !didConfigure else { return }

        guard FirebaseApp.app() == nil else {
            didConfigure = true
            return
        }

        guard let optionsPath = Bundle.main.path(forResource: "GoogleService-Info", ofType: "plist"),
              FirebaseOptions(contentsOfFile: optionsPath) != nil else {
            preconditionFailure(
                "Missing or invalid GoogleService-Info.plist in the app bundle. Put iosApp/iosApp/GoogleService-Info-Debug.plist for Debug or iosApp/iosApp/GoogleService-Info.plist for Release before launching the iOS app."
            )
        }

        FirebaseApp.configure()
        Crashlytics.crashlytics().setCustomValue(
            Bundle.main.bundleIdentifier ?? "unknown",
            forKey: "bundle_id"
        )

        #if DEBUG
        Analytics.setAnalyticsCollectionEnabled(false)
        #else
        Analytics.setAnalyticsCollectionEnabled(true)
        #endif

        didConfigure = true
    }

    #if DEBUG
    static func triggerTestCrash() -> Never {
        fatalError("Firebase Crashlytics test crash")
    }
    #endif
}
