import { Alert } from '../../../types/alerts';
import { HAZARD_METADATA } from './hazardMetadata';

/**
 * Sorts alerts for display in a deterministic, safety-focused order.
 * 
 * Rules:
 * 1. Hazard Priority (Descending): CRITICAL first, LOW last.
 * 2. Recency (Descending): Newer alerts first.
 * 
 * WHY: In an emergency, specific "Bad News" (Critical) is more valuable than 
 * generic "New News" (Info). A 5-minute old flood warning is more important 
 * than a 1-minute old weather update.
 *
 * @param alerts - The list of alerts to sort
 * @returns Alert[] - A NEW sorted array (immutable sort)
 */
export const sortAlertsForDisplay = (alerts: Alert[]): Alert[] => {
    // Create shallow copy to adhere to "No Mutation" rule
    return [...alerts].sort((a, b) => {
        const priorityA = HAZARD_METADATA[a.hazardLevel].priority;
        const priorityB = HAZARD_METADATA[b.hazardLevel].priority;

        // 1. Primary Sort: Priority DESC
        if (priorityA !== priorityB) {
            return priorityB - priorityA;
        }

        // 2. Secondary Sort: Time DESC
        return b.timestamp - a.timestamp;
    });
};
