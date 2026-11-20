# DualSpace privacy policy (source implementation, November 2026)

**DualSpace is a local Android launcher / Work-profile controller, not a WEX account-management app.** The app deliberately does **not** read, send, save, or display WEX card numbers, PINs, account credentials, authentication codes, balances, fuel transactions or purchase history.

## Information the app can see
- Whether the official WEX CardControl Android package `com.efsllc.efsmobileapp` has a launchable activity in Android profiles accessible to the user.
- Whether Android reports an accessible second profile and whether DualSpace is its Work-profile owner.
- Errors indicating that Android denied provisioning, launch, or installation.

The app displays the account labels **Love's** and **Pilot** configured in its source. These are not card identification data. No card or account identifiers are requested.

## Information the app does not access
- WEX login form fields, private application storage, cookies, tokens, PINs, balances, card details, or transactions.
- Other installed applications' lists or storage (the optional general app catalog was removed).
- Device location, camera, contacts, microphone, SMS, notifications, clipboard, or files.

## Network and data retention
- No internet access permission, network SDK, analytics, advertising, cloud account, or app-level backend.
- Android manifest merge explicitly removes `INTERNET`, `ACCESS_NETWORK_STATE`, and `QUERY_ALL_PACKAGES` permissions.
- Android backup, data extraction/transfer, and cleartext traffic are disabled for DualSpace.
- The DualSpace window uses Android `FLAG_SECURE` to discourage ordinary screen capture of its own UI; this does **not** protect screens displayed by WEX.
- No account details are stored in DualSpace databases or logs.

## Device administration and third-party boundaries
To create the second isolated profile, DualSpace acts as a **work-profile Device Policy Controller** and receives management authority in that profile. That authority carries security responsibility and should be granted only to software you trust. DualSpace currently uses it only for required provisioning/profile activation and the optional affiliated-device installation route.

Android, the official WEX app, WEX's service, device manufacturers and any organization administering another profile operate under **separate privacy and security policies**. DualSpace cannot guarantee those parties have no access to card details.

Removing a managed Work profile deletes its locally stored apps and data. DualSpace does not offer profile removal.

**Audit status:** Source reviewed and hardened. Automated permission/build tests are enforced; independent penetration testing and actual WEX dual-login verification have not yet been completed. Do not describe the application as guaranteed 100% secure.
