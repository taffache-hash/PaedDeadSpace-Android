# Release manifest

## Candidate identity

- Product: PaedDeadSpace Android
- Android package: `org.paeddeadspace.android`
- Current app version: `1.0.0` (`versionCode` 1)
- Embedded scientific package: PaedDeadSpace-Core `1.0.0`
- Core tag: `v1.0.0`
- Core release DOI: <https://doi.org/10.5281/zenodo.22838225>
- Android release status: GitHub source release v1.0.0; Android DOI not yet assigned
- Release AAB status: unsigned; not authorized for production upload

## Files included in the SHA-256 release manifest

The block below is machine-read by `scripts/write_manifest_sha256.py`. Local caches, build intermediates other than the selected AAB, keystores, passwords, tokens, and Play credentials are excluded.

<!-- sha256-files:start -->
- `app/build/outputs/bundle/release/app-release.aab`
- `app/src/main/assets/core_release.json`
- `CHANGELOG.md`
- `CITATION.cff`
- `docs/APP_INTENDED_USE.md`
- `docs/PLAY_STORE_DRAFT.md`
- `docs/RELEASE_CHECKLIST.md`
- `LICENSE`
- `PRIVACY_POLICY.md`
- `provenance/core-v1.0.0/MANIFEST_SHA256.txt`
- `README.md`
- `RELEASE_MANIFEST.md`
<!-- sha256-files:end -->

## Required release evidence

The GitHub release must record the final commit, tag, Android and Core versions, build environment, full verification results, AAB checksum, and explicit statement that the attached AAB is unsigned unless a separately controlled signed artifact is approved. The Zenodo deposit must archive the exact GitHub release and report matching identifiers and checksums.
