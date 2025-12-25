import { Alert, HazardLevel } from '../../../types/alerts';

/**
 * Describes how the device should physically react to an incoming alert.
 */
export type DeliveryDecision = {
    /** Should a system notification be triggered? */
    notify: boolean;
    /** Android Notification Priority/Channel Importance */
    priority: 'LOW' | 'DEFAULT' | 'HIGH' | 'CRITICAL';
    /** Sound profile to play */
    sound: 'NONE' | 'DEFAULT' | 'ALARM';
    /** Vibration pattern to execute */
    vibration: 'NONE' | 'SHORT' | 'LONG';
};

/**
 * Determines the delivery behavior for a given alert based on its severity.
 * 
 * WHY: We separate "Decision" from "Execution". This pure function is easily testable,
 * whereas testing actual Android Notification logic is hard.
 *
 * Policy:
 * 1. CRITICAL: "Wake up the user" (Alarm sound, Long vibrate).
 * 2. SEVERE: "Pay attention" (High priority, Default sound).
 * 3. MODERATE: "Check when free" (Default priority).
 * 4. LOW: "Silent update" (No notification).
 *
 * @param alert - The alert to evaluate
 * @returns DeliveryDecision
 */
export const decideAlertDelivery = (alert: Alert): DeliveryDecision => {
    switch (alert.hazardLevel) {
        case HazardLevel.CRITICAL:
            return {
                notify: true,
                priority: 'CRITICAL',
                sound: 'ALARM',
                vibration: 'LONG',
            };
        case HazardLevel.SEVERE:
            return {
                notify: true,
                priority: 'HIGH',
                sound: 'DEFAULT',
                vibration: 'SHORT',
            };
        case HazardLevel.MODERATE:
            return {
                notify: true,
                priority: 'DEFAULT',
                sound: 'DEFAULT',
                vibration: 'NONE',
            };
        case HazardLevel.LOW:
        default:
            return {
                notify: false, // Silent!
                priority: 'LOW',
                sound: 'NONE',
                vibration: 'NONE',
            };
    }
};
