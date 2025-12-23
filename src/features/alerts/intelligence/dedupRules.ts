import { Alert } from '../../../types/alerts';
import { calculateDistanceMeters } from '../../location/geoRules';

/**
 * Checks if two alerts are considered equivalent based on source, location, and time.
 *
 * Equivalence Criteria:
 * 1. Same source
 * 2. Distance between locations <= 300 meters
 * 3. Absolute timestamp difference <= 10 minutes
 *
 * @param a - First alert
 * @param b - Second alert
 * @returns boolean - True if alerts are equivalent
 */
export const areAlertsEquivalent = (a: Alert, b: Alert): boolean => {
    // 1. Check source
    if (a.source !== b.source) {
        return false;
    }

    // 2. Check timestamp difference (10 minutes = 600,000 ms)
    const TIME_THRESHOLD_MS = 10 * 60 * 1000;
    const timeDiff = Math.abs(a.timestamp - b.timestamp);
    if (timeDiff > TIME_THRESHOLD_MS) {
        return false;
    }

    // 3. Check distance (300 meters)
    const DISTANCE_THRESHOLD_METERS = 300;
    const distance = calculateDistanceMeters(a.location, b.location);

    if (distance > DISTANCE_THRESHOLD_METERS) {
        return false;
    }

    return true;
};

/**
 * Deduplicates a list of alerts based on equivalence rules.
 * Keeps the most recent alert when duplicates are found.
 *
 * @param alerts - List of alerts to deduplicate
 * @returns Alert[] - Deduplicated list of alerts
 */
export const deduplicateAlerts = (alerts: Alert[]): Alert[] => {
    if (!alerts || alerts.length === 0) {
        return [];
    }

    // Sort by timestamp descending (most recent first)
    // Creating a shallow copy to avoid mutating the original array
    const sortedAlerts = [...alerts].sort((a, b) => b.timestamp - a.timestamp);

    const uniqueAlerts: Alert[] = [];

    for (const alert of sortedAlerts) {
        // Check if this alert is equivalent to any we've already kept
        // Since we sorted by newest first, if we find a match, the one in uniqueAlerts
        // is newer (or same time) and we should skip the current 'alert' (the older/duplicate one).
        const isDuplicate = uniqueAlerts.some((keptAlert) =>
            areAlertsEquivalent(keptAlert, alert)
        );

        if (!isDuplicate) {
            uniqueAlerts.push(alert);
        }
    }

    return uniqueAlerts;
};
