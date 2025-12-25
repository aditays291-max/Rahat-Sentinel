import { Alert, HazardLevel } from '../../../types/alerts';
import { PRESENTATION_CONFIG } from './presentationConfig';

/**
 * Splits the list of sorted alerts into "Visible" and "Hidden" buckets based on UI capacity rules.
 * 
 * Rules:
 * 1. CRITICAL alerts are ALWAYS visible (safety override).
 * 2. Visual clutter is capped at MAX_VISIBLE_ALERTS (default 5).
 * 3. Low/Moderate alerts are capped specifically at MAX_LOW_PRIORITY_VISIBLE (default 2)
 *    to prevent them from crowding out Severe alerts if the Critical cap isn't hit.
 *
 * @param alerts - The INPUT list (Assumed to be sorted by priority/recency already)
 * @returns { visible, hidden } - Partitioned alerts
 */
export const filterAlertsForDisplay = (alerts: Alert[]): { visible: Alert[]; hidden: Alert[] } => {
    const visible: Alert[] = [];
    const hidden: Alert[] = [];

    let lowPriorityCount = 0;

    for (const alert of alerts) {
        // Rule 1: Safety Override
        // If it's CRITICAL, we must show it, even if we exceed the UI cap.
        // Failing to show a critical alert is a liability.
        if (alert.hazardLevel === HazardLevel.CRITICAL) {
            visible.push(alert);
            continue;
        }

        // Rule 2: Global UI Cap
        // If we are already full, everything else is hidden.
        // (Unless it was Critical, which would have been caught by Rule 1)
        if (visible.length >= PRESENTATION_CONFIG.MAX_VISIBLE_ALERTS) {
            hidden.push(alert);
            continue;
        }

        // Rule 3: Noise Reduction (Low Priority Cap)
        // If it's a "Noisy" alert type (Low/Moderate), check if we have too many already.
        if (alert.hazardLevel === HazardLevel.LOW || alert.hazardLevel === HazardLevel.MODERATE) {
            if (lowPriorityCount >= PRESENTATION_CONFIG.MAX_LOW_PRIORITY_VISIBLE) {
                hidden.push(alert);
                continue;
            }
            lowPriorityCount++;
        }

        // If we passed all checks, it's visible.
        visible.push(alert);
    }

    return { visible, hidden };
};
