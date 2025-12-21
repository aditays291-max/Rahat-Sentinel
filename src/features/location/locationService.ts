import { Alert } from '../../types/alerts';
import { isAlertWithinRadius } from './geoRules';

/**
 * Determines if a specific alert is relevant to the user based on
 * time validity and geographic proximity.
 *
 * @param alert - The alert object to check
 * @param userLocation - The user's current coordinate
 * @returns boolean - True if the alert should be shown/processed
 */
export const isAlertRelevantToUser = (
    alert: Alert,
    userLocation: { latitude: number; longitude: number },
): boolean => {
    // 1. Check Expiry
    // Alerts have a lifespan (TTL). If the disaster event is over, we don't show it.
    // This decoupled check allows the map to redraw periodically without re-fetching.
    const now = Date.now();
    if (alert.expiresAt && alert.expiresAt < now) {
        return false;
    }

    // 2. Check Geofence
    return isAlertWithinRadius(alert.location, userLocation);
};

/**
 * Filters a list of alerts to return only those relevant to the user.
 *
 * @param alerts - Array of alerts to filter
 * @param userLocation - The user's current coordinate
 * @returns Alert[] - Filtered list of relevant alerts
 */
export const filterRelevantAlerts = (
    alerts: Alert[],
    userLocation: { latitude: number; longitude: number },
): Alert[] => {
    if (!alerts || !Array.isArray(alerts) || !userLocation) {
        return [];
    }

    return alerts.filter((alert) => isAlertRelevantToUser(alert, userLocation));
};
