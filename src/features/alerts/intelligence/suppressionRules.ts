import { Alert, HazardLevel } from '../../../types/alerts';
import { calculateDistanceMeters } from '../../location/geoRules';

/**
 * Priority values for HazardLevels. Higher value means higher priority.
 */
const HAZARD_PRIORITY: Record<HazardLevel, number> = {
    [HazardLevel.CRITICAL]: 4,
    [HazardLevel.SEVERE]: 3,
    [HazardLevel.MODERATE]: 2,
    [HazardLevel.LOW]: 1,
};

/**
 * Determines if an alert should be suppressed based on the existence of a higher severity alert nearby.
 *
 * Suppression Logic:
 * 1. Never suppress CRITICAL alerts.
 * 2. Suppress if there exists a higher priority alert within 500 meters.
 * 3. The higher priority alert must not be expired (relative to the candidate alert's timestamp).
 *
 * @param alert - The candidate alert to check for suppression
 * @param allAlerts - List of all known alerts (can include the candidate itself, handled safely)
 * @returns boolean - True if the alert should be suppressed
 */
export const shouldSuppressAlert = (alert: Alert, allAlerts: Alert[]): boolean => {
    // 1. Never suppress CRITICAL alerts
    if (alert.hazardLevel === HazardLevel.CRITICAL) {
        return false;
    }

    const SUPPRESSION_RADIUS_METERS = 500;
    const myPriority = HAZARD_PRIORITY[alert.hazardLevel];

    for (const otherAlert of allAlerts) {
        // Skip self
        if (otherAlert.id === alert.id) {
            continue;
        }

        // 3. Check if other alert is expired relative to this alert
        // We use alert.timestamp as "now" to remain deterministic and logic-only without Date.now()
        if (otherAlert.expiresAt <= alert.timestamp) {
            continue;
        }

        // Check priority
        const otherPriority = HAZARD_PRIORITY[otherAlert.hazardLevel];
        if (otherPriority > myPriority) {
            // Check distance
            const distance = calculateDistanceMeters(alert.location, otherAlert.location);
            if (distance <= SUPPRESSION_RADIUS_METERS) {
                // Found a higher priority, active alert nearby
                return true;
            }
        }
    }

    return false;
};
