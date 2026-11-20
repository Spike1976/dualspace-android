# WEX CardControl: two fleet-card accounts

Primary real-world acceptance case: one authorized WEX CardControl account for Love's purchases and another for Pilot purchases on one Android device.

## Verified application identity
- WEX CardControl (EFS / Fleet One): Android package `com.efsllc.efsmobileapp`
- Google Play: https://play.google.com/store/apps/details?id=com.efsllc.efsmobileapp
- Not interchangeable with WEX Connect (`com.wex.octane`) or Fleet SmartHub (`com.wex.fleetsmarthub`).

## Required user workflow
1. Install WEX CardControl in the Personal profile and log in to the account used for Love's.
2. On a **spare supported device** with no existing managed profile, provision a Work profile from DualSpace and finish Android's consent screens.
3. Open **DualSpace (Work)** and select **Install WEX CardControl in Work**. This requests a supported OS package installation, not an APK clone or credentials copy.
4. If Android declines the install, record the failure, and install from an available, authorized Work Play Store instead. This may be unavailable on independently provisioned profiles.
5. Log in to the second authorized WEX account used for Pilot in the Work WEX instance.
6. In the Personal DualSpace app, use **Open Love's (Personal)** and **Open Pilot (Work)**.
7. Verify both sessions are independent across reboot, push notifications, location checks, and cardless fueling (when supported by the user's account).

**Important:** Love's and Pilot are **user-chosen names**, not restrictions enforced by DualSpace. The underlying WEX app is responsible for card/account selection and fueling transactions. Do not store or collect card numbers, PINs, passwords, or authentication tokens in DualSpace.

## Compatibility risks
- WEX may block work-profile login, device-binding behavior, or cardless fueling.
- Android's LauncherApps discovery may differ across manufacturer builds.
- DevicePolicyManager.installExistingPackage may not work for this DPC in an unaffiliated consumer work profile.
- Work profile setup and installation need physical device testing. The GitHub debug compilation does not establish WEX compatibility.
