"""JSON-only Android adapter for the frozen PaedDeadSpace-Core v1.0.0.

This module performs input decoding, age-unit conversion and output packaging.
All physiological calculations and model-boundary decisions are delegated to
the public ``paeddeadspace`` API.
"""

from __future__ import annotations

import json
import math
import warnings
from typing import Any

import paeddeadspace as pds


_REQUEST_KEYS = {
    "weight_kg",
    "age_value",
    "age_unit",
    "tidal_volume_ml_kg",
    "respiratory_rate_bpm",
    "model",
    "patient_vd_vt",
    "apparatus_dead_space_ml",
    "apparatus_name",
    "apparatus_qualification",
}

_QUALIFICATIONS = {
    "FUNCTIONAL_MEASURED": "Measured/estimated functional dead space",
    "INTERNAL_OR_GEOMETRIC": "Manufacturer internal/geometric volume",
    "USER_ESTIMATE": "User estimate / uncertain",
}

_MODEL_METADATA = {
    "USER_DEFINED": {
        "key": "USER_DEFINED",
        "name": "User-defined patient VD/VT",
        "kind": "user-defined",
    },
    "NUMA_FLETCHER_REFERENCE": {
        "key": "NUMA_FLETCHER_REFERENCE",
        "name": "Numa–Fletcher composite reference",
        "kind": "assumption/model construction",
    },
    "PEARSALL_BENCHMARK": {
        "key": "PEARSALL_BENCHMARK",
        "name": "Pearsall benchmark (VD/VT 0.30)",
        "kind": "benchmark only",
    },
}

_BREAKDOWN_MESSAGE = (
    "One-compartment model breakdown: effective dead space approaches or "
    "exceeds tidal volume."
)


def _number(name: str, value: Any) -> float:
    if value is None or isinstance(value, bool):
        raise pds.InputValidationError(f"{name} must be numeric")
    try:
        result = float(value)
    except (TypeError, ValueError) as exc:
        raise pds.InputValidationError(f"{name} must be numeric") from exc
    if not math.isfinite(result):
        raise pds.InputValidationError(f"{name} must be finite")
    return result


def _age_years(age_value: float, unit: str) -> float:
    if age_value < 0.0:
        raise pds.InputValidationError("age_value must be >= 0")
    if unit == "DAYS":
        return age_value / 365.25
    if unit == "MONTHS":
        return age_value / 12.0
    if unit == "YEARS":
        return age_value
    raise pds.InputValidationError("age_unit must be DAYS, MONTHS, or YEARS")


def _patient_model(model_key: str, patient_vd_vt: Any) -> Any:
    if model_key == "USER_DEFINED":
        return pds.UserDefinedPatientVdVt(_number("patient_vd_vt", patient_vd_vt))
    if model_key == "NUMA_FLETCHER_REFERENCE":
        return pds.NumaFletcherCompositeReference()
    if model_key == "PEARSALL_BENCHMARK":
        return pds.PearsallBenchmarkPatientDeadSpace()
    raise pds.InputValidationError("unsupported patient dead-space model")


def _apparatus_component(data: dict[str, Any]) -> pds.ApparatusComponent:
    qualification_key = str(data["apparatus_qualification"])
    qualification = _QUALIFICATIONS.get(qualification_key)
    if qualification is None:
        raise pds.InputValidationError("unsupported apparatus qualification")

    provenance = pds.Provenance(
        classification=pds.ParameterClassification.USER_DEFINED,
        source_id="ANDROID_USER_ENTERED_APPARATUS",
        citation="User-entered apparatus dead-space value",
        context=(
            f"The user qualified this entry as: {qualification}. The app does "
            "not convert geometric/internal volume into functional dead space."
        ),
    )
    return pds.ApparatusComponent(
        name=str(data["apparatus_name"]),
        dead_space_ml=_number(
            "apparatus_dead_space_ml", data["apparatus_dead_space_ml"]
        ),
        active=True,
        provenance=provenance,
        qualification=qualification,
    )


def _warning_messages(records: list[warnings.WarningMessage]) -> list[str]:
    result: list[str] = []
    seen: set[str] = set()
    for record in records:
        if issubclass(record.category, pds.SourceDomainWarning):
            message = str(record.message)
            if message not in seen:
                seen.add(message)
                result.append(message)
    return result


def _partial_state(
    inputs: pds.VentilationInputs,
    patient_model: Any,
    apparatus: tuple[pds.ApparatusComponent, ...],
) -> dict[str, float]:
    vt_ml = pds.calculate_tidal_volume_ml(
        inputs.weight_kg,
        vt_ml_kg=inputs.vt_ml_kg,
    )
    patient_vd_ml = pds.patient_dead_space_excluding_apparatus_ml(
        vt_ml,
        patient_model,
        weight_kg=inputs.weight_kg,
        age_years=inputs.age_years,
    )
    apparatus_vd_ml = pds.apparatus_stack_dead_space_ml(apparatus)
    total_vd_ml = pds.total_dead_space_ml(patient_vd_ml, apparatus_vd_ml)
    return {
        "vt_ml": vt_ml,
        "patient_vd_ml": patient_vd_ml,
        "apparatus_vd_ml": apparatus_vd_ml,
        "total_vd_ml": total_vd_ml,
    }


def _decode_request(request_json: str) -> dict[str, Any]:
    try:
        decoded = json.loads(request_json)
    except json.JSONDecodeError as exc:
        raise pds.InputValidationError("request must be valid JSON") from exc
    if not isinstance(decoded, dict):
        raise pds.InputValidationError("request must be a JSON object")
    keys = set(decoded)
    if keys != _REQUEST_KEYS:
        missing = sorted(_REQUEST_KEYS - keys)
        unexpected = sorted(keys - _REQUEST_KEYS)
        detail: list[str] = []
        if missing:
            detail.append(f"missing keys: {', '.join(missing)}")
        if unexpected:
            detail.append(f"unexpected keys: {', '.join(unexpected)}")
        raise pds.InputValidationError("; ".join(detail))
    return decoded


def compute_case(request_json: str) -> str:
    """Return JSON with status ``success``, ``input_error`` or ``model_breakdown``."""

    caught: list[warnings.WarningMessage] = []
    partial: dict[str, float] | None = None
    model_metadata: dict[str, str] | None = None

    try:
        data = _decode_request(request_json)
        weight_kg = _number("weight_kg", data["weight_kg"])
        age_value = _number("age_value", data["age_value"])
        age_years = _age_years(age_value, str(data["age_unit"]))
        vt_ml_kg = _number("tidal_volume_ml_kg", data["tidal_volume_ml_kg"])
        rr_bpm = _number("respiratory_rate_bpm", data["respiratory_rate_bpm"])
        model_key = str(data["model"])
        model_metadata = _MODEL_METADATA.get(model_key)
        if model_metadata is None:
            raise pds.InputValidationError("unsupported patient dead-space model")

        patient_model = _patient_model(model_key, data["patient_vd_vt"])
        apparatus = (_apparatus_component(data),)
        inputs = pds.VentilationInputs(
            weight_kg=weight_kg,
            age_years=age_years,
            rr_bpm=rr_bpm,
            vt_ml_kg=vt_ml_kg,
        )

        with warnings.catch_warnings(record=True) as caught:
            warnings.simplefilter("always", pds.SourceDomainWarning)
            partial = _partial_state(inputs, patient_model, apparatus)
            current = pds.calculate_ventilation(inputs, patient_model, apparatus)
            baseline = pds.calculate_ventilation(inputs, patient_model, ())
            relative_co2_burden = pds.relative_co2_burden(
                baseline.alveolar_ve_ml_min,
                current.alveolar_ve_ml_min,
            )
            rr_required_bpm = pds.rr_required_to_preserve_alveolar_ventilation(
                baseline.alveolar_ve_ml_min,
                current.vt_ml,
                current.total_vd_ml,
            )

        result = {
            "status": "success",
            "result": {
                "weight_kg": weight_kg,
                "age_value": age_value,
                "age_unit": str(data["age_unit"]),
                "age_years": age_years,
                "tidal_volume_ml_kg": vt_ml_kg,
                "respiratory_rate_bpm": rr_bpm,
                "vt_ml": current.vt_ml,
                "patient_vd_ml": current.patient_vd_ml_excluding_apparatus,
                "apparatus_vd_ml": current.apparatus_vd_ml,
                "total_vd_ml": current.total_vd_ml,
                "total_vd_vt": current.total_vd_vt,
                "alveolar_vt_ml": current.alveolar_vt_ml,
                "alveolar_ve_ml_min": current.alveolar_ve_ml_min,
                "baseline_alveolar_ve_ml_min": baseline.alveolar_ve_ml_min,
                "current_to_baseline_alveolar_ve_ratio": (
                    current.alveolar_ve_ml_min / baseline.alveolar_ve_ml_min
                ),
                "relative_co2_burden": relative_co2_burden,
                "rr_required_bpm": rr_required_bpm,
                "rr_multiplier": rr_required_bpm / inputs.rr_bpm,
                "apparatus_dead_space_percent_vt": (
                    current.apparatus_dead_space_percent_vt
                ),
                "vt_to_apparatus_dead_space_ratio": (
                    current.vt_to_apparatus_dead_space_ratio
                ),
                "patient_airway_dead_space_ml": (
                    current.patient_airway_dead_space_ml
                ),
                "patient_alveolar_tidal_volume_ml": (
                    current.patient_alveolar_tidal_volume_ml
                ),
                "patient_alveolar_dead_space_ml": (
                    current.patient_alveolar_dead_space_ml
                ),
                "warnings": _warning_messages(caught),
                "model": dict(model_metadata),
                "core_version": pds.__version__,
            },
        }
    except pds.NonPhysicalStateError as exc:
        result = {
            "status": "model_breakdown",
            "message": _BREAKDOWN_MESSAGE,
            "detail": str(exc),
            "partial": partial,
            "warnings": _warning_messages(caught),
            "model": model_metadata,
            "core_version": pds.__version__,
        }
    except pds.InputValidationError as exc:
        result = {
            "status": "input_error",
            "message": str(exc),
            "core_version": pds.__version__,
        }

    return json.dumps(result, ensure_ascii=False, separators=(",", ":"))
