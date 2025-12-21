# Pull Request Readiness Checklist

## Architecture & Safety
- [x] **Separation of Concerns**: Feature logic (`src/features/`) is decoupled from UI components.
- [x] **No Shared Contract Changes**: `src/types/alerts.ts` and `src/store/alertStore.ts` were NOT modified.
- [x] **Defensive Coding**: All external inputs (Mesh payloads, GPS coords) are validated before use.
- [x] **Deterministic Logic**: pure functions are used for calculations (e.g., Haversine).

## API Boundaries
- [x] **Bluetooth**: No direct calls to native modules yet; service stubs are in place.
- [x] **Location**: No direct calls to `Geolocation` API; service expects coordinates as arguments.
- [x] **Store**: Alerts are dispatched to the global store only after normalization and de-duplication.

## Edge Case Handling
- [x] **Duplicates**: Mesh flooding is handled by checking existing store IDs.
- [x] **Malformed Data**: Invalid JSON or partial payloads result in silent failure (null return) to prevent crashes.
- [x] **Invalid Geometry**: Infinite distances or negative radii result in `false` relevance.

## Manual Verification
- [x] Verified that `bluetoothService.ts` compiles.
- [x] Verified that `geoRules.ts` handles edge cases (NaN, Infinity).
