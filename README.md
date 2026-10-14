# PaedDeadSpace Android

**Available on Google Play:** [Download PaedDeadSpace for Android](https://play.google.com/store/apps/details?id=org.paeddeadspace.android).

PaedDeadSpace Android is an offline educational and research application for healthcare professionals. It supports reproducible exploration of predefined apparatus dead-space scenarios using the frozen `PaedDeadSpace-Core v1.0.0` scientific package.

The app does not diagnose or treat any condition, recommend ventilator settings, or provide patient-specific clinical decision support. Its outputs describe a mathematical model and are not clinical recommendations. Apparatus dead space is represented separately from patient dead space.

## Privacy and offline operation

Version 1 has no accounts, advertising, analytics, telemetry, network communication, case storage, or export. It requests no runtime permissions. Users must not enter names, record numbers, or other patient-identifying information.

See [PRIVACY_POLICY.md](PRIVACY_POLICY.md) and [docs/APP_INTENDED_USE.md](docs/APP_INTENDED_USE.md) for the complete boundaries.

## Scientific authority

The app invokes the bundled, byte-verified `PaedDeadSpace-Core v1.0.0`; Kotlin and Compose do not reproduce its physiological equations. Core provenance is pinned in `app/src/main/assets/core_release.json`:

- repository: <https://github.com/taffache-hash/PaedDeadSpace-Core>
- release DOI: <https://doi.org/10.5281/zenodo.22838225>
- concept DOI: <https://doi.org/10.5281/zenodo.22838224>

## Release status

PaedDeadSpace Android is available on Google Play (publication status updated 14 October 2026). [Open the Google Play listing](https://play.google.com/store/apps/details?id=org.paeddeadspace.android).

Version 1.0.1 is the reviewed Android source release. The checked-in Android source currently declares version `1.0.1` (`versionCode` 2) and uses the frozen PaedDeadSpace-Core `v1.0.0`. This repository documents source code and release-preparation material; debug builds and unsigned bundles are not the signed version delivered through Google Play. This documentation update does not alter any app code, scientific calculations, or intended-use boundaries.
