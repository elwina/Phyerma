#!/usr/bin/env python3
"""Download phyphox Sensor Database (devices.js) and write a JSON snapshot."""

from __future__ import annotations

import json
import re
import sys
import urllib.request
from datetime import datetime, timezone
from pathlib import Path

SOURCE_URL = "https://phyphox.org/sensordb/devices.js"


_PAIR = re.compile(
    r'([A-Za-z_][A-Za-z0-9_]*):((?:"(?:[^"\\]|\\.)*")|true|false|-?[0-9]+(?:\.[0-9]+)?(?:[eE][+-]?[0-9]+)?)'
)


def js_object_array_to_json(text: str) -> list:
    start = text.find("[")
    end = text.rfind("]")
    if start < 0 or end < 0:
        raise ValueError("devices.js does not contain a JSON-like array")
    body = text[start : end + 1]
    devices = []
    for raw_obj in re.findall(r"\{[^{}]+\}", body):
        item = {}
        for key, raw_val in _PAIR.findall(raw_obj):
            if raw_val.startswith('"'):
                item[key] = json.loads(raw_val)
            elif raw_val in ("true", "false"):
                item[key] = raw_val == "true"
            elif any(ch in raw_val for ch in ".eE"):
                item[key] = float(raw_val)
            else:
                item[key] = int(raw_val)
        if item:
            devices.append(item)
    if not devices:
        raise ValueError("parsed 0 device objects from devices.js")
    return devices


def main() -> int:
    root = Path(__file__).resolve().parents[1]
    dest_repo = root / "device-db" / "imported" / "phyphox-sensordb.json"
    dest_assets = root / "app" / "src" / "main" / "assets" / "device-db" / "phyphox-sensordb.json"
    dest_repo.parent.mkdir(parents=True, exist_ok=True)
    dest_assets.parent.mkdir(parents=True, exist_ok=True)

    print("Downloading", SOURCE_URL, flush=True)
    with urllib.request.urlopen(SOURCE_URL, timeout=120) as response:
        raw = response.read().decode("utf-8", errors="replace")

    devices = js_object_array_to_json(raw)
    payload = {
        "source": "phyphox-sensordb",
        "url": SOURCE_URL,
        "importedAt": datetime.now(timezone.utc).strftime("%Y-%m-%dT%H:%M:%SZ"),
        "count": len(devices),
        "devices": devices,
    }
    encoded = json.dumps(payload, ensure_ascii=False, separators=(",", ":"))
    dest_repo.write_text(encoded, encoding="utf-8")
    dest_assets.write_text(encoded, encoding="utf-8")
    print("Wrote", dest_assets, "devices:", len(devices), flush=True)
    return 0


if __name__ == "__main__":
    sys.exit(main())
