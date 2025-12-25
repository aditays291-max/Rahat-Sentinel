/**
 * Represents the geographic location of the user.
 */
export interface UserLocation {
    latitude: number;
    longitude: number;
    /** Accuracy in meters (optional) */
    accuracy?: number;
}

// In-memory storage for the current user location.
// This is intentionally separated from the global store to avoid excessive re-renders
// on every GPS update.
let currentUserLocation: UserLocation | null = null;

/**
 * Retrieves the current known location of the user.
 *
 * @returns UserLocation | null
 */
export const getCurrentUserLocation = (): UserLocation | null => {
    return currentUserLocation;
};

/**
 * Updates the in-memory user location.
 * This should be called by the native GPS service (when implemented).
 *
 * @param newLocation - The new location data
 */
export const updateUserLocation = (newLocation: UserLocation): void => {
    // Basic validation to ensure we don't store bad data
    if (
        !newLocation ||
        typeof newLocation.latitude !== 'number' ||
        typeof newLocation.longitude !== 'number'
    ) {
        return;
    }
    currentUserLocation = newLocation;
};
