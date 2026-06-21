import SwiftUI
import UIKit
import shared

final class AppDelegate: NSObject, UIApplicationDelegate {
    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey : Any]? = nil
    ) -> Bool {
        IosBackgroundTasksKt.registerIosBackgroundTasks()
        return true
    }

    func applicationDidEnterBackground(_ application: UIApplication) {
        IosBackgroundTasksKt.syncIosAutoCheckInScheduleFromStoredSettings()
    }
}

@main
struct iOSApp: App {
    @UIApplicationDelegateAdaptor(AppDelegate.self) private var appDelegate

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
