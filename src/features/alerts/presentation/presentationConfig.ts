/**
 * Configuration limits for Alert Presentation.
 * 
 * These constants control the "Capping Strategy" to prevent UI overload.
 */
export const PRESENTATION_CONFIG = {
    /** Maximum number of alerts shown in the main list/map view before "Show More" */
    MAX_VISIBLE_ALERTS: 5,

    /** 
     * Maximum number of "Low Importance" (Low/Moderate) alerts to show 
     * within the visible set. Rigidly reserves space for higher priority items.
     */
    MAX_LOW_PRIORITY_VISIBLE: 2,
};
