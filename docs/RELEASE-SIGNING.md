# Production signing and upgrade compatibility

## Safety warning for device-policy apps

The Android app signature is part of the application's identity. A managed-profile device policy controller (DPC) should use its **permanent release signing key before provisioning real user profiles**. A developer debug build and a production build signed by a different key cannot be upgraded in place. Replacing the DPC may require removing the associated Work profile, which deletes its locally stored apps and data.

**Never test work-profile creation on a primary phone with important data using a throwaway signing key.**

## Required GitHub Actions secrets

Configure these under repository Settings -> Secrets and variables -> Actions, or in the protected `production` environment:

- `DUALSPACE_KEYSTORE_BASE64`: complete production .jks keystore bytes encoded as a single-line base64 string
- `DUALSPACE_KEYSTORE_PASSWORD`: keystore password
- `DUALSPACE_KEY_ALIAS`: production key alias
- `DUALSPACE_KEY_PASSWORD`: key password

Never commit the keystore or any of these values to Git. Do not paste them into this chat.

## Build

After physical provisioning tests and security review have passed, open Actions -> **Signed Android Release** -> **Run workflow**.

The job requires all four secrets, runs unit tests and lint, builds release APK and AAB with R8 shrinking, and uploads signed artifacts as a GitHub Actions artifact. No automatic publishing to Google Play is configured.

## Verification before distribution

- Confirm the final APK/AAB is signed with the permanent key.
- Check the release package name `com.dualspace.mvp` and versionCode increment.
- Verify installed release upgrades with the **same signing certificate**.
- Test a clean install and a safe upgrade on non-production hardware.
- Review Google Play device-administration policy and Android 16 target requirements.
- Store a secure backup of your original signing keystore. Loss of the key can prevent future updates without a supported signing-key migration process.

## For local development

Build debug artifacts through Android Debug Build. Debug APKs are for testing only and are signed with ephemeral development keys. Do not distribute them as production DPC releases.
