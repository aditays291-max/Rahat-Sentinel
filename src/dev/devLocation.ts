import { UserLocation } from '../features/locationAwareness/locationContext';

/**
 * Fixed location for DEV mode demonstrations.
 * Centered on Kathmandu, Nepal to match demo alert coordinates.
 */
export const DEV_FIXED_LOCATION: UserLocation = {
    latitude: 27.7172,
    longitude: 85.3240,
    accuracy: 10, // High accuracy for demo purposes
};

/**
 * Returns the fixed DEV location if in development mode, otherwise null.
 * This allows production builds to use real GPS without any overhead.
 *
 * @returns UserLocation | null
 */
export const getDevLocationOrNull = (): UserLocation | null => {
    if (__DEV__) {
        return DEV_FIXED_LOCATION;
    }
    return null;
};
