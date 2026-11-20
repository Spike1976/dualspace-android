# Release gate (do not label production without these)

## Automated gates
- [ ] GitHub Actions debug APK assembles successfully.
- [ ] Unit tests pass.
- [ ] Android lint passes, or every warning is reviewed and documented.
- [ ] Full manifest permissions review, Android policy review and privacy statement verification.
- [ ] Release signing key created and stored securely outside the repository.
- [ ] Signed release AAB/APK generated via secure GitHub environment or offline signing.
- [ ] Version code incremented; release tagged; dependency revisions audited.

## Hands-on gates (required)
- [ ] New work profile provisions on spare Samsung hardware.
- [ ] New work profile provisions on Pixel/AOSP emulator or spare Pixel.
- [ ] Existing enterprise-managed profile is never replaced or deleted.
- [ ] WEX CardControl installs in Personal and in Work through an authorized store.
- [ ] Love's and Pilot sign-ins work independently, including after reboot.
- [ ] Cards are never sent to or persisted by DualSpace.
- [ ] WEX's own rules allow the intended use.
- [ ] Cardless fueling and location/notification behavior verified separately where applicable.
- [ ] Offline, no Play Store, paused Work profile, denied provisioning, and missing WEX app paths tested.
- [ ] App accessibility tested with TalkBack and large display font.
- [ ] No crash in 48-hour exploratory testing on each supported Android version.

No successful CI build can substitute for provisioning and account testing on real devices.
