/**
 * Represents a geographic coordinate.
 */
interface GeoCoordinate {
    latitude: number;
    longitude: number;
}

/**
 * Calculates the distance between two points in meters using the Haversine formula.
 *
 * @param a - First coordinate
 * @param b - Second coordinate
 * @returns Distance in meters
 */
export const calculateDistanceMeters = (
    a: GeoCoordinate,
    b: GeoCoordinate,
): number => {
    // Safety check for inputs
    if (!a || !b || typeof a.latitude !== 'number' || typeof b.latitude !== 'number') {
        // Returns Infinity ensures that any "distance < radius" check will safely fail (return false).
        // This prevents crash-loops if sensors return malformed data.
        return Infinity;
    }

    const R = 6371e3; // Earth's radius in meters
    const phi1 = (a.latitude * Math.PI) / 180; // φ, λ in radians
    const phi2 = (b.latitude * Math.PI) / 180;
    const deltaPhi = ((b.latitude - a.latitude) * Math.PI) / 180;
    const deltaLambda = ((b.longitude - a.longitude) * Math.PI) / 180;

    const aVal =
        Math.sin(deltaPhi / 2) * Math.sin(deltaPhi / 2) +
        Math.cos(phi1) *
        Math.cos(phi2) *
        Math.sin(deltaLambda / 2) *
        Math.sin(deltaLambda / 2);

    const c = 2 * Math.atan2(Math.sqrt(aVal), Math.sqrt(1 - aVal));

    return R * c; // Distance in meters
};

/**
 * Checks if a user is within the radius of an alert.
 *
 * @param alertLocation - The center point of the alert (lat, lon, radius)
 * @param userLocation - The user's current location (lat, lon)
 * @returns boolean - True if user is inside the alert zone
 */
export const isAlertWithinRadius = (
    alertLocation: GeoCoordinate & { radius: number },
    userLocation: GeoCoordinate,
): boolean => {
    if (
        !alertLocation ||
        !userLocation ||
        typeof alertLocation.latitude !== 'number' ||
        typeof userLocation.latitude !== 'number' ||
        typeof alertLocation.radius !== 'number'
    ) {
        return false;
    }

    // Negative or zero radius logic:
    // If radius is negative, it's invalid -> return false.
    // If radius is 0, it matches only exact location -> typically false unless identical.
    if (alertLocation.radius < 0) {
        return false;
    }

    const distance = calculateDistanceMeters(alertLocation, userLocation);

    // Ensure distance is valid number before comparing
    if (!isFinite(distance)) {
        return false;
    }

    return distance <= alertLocation.radius;
};
