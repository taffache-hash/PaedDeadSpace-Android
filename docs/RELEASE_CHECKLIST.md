# Android release checklist

This checklist is a human-controlled gate. Completing a build does not authorize publication, signing, account access, or regulatory claims.

## 1. Scientific and functional verification

- [ ] Confirm the embedded Core reports version `1.0.0` and all four source files match the published Core v1.0.0 manifest byte for byte.
- [ ] Archive the passing host bridge, Kotlin unit, Android instrumented, lint, and Core-equivalence test results.
- [ ] Confirm all accepted patient dead-space models, source-domain warnings, invalid inputs, zero apparatus dead space, and `VD >= VT` model-boundary behavior are covered.
- [ ] Perform a manual airplane-mode/offline run from first launch through results and New scenario.
- [ ] Confirm warnings precede quantitative results and model-breakdown screens suppress relative CO2 burden and required respiratory rate.
- [ ] Confirm New scenario clears all in-memory values and no case remains after process termination.

## 2. Privacy, security, and package review

- [ ] Inspect the final release AAB manifest: no Internet, location, camera, microphone, Bluetooth, storage, health-data, advertising, or other sensitive permission.
- [ ] Record any AndroidX app-scoped signature permission separately; do not describe it as a user-granted runtime permission.
- [ ] Confirm cloud backup and device transfer exclude all application data domains.
- [ ] Confirm the release contains no keystore, signing password, Google credential, Zenodo token, local cache, patient data, analytics SDK, advertising SDK, or telemetry SDK.
- [ ] Confirm supported ABIs, minimum SDK 24, compile SDK 37, and target SDK 36 against then-current Google Play requirements.

## 3. Version and provenance

- [ ] Replace the development version with the approved Android release version and monotonically increasing version code.
- [ ] Confirm `core_release.json`, in-app Details, README, CITATION, changelog, and release notes identify the same Core v1.0.0 release and DOI.
- [ ] Build the final unsigned AAB from a clean commit and regenerate `MANIFEST_SHA256.txt`.
- [ ] Verify the checksum manifest with `python scripts/write_manifest_sha256.py --check`.
- [ ] Record the final commit SHA, build environment, build date, AAB SHA-256, and test evidence in the GitHub release.

## 4. GitHub and Zenodo — in this order

- [ ] Merge the reviewed Android branch into the publication branch.
- [ ] Create the approved immutable Git tag and GitHub release; attach the selected release files and checksums.
- [ ] Archive that exact GitHub release in Zenodo.
- [ ] Verify the Zenodo record, authorship, title, version, license, related Core identifiers, and deposited file checksums.
- [ ] Record the Android concept DOI and version DOI in CITATION, README, release notes, and in-app provenance before any later production build.

## 5. Manuscript — only after Android DOI exists

- [ ] Update a new controlled manuscript version, preserving the current scientific wording outside the approved Android additions.
- [ ] Cite the frozen Android GitHub release and Zenodo DOI.
- [ ] State that Android is offline educational/research software for healthcare professionals and not patient-specific clinical decision support.
- [ ] Run full citation/reference and visual/structural DOCX quality checks.

## 6. Google Play preparation

- [ ] Publish the final privacy policy at an active, public, non-geofenced, non-editable HTTPS URL; do not use a PDF.
- [ ] Add privacy policy text or its final link in the released app and verify it matches the Play Console policy and Data safety answers.
- [ ] Complete the Data safety section for the exact AAB; expected v1 answer is no data collected and no data shared, subject to final review.
- [ ] Complete the Health apps declaration accurately; expected feature is **Medical Reference and Education**, not “no health features.”
- [ ] Reconfirm that the app is not regulated as a medical device under the applicable intended purpose and jurisdictions; obtain specialist advice if the final feature set or claims change.
- [ ] Complete content rating, target audience, app access, ads, and government-app declarations.
- [ ] Prepare Play-compliant launcher icon, feature graphic, phone screenshots, and listing translations that show the actual app.
- [ ] Recheck title (30 characters), short description (80), and full description (4,000) against the then-current Play limits.

## 7. Testing and production control

- [ ] Configure Play App Signing and protect the upload key outside the repository.
- [ ] Upload the final signed bundle to internal testing first.
- [ ] Install the Play-delivered build and repeat offline, rotation, small-screen, large-font, and process-restart checks on supported devices.
- [ ] Review Play pre-launch, policy, accessibility, security, and device-compatibility reports; resolve every material finding.
- [ ] Complete any required closed-testing period and tester threshold shown in the account.
- [ ] Obtain final human approval of the exact AAB, listing, declarations, public privacy page, GitHub/Zenodo records, and updated manuscript.
- [ ] Promote to production only after every applicable item above is complete.
