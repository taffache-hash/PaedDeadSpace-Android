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

Version 1.0.1 is the reviewed Android source release. It is the tagged [Android source release](https://github.com/taffache-hash/PaedDeadSpace-Android/releases/tag/v1.0.1). The checked-in Android source declares version `1.0.1` (`versionCode` 2) and embeds the frozen PaedDeadSpace-Core `v1.0.0`. The unsigned GitHub Android App Bundle is **not** the signed production artifact distributed by Google Play; repository tags and source releases remain unchanged by this documentation update.

The Android archive has manuscript-listed Zenodo identifiers, but the version-versus-concept DOI labels disagree with the text of the GitHub v1.0.1 release notes. Please consult the authoritative Zenodo record before citing a specific Android archive DOI. This statement does not affect the independently cited Core release DOI above, and does not imply clinical validation or medical-device certification.
