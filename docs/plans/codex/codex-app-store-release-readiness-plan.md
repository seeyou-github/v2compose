# App Store Release Readiness Implementation Plan

**Goal:** Prepare the V2compose iOS app for a first App Store submission with version `1.0.1` build `2`.

**Architecture:** Keep the app implementation unchanged and focus on release configuration, privacy compliance assets, App Store submission material, and verification. XcodeGen remains the source for generated iOS project structure, while `Base.xcconfig` remains the source for marketing and build versions.

**Tech Stack:** Kotlin Multiplatform, Compose Multiplatform, XcodeGen, iOS `Info.plist`, Apple privacy manifest, Firebase Analytics, Firebase Crashlytics.

---

## Tasks

- [x] Unify iOS versioning by resolving `Info.plist` values through `$(MARKETING_VERSION)` and `$(CURRENT_PROJECT_VERSION)`, with `CURRENT_PROJECT_VERSION = 2`.
- [x] Set the first App Store release to iPhone-only by applying `TARGETED_DEVICE_FAMILY = 1` through `iosApp/project.yml` and regenerating the Xcode project.
- [x] Add an app-level `PrivacyInfo.xcprivacy` resource to the iOS target with tracking disabled, app functionality data categories declared, and file timestamp required-reason API usage declared.
- [x] Add GitHub Pages-ready Privacy Policy, Support, and Terms documents under `docs/`.
- [x] Add App Store Connect submission notes covering store copy, public URLs, App Privacy draft, Review Notes, and release checks.
- [x] Update README privacy wording so it no longer claims the app collects no user privacy-related data.
- [x] Update the App Store checklist to reflect the implemented repository-side items and the remaining manual App Store Connect / Archive work.

## Verification

- Run `xcodegen generate --spec iosApp/project.yml` and confirm `PrivacyInfo.xcprivacy` is in the Resources phase.
- Run Android, shared, iOS compile, and Release framework embedding checks.
- Use Xcode to create a Release Archive, validate, upload, inspect processing, and generate the privacy report.

