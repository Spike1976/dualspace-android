# DualSpace MVP 0.1.0

A **source-only, unverified** Android Studio proof of concept for two independent copies of supported apps using Android's official managed work-profile isolation. Kotlin, Jetpack Compose, Material 3. No root, no APK patching, no cloud accounts.

## What works in source
- Personal UI launches Android work-profile provisioning consent flow (`ACTION_PROVISION_MANAGED_PROFILE`).
- Device-admin receiver and Android 12+ provisioning-mode/policy-compliance activity entry points.
- Work-profile-side DPC screen attempts `DevicePolicyManager.installExistingPackage()` for a user-entered package ID.
- Catalog displays launchable apps from profiles visible through `LauncherApps`; user can attempt to launch each profile's installation.
- No password interception or profile removal behavior.

## Build
1. Install Android Studio, Android SDK Platform 35, JDK 17 and Gradle 8.11.1 (or use Studio's configured Gradle distribution). **This ZIP intentionally does not include a Gradle wrapper JAR or compiled APK**.
2. Open `DualSpace-MVP` as an Android Studio project and sync Gradle.
3. Run `gradle :app:assembleDebug` using Gradle 8.11.1, or configure a verified Gradle wrapper using `gradle wrapper --gradle-version 8.11.1`.
4. Install on a *spare* Android 12+ phone with work-profile support and no existing corporate work profile.

## Manual test procedure
1. Open personal DualSpace, tap **Create Work Profile**, follow Android prompts.
2. Locate DualSpace icon with the **briefcase badge** (work profile).
3. On personal Dashboard, find a simple third-party test app and copy its package ID.
4. Launch work-side DualSpace and enter that package ID. Tap **Enable Second Copy**.
5. Refresh personal-side DualSpace; both launchable copies should appear if profile discovery is permitted.
6. Sign into each app separately and verify independent data after device restart.

## Important limitations / known gaps
- This is not a tested build. No Android SDK or Gradle distribution was available in the authoring environment, so compilation and provisioning on actual devices were **not** verified.
- LauncherApps profile access depends on Android and launcher/profile visibility; cannot guarantee cross-profile enumeration or launching for every device.
- `installExistingPackage` needs a profile-owner context, and may fail for packages not installed/available on the device or restricted by Android.
- Profile provisioning might not succeed on an existing employer-managed, organization-owned, or otherwise restricted device.
- No automatic passing of the app package ID between profiles (work profile has a manual package ID field). Cross-profile IPC is a planned enhancement requiring explicit Android policy support.
- No managed Google Play provisioning in the MVP; this is not a generic app-store installer.
- No notification mirroring, profile deletion, backup or purchase logic.
- The profile owner must not claim management permissions it does not need. Production release requires additional compatibility, security, privacy and Play-policy testing.
- Android's managed profile is not an evasion layer for apps that prohibit duplicate logins.

## Next engineering tasks
- Compile and resolve SDK/API mismatches on a supported Android Studio build environment.
- Validate provisioning on an actual Samsung device and AOSP/Pixel emulator.
- Add robust provisioning callbacks and an installed-work-app status panel.
- Replace manual package-ID entry with a user-approved and policy-compliant cross-profile handoff if feasible.
- Add instrumentation tests and Play compliance review.

## Reference documentation
- https://developer.android.com/reference/android/app/admin/DevicePolicyManager
- https://developer.android.com/reference/android/content/pm/LauncherApps
- https://source.android.com/docs/devices/admin/provision
