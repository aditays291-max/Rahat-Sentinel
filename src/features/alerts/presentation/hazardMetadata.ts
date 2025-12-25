import { HazardLevel } from '../../../types/alerts';

/**
 * Definition of UI semantics for a hazard.
 */
export interface HazardMetadata {
    label: string;
    /** Hex color code for badges/icons */
    color: string;
    /** Numeric priority (Start at 1, Higher is more critical) */
    priority: number;
}

/**
 * Single Source of Truth for Hazard Semantics.
 * 
 * WHY: We map abstract enums (CRITICAL) to concrete UI properties (Red Color, Priority 4).
 * This ensures that a "Critical" alert always looks the same, whether it's on a map pin,
 * a list item, or a push notification.
 */
export const HAZARD_METADATA: Record<HazardLevel, HazardMetadata> = {
    [HazardLevel.CRITICAL]: {
        label: 'CRITICAL',
        color: '#e74c3c', // Red
        priority: 4,
    },
    [HazardLevel.SEVERE]: {
        label: 'SEVERE',
        color: '#e67e22', // Orange
        priority: 3,
    },
    [HazardLevel.MODERATE]: {
        label: 'MODERATE',
        color: '#f1c40f', // Yellow
        priority: 2,
    },
    [HazardLevel.LOW]: {
        label: 'INFO',
        color: '#3498db', // Blue
        priority: 1,
    },
};
