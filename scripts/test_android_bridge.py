from __future__ import annotations

import json
from pathlib import Path
import sys
import unittest


ROOT = Path(__file__).resolve().parents[1]
PYTHON_SRC = ROOT / "app" / "src" / "main" / "python"
sys.path.insert(0, str(PYTHON_SRC))

import android_bridge  # noqa: E402


def request(**overrides: object) -> str:
    values: dict[str, object] = {
        "weight_kg": 10.0,
        "age_value": 2.0,
        "age_unit": "YEARS",
        "tidal_volume_ml_kg": 8.0,
        "respiratory_rate_bpm": 20.0,
        "model": "PEARSALL_BENCHMARK",
        "patient_vd_vt": None,
        "apparatus_dead_space_ml": 30.0,
        "apparatus_name": "User-entered apparatus",
        "apparatus_qualification": "USER_ESTIMATE",
    }
    values.update(overrides)
    return json.dumps(values)


class AndroidBridgeTest(unittest.TestCase):
    def compute(self, **overrides: object) -> dict[str, object]:
        return json.loads(android_bridge.compute_case(request(**overrides)))

    def test_pearsall_golden_case_matches_frozen_core(self) -> None:
        response = self.compute()
        self.assertEqual("success", response["status"])
        result = response["result"]
        self.assertAlmostEqual(80.0, result["vt_ml"])
        self.assertAlmostEqual(54.0, result["total_vd_ml"])
        self.assertAlmostEqual(520.0, result["alveolar_ve_ml_min"])
        self.assertAlmostEqual(43.0769230769, result["rr_required_bpm"])
        self.assertEqual("1.0.0", result["core_version"])
        self.assertNotIn("current_to_baseline_alveolar_ve_ratio", result)
        self.assertNotIn("rr_multiplier", result)

    def test_numa_warning_is_returned_once(self) -> None:
        response = self.compute(
            age_value=0.0,
            age_unit="DAYS",
            model="NUMA_FLETCHER_REFERENCE",
            patient_vd_vt=None,
            apparatus_dead_space_ml=0.0,
        )
        self.assertEqual("success", response["status"])
        warnings = response["result"]["warnings"]
        self.assertEqual(1, len(warnings))
        self.assertIn("Numa intrathoracic component", warnings[0])

    def test_zero_apparatus_is_a_valid_success(self) -> None:
        response = self.compute(
            model="USER_DEFINED",
            patient_vd_vt=0.3,
            apparatus_dead_space_ml=0.0,
        )
        self.assertEqual("success", response["status"])
        self.assertEqual(0.0, response["result"]["apparatus_vd_ml"])
        self.assertAlmostEqual(1.0, response["result"]["relative_co2_burden"])

    def test_negative_weight_is_an_input_error(self) -> None:
        response = self.compute(weight_kg=-1.0)
        self.assertEqual("input_error", response["status"])
        self.assertIn("weight_kg", response["message"])

    def test_vd_at_or_above_vt_is_a_model_boundary_without_rr(self) -> None:
        response = self.compute(
            model="USER_DEFINED",
            patient_vd_vt=0.3,
            apparatus_dead_space_ml=60.0,
        )
        self.assertEqual("model_breakdown", response["status"])
        self.assertAlmostEqual(80.0, response["partial"]["vt_ml"])
        self.assertAlmostEqual(84.0, response["partial"]["total_vd_ml"])
        self.assertNotIn("rr_required_bpm", response)

    def test_age_conversion_matches_the_audited_ui_semantics(self) -> None:
        one_year_days = self.compute(age_value=365.25, age_unit="DAYS")
        one_year_months = self.compute(age_value=12.0, age_unit="MONTHS")
        self.assertEqual(1.0, one_year_days["result"]["age_years"])
        self.assertEqual(1.0, one_year_months["result"]["age_years"])


if __name__ == "__main__":
    unittest.main()
