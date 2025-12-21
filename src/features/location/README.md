# Location Feature

This feature handles location services and geofencing.

## Components
- locationService: Manages GPS location.
- geoRules: Defines rules for geofencing.

## Edge Cases & Safety

### Invalid GPS Data
The logic defensively checks for `NaN` or non-finite coordinates. If an alert has invalid location data, `calculateDistanceMeters` returns `Infinity`, ensuring `isAlertWithinRadius` safely returns `false`.

### Negative Radius
An alert with a negative radius is considered physically impossible and invalid. The logic explicitly checks `radius < 0` and rejects it, preventing logic errors in downstream components.
