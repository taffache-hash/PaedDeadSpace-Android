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

## Zenodo archive and citation

The Zenodo GitHub integration lists the Android repository's archived-release DOI as [10.5281/zenodo.22850342](https://doi.org/10.5281/zenodo.22850342). The manuscript uses this identifier for Android v1.0.1; the corresponding all-versions (concept) DOI is [10.5281/zenodo.22850341](https://doi.org/10.5281/zenodo.22850341). These identifiers follow the current integration badge and the version-specific citation shown in `CITATION.cff`.

**Release-note erratum (documentation only):** the explanatory text of the existing GitHub v1.0.1 release reverses the labels “version DOI” and “concept DOI”. This README records the corrected labels without changing the tagged release, associated artifacts, scientific Core or published app. The Zenodo record itself should remain the authoritative metadata source. DOI registration or public availability is not evidence of clinical validation or medical-device certification.
