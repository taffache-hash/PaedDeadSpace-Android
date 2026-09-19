# PaedDeadSpace Android v1 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build an offline Android educational and research application for healthcare professionals that presents only PaedDeadSpace-Core v1.0.0 calculations and their documented boundaries.

**Architecture:** A Kotlin/Jetpack Compose application owns UI state and rendering. It bundles the unmodified PaedDeadSpace-Core v1.0.0 Python source and invokes a single JSON bridge with Chaquopy; Kotlin never reimplements scientific equations. A typed Android gateway turns bridge responses into explicit success, input-error, and model-boundary UI states.

**Tech Stack:** Kotlin 2.2.10, Android Gradle Plugin 9.2.0, Gradle 9.4.1, JDK 17, Jetpack Compose BOM 2026.09.00, Chaquopy 17.0.0, Python 3.13, Android minSdk 24, compileSdk/targetSdk 37, JUnit, Compose UI tests, Android instrumented tests, GitHub Actions.

**Spec:** `docs/superpowers/specs/2026-09-19-paeddeadspace-android-design.md`

## Global Constraints

- Build a distinct repository named `PaedDeadSpace-Android`; do not change PaedDeadSpace-Core, PaedDeadSpace-UI, PaedDeadSpace-Windows, or the manuscript during implementation.
- Bundle PaedDeadSpace-Core v1.0.0 unchanged as the sole source of equations, provenance, warnings, and model-boundary logic.
- Use Chaquopy in the `app` module only, Python 3.13, and Android minSdk 24.
- The app is offline: declare no Internet or data-access permissions, use no account, telemetry, analytics, persistence, export, database, or cloud service.
- Inputs for weight, age, age unit, tidal volume, respiratory rate, and patient dead-space model begin unselected/blank; no hidden clinical defaults are allowed.
- Preserve Core labels and behavior for benchmark, reference/sensitivity construction, source-domain warning, validation error, and `VD >= VT` model breakdown.
- Do not add disease mode, absolute PaCO2 output, clinical thresholds, traffic-light states, target zones, treatment language, or recommendations.
- Play-facing language must state educational/research scope for healthcare professionals and not imply diagnostic, treatment, or patient-specific clinical decision-support functionality.
- No Google Play production upload occurs until the exact Android GitHub release, Zenodo archive, manuscript update, privacy policy, Health Apps declaration, and final listing review exist.

## Review Focus

- Required numerical fields left blank must block calculation and identify the missing field; Task 5 owns UI validation tests.
- A nonphysical `VD >= VT` scenario must show a model-boundary state and no RR-preservation estimate; Task 4 owns bridge and instrumentation tests.
- A Pearsall benchmark scenario must return the frozen v1.0.0 values, including RR 43.0769/min before display rounding; Task 4 owns equivalence tests.
- An out-of-source-domain Numa/Fletcher scenario must remain calculable when Core permits it and visibly surface the Core warning; Task 4 owns warning propagation tests.
- The packaged release must have no runtime Internet permission, no trackers, and no stored input after New scenario; Tasks 5 and 6 own manifest, navigation, and state-reset tests.

---

## File Structure

The implementation creates a new Git repository with this focused layout:

```text
PaedDeadSpace-Android/
  app/
    src/main/AndroidManifest.xml                         # no-permission application declaration
    src/main/java/org/paeddeadspace/android/
      MainActivity.kt                                    # Activity entrypoint
      core/CalculationContract.kt                        # typed request and response models
      core/ChaquopyCoreGateway.kt                        # Kotlin-to-Python JSON invocation
      core/ScenarioValidator.kt                          # non-scientific completeness checks
      state/PaedDeadSpaceViewModel.kt                    # screen state and one-shot calculation flow
      ui/PaedDeadSpaceApp.kt                             # navigation between notice, scenario, results
      ui/NoticeScreen.kt                                 # educational-use acknowledgement
      ui/ScenarioScreen.kt                               # explicit input form
      ui/ResultsScreen.kt                                # result, warning, and boundary rendering
      ui/DetailsScreen.kt                                # provenance and version content
    src/main/python/
      paeddeadspace/                                    # unmodified Core v1.0.0 package
      android_bridge.py                                  # JSON-only adapter around public Core API
    src/main/assets/core_release.json                    # Core tag, DOI, source hash, package version
    src/main/res/values/strings.xml                      # all user-facing copy
    src/test/...                                         # Kotlin unit tests
    src/androidTest/...                                  # Chaquopy and Compose integration tests
  fixtures/core_v1_0_0_cases.json                        # frozen cross-runtime expected results
  docs/APP_INTENDED_USE.md                               # app scope and forbidden claims
  docs/RELEASE_CHECKLIST.md                              # GitHub, Zenodo, manuscript, Play gates
  docs/PLAY_STORE_DRAFT.md                               # listing copy and disclosure checklist
  PRIVACY_POLICY.md                                      # public policy source; no data collection
  CITATION.cff                                           # Android component citation metadata
  CHANGELOG.md                                           # user-facing release history
  RELEASE_MANIFEST.md                                    # immutable dependency/version linkage
  MANIFEST_SHA256.txt                                    # generated release integrity record
  .github/workflows/android.yml                          # build and test CI
  settings.gradle.kts, build.gradle.kts, gradle/libs.versions.toml
```

## Task 1: Create the reproducible Android shell

**Files:**
- Create: `settings.gradle.kts`
- Create: `build.gradle.kts`
- Create: `gradle/libs.versions.toml`
- Create: `app/build.gradle.kts`
- Create: `app/src/main/AndroidManifest.xml`
- Create: `app/src/main/java/org/paeddeadspace/android/MainActivity.kt`
- Create: `app/src/main/java/org/paeddeadspace/android/ui/PaedDeadSpaceApp.kt`
- Test: `app/src/androidTest/java/org/paeddeadspace/android/AppLaunchTest.kt`

**Interfaces:**
- Consumes: Android SDK API 37, JDK 17, Gradle 9.4.1, Chaquopy 17.0.0.
- Produces: a launchable Compose application with the `PaedDeadSpaceApp()` root composable.

- [ ] **Step 1: Create the Android repository and Gradle wrapper**

Run from the parent directory:

```powershell
git init PaedDeadSpace-Android
cd PaedDeadSpace-Android
gradle wrapper --gradle-version 9.4.1
```

- [ ] **Step 2: Add a failing launch test before adding UI behavior**

```kotlin
@RunWith(AndroidJUnit4::class)
class AppLaunchTest {
    @get:Rule val composeRule = createAndroidComposeRule<MainActivity>()

    @Test fun appShowsEducationalNoticeOnFirstLaunch() {
        composeRule.onNodeWithText("Educational and research use").assertExists()
    }
}
```

- [ ] **Step 3: Run the test to confirm that the missing activity fails**

Run:

```powershell
.\gradlew.bat connectedDebugAndroidTest --tests "*AppLaunchTest"
```

Expected: build failure because `MainActivity` and the app module do not yet exist.

- [ ] **Step 4: Add pinned Gradle configuration and a minimal Compose root**

Configure the root build with Android application plugin `9.2.0`, Kotlin Android plugin `2.2.10`, and `com.chaquo.python` plugin `17.0.0`. Configure `app` with `compileSdk = 37`, `targetSdk = 37`, `minSdk = 24`, Java/Kotlin target 17, Compose BOM `2026.09.00`, and Python version `3.13`.

Use this manifest shape, deliberately with no `<uses-permission>` element:

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <application
        android:allowBackup="false"
        android:label="PaedDeadSpace"
        android:theme="@style/Theme.PaedDeadSpace">
        <activity android:name=".MainActivity" android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>
</manifest>
```

Create the exact public root interface:

```kotlin
@Composable
fun PaedDeadSpaceApp() {
    Text("Educational and research use")
}
```

and call it from `MainActivity` inside the app theme.

- [ ] **Step 5: Run the launch test and static build**

Run:

```powershell
.\gradlew.bat testDebugUnitTest assembleDebug connectedDebugAndroidTest --tests "*AppLaunchTest"
```

Expected: all selected tasks pass and a debug APK is created.

- [ ] **Step 6: Commit the reproducible shell**

```powershell
git add settings.gradle.kts build.gradle.kts gradle app
git commit -m "build: bootstrap offline Android Compose application"
```

## Task 2: Bundle and identify the frozen Core v1.0.0

**Files:**
- Create: `app/src/main/python/paeddeadspace/__init__.py`
- Create: `app/src/main/python/paeddeadspace/core.py`
- Create: `app/src/main/python/paeddeadspace/models.py`
- Create: `app/src/main/python/paeddeadspace/provenance.py`
- Create: `app/src/main/assets/core_release.json`
- Create: `fixtures/core_v1_0_0_cases.json`
- Create: `app/src/test/java/org/paeddeadspace/android/core/CoreReleaseMetadataTest.kt`
- Modify: `app/build.gradle.kts`

**Interfaces:**
- Consumes: the exact source files and `MANIFEST_SHA256.txt` from PaedDeadSpace-Core v1.0.0.
- Produces: a Python source set packaged by Chaquopy and a machine-readable `core_release.json` contract.

- [ ] **Step 1: Write a failing release-metadata unit test**

```kotlin
class CoreReleaseMetadataTest {
    @Test fun releaseMetadataPinsThePublishedCore() {
        val metadata = CoreReleaseMetadata.parse(
            """{"package_version":"1.0.0","git_tag":"v1.0.0","repository":"https://github.com/taffache-hash/PaedDeadSpace-Core","concept_doi":"10.5281/zenodo.22838224","release_doi":"10.5281/zenodo.22838225"}"""
        )
        assertEquals("1.0.0", metadata.packageVersion)
        assertEquals("v1.0.0", metadata.gitTag)
        assertEquals("10.5281/zenodo.22838225", metadata.releaseDoi)
    }
}
```

- [ ] **Step 2: Run the unit test to confirm the loader and metadata are absent**

Run:

```powershell
.\gradlew.bat testDebugUnitTest --tests "*CoreReleaseMetadataTest"
```

Expected: compilation failure because `CoreReleaseMetadata` is not defined.

- [ ] **Step 3: Copy the Core without changing it and record its provenance**

Copy only the four package source files from the verified `PaedDeadSpace-Core v1.0.0` release into `app/src/main/python/paeddeadspace/`. Verify every copied file against the release manifest before staging it.

Create `core_release.json` with this exact data shape:

```json
{
  "package_name": "paeddeadspace",
  "package_version": "1.0.0",
  "git_tag": "v1.0.0",
  "repository": "https://github.com/taffache-hash/PaedDeadSpace-Core",
  "concept_doi": "10.5281/zenodo.22838224",
  "release_doi": "10.5281/zenodo.22838225",
  "source_manifest": "MANIFEST_SHA256.txt"
}
```

Place the same Core release version in `fixtures/core_v1_0_0_cases.json`; this fixture is only expected input/output data and must contain no equations.

- [ ] **Step 4: Implement the non-scientific Kotlin metadata loader**

```kotlin
@Serializable
data class CoreReleaseMetadata(
    val packageVersion: String,
    val gitTag: String,
    val repository: String,
    val conceptDoi: String,
    val releaseDoi: String,
) {
    companion object {
        fun parse(jsonText: String): CoreReleaseMetadata =
            Json { ignoreUnknownKeys = false }.decodeFromString(jsonText)
    }
}
```

Implement `parse` with `kotlinx.serialization.json.Json`, using `@SerialName` to map snake_case JSON keys to Kotlin properties. Store `core_release.json` at `app/src/main/assets/`; the details screen reads it with `context.assets.open("core_release.json")`, passes its text to `parse`, and displays only the parsed metadata.

- [ ] **Step 5: Replace the temporary loader body and run the metadata test**

Run:

```powershell
.\gradlew.bat testDebugUnitTest --tests "*CoreReleaseMetadataTest"
```

Expected: PASS and the test proves that the UI-visible metadata pins Core v1.0.0.

- [ ] **Step 6: Commit the immutable Core bundle**

```powershell
git add app/src/main/python app/src/main/assets fixtures app/src/test
git commit -m "feat: bundle PaedDeadSpace Core v1.0.0 with release metadata"
```

## Task 3: Define the Android-to-Core calculation contract

**Files:**
- Create: `app/src/main/java/org/paeddeadspace/android/core/CalculationContract.kt`
- Create: `app/src/main/java/org/paeddeadspace/android/core/ScenarioValidator.kt`
- Create: `app/src/test/java/org/paeddeadspace/android/core/ScenarioValidatorTest.kt`

**Interfaces:**
- Consumes: explicit editable values from the scenario form.
- Produces: `CalculationRequest` for valid scenarios and `DraftValidationError` for missing or malformed fields.

- [ ] **Step 1: Write failing validator tests for blank fields and explicit model selection**

```kotlin
@Test fun blankAgeUnitIsRejectedBeforeTheCoreIsCalled() {
    val result = ScenarioValidator.toRequest(ScenarioDraft.blank())
    assertEquals(DraftValidationError.MissingAgeUnit, result.error)
}

@Test fun userDefinedModeRequiresPatientOnlyVdVt() {
    val result = ScenarioValidator.toRequest(
        ScenarioDraft.valid().copy(model = PatientModel.UserDefined, patientVdVt = null)
    )
    assertEquals(DraftValidationError.MissingPatientVdVt, result.error)
}
```

- [ ] **Step 2: Run the validator tests to verify failure**

Run:

```powershell
.\gradlew.bat testDebugUnitTest --tests "*ScenarioValidatorTest"
```

Expected: compilation failure because contract types are absent.

- [ ] **Step 3: Define the complete request and response types**

Create these exact contract names:

```kotlin
enum class AgeUnit { DAYS, MONTHS, YEARS }
enum class PatientModel { USER_DEFINED, PEARSALL_BENCHMARK, NUMA_FLETCHER_REFERENCE }
enum class ApparatusQualification { FUNCTIONAL_MEASURED, INTERNAL_OR_GEOMETRIC, USER_ESTIMATE }

data class CalculationRequest(
    val weightKg: Double,
    val ageValue: Double,
    val ageUnit: AgeUnit,
    val tidalVolumeMlKg: Double,
    val respiratoryRateBpm: Double,
    val model: PatientModel,
    val patientVdVt: Double?,
    val apparatusDeadSpaceMl: Double,
    val apparatusName: String,
    val apparatusQualification: ApparatusQualification,
)

sealed interface CoreResponse {
    data class Success(val result: CalculationResult) : CoreResponse
    data class InputError(val message: String) : CoreResponse
    data class ModelBreakdown(val message: String, val partial: PartialVolumes?) : CoreResponse
}
```

`CalculationResult` must include all values listed in the approved spec, source-domain warnings as `List<String>`, selected-model metadata, and the exact embedded Core version. `PartialVolumes` contains only `vtMl`, `patientVdMl`, `apparatusVdMl`, and `totalVdMl`, each nullable.

- [ ] **Step 4: Implement deterministic, non-scientific draft validation**

`ScenarioValidator.toRequest` must require numeric positive weight, nonnegative age, selected age unit, positive VT/kg, positive RR, selected model, nonnegative apparatus dead space, nonblank apparatus name/qualification, and patient VD/VT in `[0, 1]` only for `USER_DEFINED`. It must return a single field-specific `DraftValidationError` and never estimate a missing value.

- [ ] **Step 5: Run all validator tests**

Run:

```powershell
.\gradlew.bat testDebugUnitTest --tests "*ScenarioValidatorTest"
```

Expected: PASS for blank age unit, blank patient VD/VT in user-defined mode, valid request creation, and nonnegative apparatus dead-space acceptance.

- [ ] **Step 6: Commit the calculation contract**

```powershell
git add app/src/main/java/org/paeddeadspace/android/core app/src/test
git commit -m "feat: define explicit Android calculation contract"
```

## Task 4: Implement the Python bridge and Core equivalence tests

**Files:**
- Create: `app/src/main/python/android_bridge.py`
- Create: `app/src/main/java/org/paeddeadspace/android/core/ChaquopyCoreGateway.kt`
- Create: `app/src/androidTest/java/org/paeddeadspace/android/core/ChaquopyCoreGatewayTest.kt`
- Modify: `fixtures/core_v1_0_0_cases.json`

**Interfaces:**
- Consumes: `CalculationRequest` serialized to JSON.
- Produces: `CoreResponse` by invoking Python function `android_bridge.compute_case(request_json: str) -> str`.

- [ ] **Step 1: Add a failing instrumented golden-case test**

```kotlin
@Test fun pearsallGoldenCaseMatchesFrozenCoreOutput() = runTest {
    val response = gateway.calculate(pearsallGoldenRequest())
    val success = assertIs<CoreResponse.Success>(response)
    assertEquals(80.0, success.result.vtMl, 0.0001)
    assertEquals(54.0, success.result.totalVdMl, 0.0001)
    assertEquals(43.0769230769, success.result.rrRequiredBpm, 0.0001)
    assertEquals("1.0.0", success.result.coreVersion)
}
```

- [ ] **Step 2: Run the instrumented test to verify that the bridge is absent**

Run:

```powershell
.\gradlew.bat connectedDebugAndroidTest --tests "*ChaquopyCoreGatewayTest"
```

Expected: compilation failure because `ChaquopyCoreGateway` does not exist.

- [ ] **Step 3: Write a JSON-only Python adapter around public Core APIs**

Implement `compute_case` in `android_bridge.py` with this contract:

```python
def compute_case(request_json: str) -> str:
    """Return JSON with status: success, input_error, or model_breakdown."""
```

The bridge must:

- parse explicit JSON keys only;
- convert age days/months/years to Core `age_years` using the audited UI conversion semantics;
- build `VentilationInputs`, one Core patient-model object, and one `ApparatusComponent` with `USER_DEFINED` provenance;
- invoke `calculate_ventilation` for current and no-added-apparatus baseline states;
- invoke `rr_required_to_preserve_alveolar_ventilation` only after a physical current Core state;
- capture and deduplicate `SourceDomainWarning` messages;
- map `InputValidationError` to `input_error` and `NonPhysicalStateError` to `model_breakdown`;
- serialize Core-derived numbers without applying clinical interpretation or display rounding.

Do not implement any physiological arithmetic outside calls to public `paeddeadspace` functions. The bridge may only do unit conversion, JSON transformation, and result packaging.

- [ ] **Step 4: Implement the Kotlin Chaquopy gateway**

```kotlin
interface CoreGateway {
    suspend fun calculate(request: CalculationRequest): CoreResponse
}

class ChaquopyCoreGateway(private val appContext: Context) : CoreGateway {
    override suspend fun calculate(request: CalculationRequest): CoreResponse =
        withContext(Dispatchers.Default) {
            if (!Python.isStarted()) {
                Python.start(AndroidPlatform(appContext))
            }
            val rawJson = Python.getInstance()
                .getModule("android_bridge")
                .callAttr("compute_case", CalculationRequestCodec.encode(request))
                .toString()
            CoreResponseCodec.decode(rawJson)
        }
}
```

Start Python once through `Python.start(AndroidPlatform(appContext))`, acquire `android_bridge`, call `compute_case` with a serialized `CalculationRequest`, and decode the returned JSON into the sealed `CoreResponse`. Python exceptions not represented by bridge JSON must map to `CoreResponse.InputError("Calculation could not be completed.")` while preserving the full error only in local debug logging.

- [ ] **Step 5: Expand the fixture and test the required Core behaviors**

Add JSON fixture entries and instrumentation tests for:

```text
pearsall_10kg_30ml: success, VT 80, total VD 54, alveolar VE 520, RR 43.0769230769
numa_fletcher_warning: success with at least one source-domain warning
user_defined_zero_apparatus: success with apparatus VD 0
invalid_negative_weight: input_error
model_breakdown_vd_ge_vt: model_breakdown and no rrRequiredBpm value
```

Run:

```powershell
.\gradlew.bat connectedDebugAndroidTest --tests "*ChaquopyCoreGatewayTest"
```

Expected: PASS for all five fixture entries; no test calculates expected physiology in Kotlin.

- [ ] **Step 6: Commit the Core bridge**

```powershell
git add app/src/main/python/android_bridge.py app/src/main/java/org/paeddeadspace/android/core app/src/androidTest fixtures
git commit -m "feat: invoke frozen Core through Android bridge"
```

## Task 5: Implement the essential Compose workflow

**Files:**
- Create: `app/src/main/java/org/paeddeadspace/android/state/PaedDeadSpaceViewModel.kt`
- Create: `app/src/main/java/org/paeddeadspace/android/ui/NoticeScreen.kt`
- Create: `app/src/main/java/org/paeddeadspace/android/ui/ScenarioScreen.kt`
- Create: `app/src/main/java/org/paeddeadspace/android/ui/ResultsScreen.kt`
- Create: `app/src/main/java/org/paeddeadspace/android/ui/DetailsScreen.kt`
- Modify: `app/src/main/java/org/paeddeadspace/android/ui/PaedDeadSpaceApp.kt`
- Modify: `app/src/main/res/values/strings.xml`
- Test: `app/src/androidTest/java/org/paeddeadspace/android/ui/ScenarioFlowTest.kt`

**Interfaces:**
- Consumes: `ScenarioValidator`, `CoreGateway`, `CoreResponse`, and Core release metadata.
- Produces: a three-screen, in-memory-only user workflow.

- [ ] **Step 1: Write failing Compose tests for the required workflow**

```kotlin
@Test fun calculateIsBlockedUntilAgeUnitAndModelAreExplicitlySelected() {
    rule.onNodeWithText("Calculate").performClick()
    rule.onNodeWithText("Select an age unit").assertExists()
    rule.onNodeWithText("Select a patient dead-space model").assertExists()
}

@Test fun newScenarioClearsInMemoryValues() {
    launchSuccessfulResult()
    rule.onNodeWithText("New scenario").performClick()
    rule.onNodeWithText("Weight, kg").assertTextEquals("")
}
```

- [ ] **Step 2: Run the UI test to verify it fails**

Run:

```powershell
.\gradlew.bat connectedDebugAndroidTest --tests "*ScenarioFlowTest"
```

Expected: failure because the scenario and result screens do not yet exist.

- [ ] **Step 3: Implement view-model state with no persistence layer**

Define `PaedDeadSpaceUiState` as a sealed interface with `Notice`, `Scenario(ScenarioDraft, fieldErrors)`, `Calculating`, and `Results(CoreResponse)` states. `PaedDeadSpaceViewModel` retains state only in memory, calls `ScenarioValidator` before `CoreGateway`, and resets to a fresh blank `ScenarioDraft` when `newScenario()` is called. Do not use DataStore, Room, SharedPreferences, files, or saved case state.

- [ ] **Step 4: Implement the three screens using the approved wording**

Use these mandatory visible strings:

```text
Educational and research use
For healthcare professionals. This app does not provide patient-specific clinical recommendations.
New scenario
Patient dead-space model
Age unit
Calculate
Results
Details and provenance
New scenario
Mathematical model boundary
```

`ScenarioScreen` must use blank fields for required values, a select-only age unit, an explicit model picker, and apparatus qualification selection. `ResultsScreen` must render warnings before values, render success values in the approved order, and render no graph. `ModelBreakdown` must show its boundary message and partial volumes only; it must not render relative CO2 burden or RR required. `DetailsScreen` must present model status, Core version, GitHub URL, DOI, and the non-clinical scope.

- [ ] **Step 5: Run Compose workflow and accessibility checks**

Run:

```powershell
.\gradlew.bat connectedDebugAndroidTest --tests "*ScenarioFlowTest"
.\gradlew.bat lintDebug
```

Expected: PASS; lint reports no hard errors; no required field has a default number or preselected model/unit.

- [ ] **Step 6: Commit the essential mobile UI**

```powershell
git add app/src/main/java/org/paeddeadspace/android/state app/src/main/java/org/paeddeadspace/android/ui app/src/main/res app/src/androidTest
git commit -m "feat: add essential offline calculation workflow"
```

## Task 6: Enforce offline privacy and intended-use boundaries

**Files:**
- Create: `docs/APP_INTENDED_USE.md`
- Create: `PRIVACY_POLICY.md`
- Create: `app/src/test/java/org/paeddeadspace/android/ManifestPolicyTest.kt`
- Modify: `app/src/main/AndroidManifest.xml`
- Modify: `app/src/main/res/values/strings.xml`
- Modify: `README.md`

**Interfaces:**
- Consumes: the approved scope and final rendered application behavior.
- Produces: machine-testable no-permission policy and consistent in-app/repository text.

- [ ] **Step 1: Write a failing manifest policy test**

```kotlin
@Test fun manifestDeclaresNoInternetOrSensitiveDataPermission() {
    val permissions = ManifestPermissions.load("src/main/AndroidManifest.xml")
    assertFalse("android.permission.INTERNET" in permissions)
    assertTrue(permissions.isEmpty())
}
```

- [ ] **Step 2: Run the test to verify that its helper is absent**

Run:

```powershell
.\gradlew.bat testDebugUnitTest --tests "*ManifestPolicyTest"
```

Expected: compilation failure because `ManifestPermissions` is not implemented.

- [ ] **Step 3: Write intended-use and privacy-policy sources**

`docs/APP_INTENDED_USE.md` and `README.md` must state that the Android app is an offline educational/research reference for healthcare professionals; it does not diagnose, treat, recommend settings, or provide patient-specific decision support.

`PRIVACY_POLICY.md` must state exactly that version 1 does not collect, store, transmit, share, or sell personal data; does not use accounts, advertising, analytics, telemetry, cookies, or device sensors; and requests no runtime permissions. It must also say that users should not enter patient-identifying information. Before Play submission, publish this text as a public, non-editable web page; do not use a PDF as the Play privacy-policy URL.

- [ ] **Step 4: Implement the XML-permission test helper and ensure the manifest stays empty**

Implement `ManifestPermissions.load(path: String): Set<String>` by parsing `<uses-permission>` nodes from the repository manifest. Do not add a permission to make the test pass. Keep the manifest without `<uses-permission>` elements.

- [ ] **Step 5: Run unit tests, lint, and inspect merged permissions**

Run:

```powershell
.\gradlew.bat testDebugUnitTest lintDebug
.\gradlew.bat :app:processDebugMainManifest
```

Expected: all tests pass; the generated merged manifest contains no Internet or sensitive-data permission.

- [ ] **Step 6: Commit the policy boundary**

```powershell
git add AndroidManifest.xml docs PRIVACY_POLICY.md README.md app/src/test app/src/main/res
git commit -m "docs: define offline privacy and educational-use boundaries"
```

## Task 7: Automate verification and prepare the release record

**Files:**
- Create: `.github/workflows/android.yml`
- Create: `docs/RELEASE_CHECKLIST.md`
- Create: `docs/PLAY_STORE_DRAFT.md`
- Create: `CITATION.cff`
- Create: `CHANGELOG.md`
- Create: `RELEASE_MANIFEST.md`
- Create: `scripts/write_manifest_sha256.py`
- Create: `scripts/verify_core_bundle.py`
- Test: `scripts/test_verify_core_bundle.py`

**Interfaces:**
- Consumes: the embedded Core package, `core_release.json`, test fixtures, app version, and final Android App Bundle.
- Produces: reproducible CI evidence, release metadata, checksums, and human-controlled publication gates.

- [ ] **Step 1: Write a failing Core-bundle verification test**

```python
def test_core_bundle_matches_the_declared_v1_release() -> None:
    report = verify_core_bundle("app/src/main/python/paeddeadspace", "app/src/main/assets/core_release.json")
    assert report.package_version == "1.0.0"
    assert report.changed_files == []
```

- [ ] **Step 2: Run the script test to verify failure**

Run:

```powershell
python -m pytest -q scripts/test_verify_core_bundle.py
```

Expected: failure because `verify_core_bundle.py` is not implemented.

- [ ] **Step 3: Implement release-integrity scripts and CI**

`verify_core_bundle.py` must compare every bundled Core file against a checked-in copy of the official v1.0.0 manifest and return changed/missing/unexpected file lists. `write_manifest_sha256.py` must generate checksums only for release files chosen in `RELEASE_MANIFEST.md`; it must exclude local Gradle caches, keystores, build directories, and Play credentials.

The GitHub workflow must run on pull requests and tags, set up JDK 17 and Python 3.13, run `testDebugUnitTest`, `lintDebug`, Python bridge tests, and `assembleRelease`. On a protected release tag it also runs `bundleRelease` and uploads the unsigned `.aab` as a workflow artifact. Never store a signing key, Zenodo token, or Google credential in the repository or workflow output.

- [ ] **Step 4: Draft the exact human release gates**

`docs/RELEASE_CHECKLIST.md` must require: Core equivalence test evidence; offline-device test; manifest permission review; final version linkage; GitHub tag/release; Zenodo archive/DOI; manuscript update; public privacy-policy URL; final Play health declaration; Data safety disclosure; screenshots; content rating; internal Play test; and final production review.

`docs/PLAY_STORE_DRAFT.md` must contain only educational/research wording, identify the professional audience, state that the app is not a medical device and does not provide patient-specific recommendations, and avoid any diagnosis, treatment, safety, or performance claim.

- [ ] **Step 5: Run the complete local verification sequence**

Run:

```powershell
.\gradlew.bat testDebugUnitTest lintDebug assembleDebug bundleRelease
.\gradlew.bat connectedDebugAndroidTest
python -m pytest -q scripts/test_verify_core_bundle.py
python scripts/write_manifest_sha256.py --check
```

Expected: all commands pass; the resulting AAB is unsigned and suitable only for the controlled release process.

- [ ] **Step 6: Commit release automation and documentation**

```powershell
git add .github docs scripts CITATION.cff CHANGELOG.md RELEASE_MANIFEST.md MANIFEST_SHA256.txt
git commit -m "chore: add Android verification and release records"
```

## Task 8: Perform the controlled public-release sequence

**Files:**
- Modify: `CHANGELOG.md`
- Modify: `RELEASE_MANIFEST.md`
- Modify: `MANIFEST_SHA256.txt`
- Modify: `docs/RELEASE_CHECKLIST.md`
- Modify later, after Zenodo DOI exists: PaedDeadSpace manuscript in its own controlled worktree

**Interfaces:**
- Consumes: approved signed release candidate, GitHub release URL, Zenodo DOI, final manuscript update, Play Store account and listing materials controlled by the user.
- Produces: a traceable public Android release and, only afterward, a Play production submission.

- [ ] **Step 1: Freeze a release candidate and run its full evidence suite**

Run:

```powershell
git status --short
.\gradlew.bat testDebugUnitTest lintDebug bundleRelease connectedDebugAndroidTest
python -m pytest -q scripts/test_verify_core_bundle.py
python scripts/write_manifest_sha256.py --write
git diff -- MANIFEST_SHA256.txt RELEASE_MANIFEST.md CHANGELOG.md
```

Expected: a clean working tree before checksum generation; all tests pass; only intended release-record changes remain afterward.

- [ ] **Step 2: Create the GitHub release from an immutable tag**

Run after user-controlled signing and version selection:

```powershell
git add CHANGELOG.md RELEASE_MANIFEST.md MANIFEST_SHA256.txt
git commit -m "chore: prepare Android v1.0.0 release"
git tag -a v1.0.0 -m "PaedDeadSpace Android v1.0.0"
git push origin main --tags
```

Attach the signed Android App Bundle and checksum record to the GitHub release. Do not make a GitHub Release asset available until the tag, manifest, and build artifact agree.

- [ ] **Step 3: Archive exactly the GitHub release in Zenodo**

Create the Zenodo archive from the immutable GitHub v1.0.0 release, capture its Android DOI, and add it to `RELEASE_MANIFEST.md` in a follow-up tag. Do not alter bundled Core files after DOI assignment.

- [ ] **Step 4: Update the manuscript only after Android repository and DOI exist**

In a separate manuscript task, add the Android repository and DOI to Software and code availability, update the software-architecture description, and preserve all educational/non-prescriptive claims. Run a complete citations/references consistency check and DOCX structural plus visual QA before delivery.

- [ ] **Step 5: Complete Play Console content before production release**

Under the user-controlled Play account, complete the Health Apps declaration using the final actual feature set, Data safety disclosure, public privacy-policy URL, store listing, screenshots, content rating, app access declaration, and internal test track. Do not select clinical decision support or medical-device functionality unless a separate compliance assessment changes the approved intended use.

- [ ] **Step 6: Publish only after the final checklist is signed off**

Confirm every checkbox in `docs/RELEASE_CHECKLIST.md`, review the production AAB generated from the tagged source, and submit to Play production. Record the Play listing URL and release status in the release manifest without changing the scientific scope.

## Plan Self-Review

### Spec coverage

- Offline, no-account/no-storage/no-network behavior: Tasks 1, 5, and 6.
- Frozen Core as sole scientific source: Tasks 2 and 4.
- Essential three-screen mobile UX and no graphs: Task 5.
- Warnings and mathematical model boundary: Tasks 3, 4, and 5.
- Exact Core equivalence evidence: Tasks 2, 4, and 7.
- GitHub, Zenodo, manuscript, and Play order: Tasks 7 and 8.
- Educational/non-prescriptive wording and Play-facing controls: Tasks 5, 6, 7, and 8.

No specification requirement is unassigned.

### Placeholder scan

The plan contains no unresolved design marker or placeholder step. Every shown interface has a concrete implementation path and an owning test.

### Type consistency

`CalculationRequest` is created only by `ScenarioValidator`, serialized by `ChaquopyCoreGateway`, parsed by `android_bridge.compute_case`, and decoded into `CoreResponse`. `CoreResponse.ModelBreakdown` is rendered by `ResultsScreen` without RR or relative-CO2 fields. All later task references use those same names.

### Review-focus coverage

- Missing required fields: Task 5 Step 1.
- Model breakdown suppression: Task 4 Step 5 and Task 5 Step 4.
- Pearsall numerical equivalence: Task 4 Step 1.
- Source-domain warnings: Task 4 Step 5.
- No permission/no retained scenario state: Task 5 Step 1 and Task 6 Steps 1–5.
