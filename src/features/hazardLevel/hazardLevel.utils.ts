import { HazardLevel } from '../../types/alerts';
import { HazardMetadata } from './hazardLevel.types';

const HAZARD_CONFIG: Record<HazardLevel, HazardMetadata> = {
    [HazardLevel.CRITICAL]: {
        label: 'Critical',
        description: 'Immediate threat to life or property. Take action now.',
        priority: 4,
        color: 'red',
    },
    [HazardLevel.SEVERE]: {
        label: 'Severe',
        description: 'Significant threat. Be prepared to take action.',
        priority: 3,
        color: 'orange',
    },
    [HazardLevel.MODERATE]: {
        label: 'Moderate',
        description: 'Possible threat. Stay informed.',
        priority: 2,
        color: 'yellow',
    },
    [HazardLevel.LOW]: {
        label: 'Low',
        description: 'Minimal threat. Awareness required.',
        priority: 1,
        color: 'green',
    },
};

/**
 * Returns the metadata associated with a specific HazardLevel.
 */
export const getHazardMetadata = (level: HazardLevel): HazardMetadata => {
    return HAZARD_CONFIG[level];
};

/**
 * Checks if a hazard level is considered 'Severe' or higher.
 * useful for filtering high-priority alerts.
 */
export const isSevereOrAbove = (level: HazardLevel): boolean => {
    const meta = getHazardMetadata(level);
    const severeMeta = getHazardMetadata(HazardLevel.SEVERE);
    return meta.priority >= severeMeta.priority;
};

/**
 * Compares two HazardLevels for sorting.
 * Returns positive if a > b (a is higher priority).
 * Returns negative if a < b.
 * Returns 0 if equal.
 */
export const compareHazard = (a: HazardLevel, b: HazardLevel): number => {
    const metaA = getHazardMetadata(a);
    const metaB = getHazardMetadata(b);
    return metaA.priority - metaB.priority;
};
