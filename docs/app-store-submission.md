# V2X App Store Submission Notes

## App Information

- App name: V2X
- Bundle ID: `io.github.v2compose.iosApp`
- Version: `1.0.1`
- Build: `2`
- Device support: iPhone only
- Orientation: Portrait
- Category: Social Networking
- Primary language: Chinese Simplified
- SKU: `v2compose-ios`

## Store Copy

Subtitle:

```text
A clean V2EX client
```

Keywords:

```text
V2EX,forum,community,client,Markdown,check-in
```

Chinese description:

```text
V2X 是一个界面简洁、交互流畅的 V2EX 客户端。

特点：
* Material You 风格界面
* 支持浏览主题、节点、用户信息与通知
* 支持登录、发帖、回帖与收藏等日常操作
* 编辑器支持 Markdown
* 支持自动签到与自定义代理服务器
```

English description:

```text
V2X is a clean and smooth V2EX client.

Features:
* Material You style interface
* Browse topics, nodes, user profiles, and notifications
* Sign in, create topics, reply, and manage favorites
* Markdown editor support
* Automatic check-in and custom proxy support
```

Version release notes:

```text
Initial iOS release for App Store review.
```

## Public URLs

Use the GitHub Pages URLs generated from these repository documents:

- Privacy Policy: `https://cooaer.github.io/v2compose/privacy-policy`
- Support URL: `https://cooaer.github.io/v2compose/support`
- Terms of Service: `https://cooaer.github.io/v2compose/terms`

## App Privacy Draft

Data not used for tracking:

- User ID, linked to the user, used for app functionality.
- Other user content, linked to the user, used for app functionality.
- Search history, not linked to the user by V2X, used for app functionality.
- (Removed) Firebase Analytics / Crashlytics data collection (Firebase SDK removed).

Tracking:

- Do not mark tracking.
- Do not declare tracking domains.
- Do not request App Tracking Transparency permission.

Permissions:

- Photos Add Only: used only to save selected images to the system photo library.
- Notifications: used for app reminders and automatic check-in related flows when enabled.
- Background Fetch / BGTaskScheduler: used to support automatic check-in scheduling.

## Review Notes

```text
V2X is an independent V2EX client.

Automatic check-in:
The app can schedule V2EX daily check-in after the user signs in and explicitly enables automatic check-in in Settings.

Background refresh:
Background refresh is used only to support the automatic check-in schedule. It does not run advertising, tracking, or unrelated background collection.

Notifications:
Notifications are used for app reminders and automatic check-in related status when the user grants permission.

Photo library:
The app requests add-only photo access only when the user saves an image from V2X to the system photo library.

Firebase:
Firebase Analytics and Crashlytics are not used (Firebase SDK removed).

Test account:
If review requires login, provide a dedicated V2EX test account in App Store Connect review notes. Do not commit credentials to the repository.
```

## Local Release Checks

- (Removed) Firebase `GoogleService-Info.plist` release checks (Firebase SDK removed).
- Generate an Xcode privacy report from the Release archive and compare it with the App Privacy draft above.

