# DualSpace privacy statement (developer preview)

DualSpace is a locally operating Android work-profile launcher. The current source has no analytics SDK, no remote account system, no backend service, no advertising SDK, and does not request internet access.

- It reads the list of launchable applications and accessible Android profiles using the operating system's LauncherApps API in order to display available app copies.
- It asks Android to create a managed work profile only after showing a consent explanation.
- It launches third-party apps through Android's launcher service. It does not access their private storage.
- It does not collect or transmit card numbers, WEX login credentials, fuel-card PINs, payment transactions, or location data.
- The user signs into WEX CardControl directly inside WEX's app; DualSpace does not intercept or handle that authentication.
- Removing an Android work profile permanently deletes the apps and local data in that profile. DualSpace does not currently expose a delete-profile operation.

This source is a **developer preview**, not yet a Play Store-published privacy policy for a validated final release. Review this statement and the final binary before publication.
