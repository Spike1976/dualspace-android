# DualSpace Android 0.2.0

**Android-only dual-account launcher and work-profile controller** written in Kotlin and Jetpack Compose. Personal and Work instances of compatible Android apps can maintain separate sign-ins because Android isolates each profile's data.

**Current status: developer release candidate, NOT production-certified.** GitHub Actions checks compilation, tests, Android lint, and optimized release assembly. Profile provisioning, second-copy installation and WEX fuel-card operations still require physical device testing.

## Priority use case: two WEX CardControl accounts

- **Love's** opens WEX CardControl in the Personal profile.
- **Pilot** opens WEX CardControl in the Work profile.
- Official WEX Android package: `com.efsllc.efsmobileapp`.
- These are dashboard labels only; the WEX app itself controls card selection, card balances, authorization and fueling. DualSpace does **not** hold card numbers, PINs, passwords or tokens.

See [WEX testing instructions](docs/WEX-CARDCONTROL.md).

## Feature scope

- Android 12+ and Android 16 API 36 target.
- Managed profile consent and Device Policy Controller provisioning components.
- Detect launchable app copies via Android `LauncherApps`; no package-inventory permission.
- Launch Personal and Work WEX copies from the Personal dashboard when Android grants cross-profile access.
- Work instance provides a store installation route where a supported managed Play Store is available.
- Affiliated device-owner deployments can optionally try `installExistingPackage`; consumer managed profiles typically cannot.
- Automatic app-list refresh when returning to the dashboard.
- No root, modified APKs, cloud, analytics, ad SDK, location permission, or password interception.
- Profile-removal is deliberately **not** implemented to avoid accidental data loss.

## Build from source

- Android Studio with Android 16 / API 36 SDK installed
- JDK 17 and Gradle 8.11.1
- Android Gradle Plugin 8.9.2; Kotlin 2.0.21

Run from project root:

```sh
gradle :app:testDebugUnitTest :app:lintDebug :app:assembleDebug :app:assembleRelease
```

The unsigned optimized release APK is **not** a Play Store deliverable without a protected signing process. Debug APK artifacts are available under GitHub Actions after successful workflow runs.

## Security and compatibility

1. Never install on or remove an existing employer-managed Work profile for testing.
2. The consumer Work profile is not an affiliated managed device; the silent `installExistingPackage()` route may be denied by Android.
3. A Work Play Store is not guaranteed without managed Google Play provisioning. If neither supported store nor authorized installation route is available, DualSpace cannot install the second WEX copy.
4. WEX may enforce device/profile restrictions independent of Android. A successful APK build does not prove WEX app login, card authorization or fueling works.
5. Removing the managed profile permanently deletes its local user and app data. Back up any work data before removal using Android-approved methods.
6. The app does not perform card transactions or connect to WEX APIs directly.

Read [privacy statement](PRIVACY.md), [QA protocol](docs/TESTING.md), and [release gates](docs/RELEASE-CHECKLIST.md).

## Roadmap to a supported production release

- Complete Samsung and Pixel provisioning tests with documented Android versions.
- Verify WEX CardControl install and two authorized logins in real profiles.
- Complete Play Console policy review for DPC device-management behavior.
- Provide an approved, supportable installation method where managed Play is unavailable.
- Perform accessibility and security review.
- Create a signing keystore securely **outside GitHub source** and publish a signed AAB after passing all gates.

## References

- https://developer.android.com/reference/android/app/admin/DevicePolicyManager
- https://developer.android.com/reference/android/content/pm/LauncherApps
- https://developer.android.com/work/dpc/build-dpc
