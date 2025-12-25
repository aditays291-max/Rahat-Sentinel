import { Alert } from '../../types/alerts';
import { UserLocation } from './locationContext';
import { filterRelevantAlerts } from '../location/locationService';

/**
 * Re-evaluates which alerts are relevant based on the user's current location.
 * 
 * WHY: As the user evacuates, their context changes. An alert that was "irrelevant"
 * 10km away becomes "critical" when they enter the zone. This engine ensures
 * the UI reflects the *current* reality, not the reality when the packet arrived.
 *
 * @param alerts - The full list of active alerts
 * @param userLocation - The user's current location
 * @returns Alert[] - The subset of alerts that are geographically relevant
 */
export const recalculateRelevantAlerts = (
    alerts: Alert[],
    userLocation: UserLocation | null,
): Alert[] => {
    // 1. If location is unknown, we cannot filter by geography.
    // Return all alerts to ensure safety (better to show too much than too little).
    if (!userLocation) {
        return alerts;
    }

    // 2. Use the central location service to filter
    return filterRelevantAlerts(alerts, userLocation);
};
