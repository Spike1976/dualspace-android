# DualSpace security audit (source review, November 2026)

## Security requirement
DualSpace must **never see or capture** WEX card numbers, PINs, passwords, authorization codes, balances, location histories or transactions. Only the **official WEX CardControl app** is authorized to handle those data.

## Source-level protections
- Only exact package `com.efsllc.efsmobileapp` is queried via LauncherApps; unrelated installed apps are not enumerated.
- Android's two separate user profiles own the two WEX instances, with app-data isolation enforced by the OS.
- The app only invokes Android APIs to provision a managed profile, identify an app's launch activity, and launch that activity.
- No use of AccessibilityService, MediaProjection, input method APIs, VPN APIs, SMS, contacts, camera, screen-reading, or clipboard interception.
- No network permission; network and broad package-inventory permissions are explicitly removed from the manifest at merge time.
- No WEX credentials, PINs, card identifiers, tokens, payment values, or personal data are stored in DualSpace.
- Android backup and device-to-device extraction are disabled for DualSpace's local data.
- DualSpace screen uses FLAG_SECURE to resist ordinary screenshots and screen sharing **inside DualSpace only**, not inside WEX.
- Provisioning components are protected with `android.permission.BIND_DEVICE_ADMIN`.
- Main activity remains exported solely as the standard OS launcher entrypoint.
- Generic user-facing errors are used, rather than showing exception messages that may contain profile metadata.
- CI tests, lint, build, and a permission audit fail if the built APK requests any Android runtime or install-time permissions.

## Limits that cannot be solved by this app
- Android's OS, any company that administers a different work profile, the WEX app itself, and WEX's servers remain separate trust boundaries.
- As a Work profile owner, DualSpace has Android management powers and can generally obtain profile/app metadata. Its **current implementation** does not inspect WEX private data, but this does not mathematically prove future versions cannot.
- WEX controls whether it allows sign-in, cardless fueling, and work-profile use. There is no guarantee WEX will support two sessions on this device.
- Android apps with accessibility or device-administrator permissions, malware, a compromised OS, rooting, or malicious input overlays may defeat privacy protections.
- FLAG_SECURE protects only DualSpace's own window, **not** the launched WEX window. User should rely on WEX's security for WEX screens.
- No third-party audit, penetration test, device provisioning test, or WEX integration validation has been completed.
- Device app-store links open outside DualSpace; WEX's separate permissions and network access are controlled by its own manifest, not DualSpace.

## Pre-release test protocol
1. On a spare device, install release-signed DualSpace and provision the work profile.
2. Build with CI and inspect APK's merged permissions. Any permission request fails the release gate.
3. Confirm both WEX logins have separate data across reboot and work-profile pausing.
4. Inspect logcat while using both apps; verify no account details are printed by DualSpace.
5. Run device security, overlays, accessibility and screenshot tests, and check Android screen-recorder behavior.
6. Run static analysis and independent security review before calling this production-secure.
7. Document data safety and DPC powers accurately to users; do not imply an absolute guarantee.

## Conclusion
This source change **reduces potential data collection** and makes accidental WEX-data access less plausible. It cannot prove *zero risk*. The most important guarantee is Android's sandbox separation, not a claim that the DPC is omnipotently private.
