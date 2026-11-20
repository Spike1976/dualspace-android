# Device testing checklist

**Do not test on a phone with an existing employer-managed profile.** Never remove a profile without verifying its data has been backed up.

## Setup
1. Install debug APK on a spare Android 12+ phone.
2. Confirm this device supports managed profiles and has no existing managed work profile.
3. Launch DualSpace, tap Create Work Profile, and approve Android setup.
4. Wait until Android shows a Work tab and a work-badged DualSpace app.

## Verification
- Work profile owner enabled successfully (check device policy information).
- Personal and Work app sessions stay distinct.
- Try a harmless test package. DevicePolicyManager.installExistingPackage may fail on unaffiliated consumer-managed profiles. Treat failure as **expected unsupported** until the exact device is proven compatible.
- Verify LauncherApps can list and launch accessible profile apps.
- Restart the device and ensure both apps retain data.
- Pause work profile and verify that launching Work apps behaves predictably.
- Never manually delete an existing unrelated managed profile.

## CI
GitHub Actions workflow 'Android Debug Build' assembles the debug APK and uploads the artifact on success. A successful compile is **not** proof that provisioning or duplication works on-device.
