import { Alert, HazardLevel } from '../../../types/alerts';

/**
 * Represents the current visibility state of the application.
 */
export type AppVisibilityState = 'FOREGROUND' | 'BACKGROUND';

/**
 * Determines how the notification should be presented in distinct contexts.
 */
export type DeliveryContextDecision = {
    /** Whether to trigger a system notification at all */
    shouldNotify: boolean;
    /** The intrusiveness of the notification */
    mode: 'NONE' | 'SILENT' | 'HEADS_UP';
};

/**
 * Refines the delivery decision based on whether the user is looking at the app.
 * 
 * WHY:
 * 1. Background: User isn't looking. We need to notify them for most things.
 * 2. Foreground: User IS looking. 
 *    - "Moderate" alert? Don't buzz/pop-up. Just show it in the list (Live Update).
 *    - "Critical" alert? INTERRUPT EVERYTHING. Heads-up display.
 *
 * @param alert - The alert to evaluate
 * @param visibility - Current app state
 * @returns DeliveryContextDecision
 */
export const decideDeliveryContext = (
    alert: Alert,
    visibility: AppVisibilityState
): DeliveryContextDecision => {
    // 1. Critical Alerts: Always interrupt, regardless of stats
    if (alert.hazardLevel === HazardLevel.CRITICAL) {
        return { shouldNotify: true, mode: 'HEADS_UP' };
    }

    if (visibility === 'FOREGROUND') {
        // User is looking at the app.
        // Severe: Show a "Silent" notification (e.g. In-App Toast or passive banner)
        // Moderate/Low: No notification. The UI list will update automatically.
        if (alert.hazardLevel === HazardLevel.SEVERE) {
            return { shouldNotify: true, mode: 'SILENT' };
        }
        return { shouldNotify: false, mode: 'NONE' };
    } else {
        // User is NOT looking at the app (Background).
        // Severe: Heads Up (High Priority)
        // Moderate: Silent (Tray only, maybe sound depending on Policy)
        // Low: None (spam prevention)

        switch (alert.hazardLevel) {
            case HazardLevel.SEVERE:
                return { shouldNotify: true, mode: 'HEADS_UP' };
            case HazardLevel.MODERATE:
                // Note: DeliveryPolicy might say "Default Sound", 
                // Context says "Silent Mode" (Tray). 
                // The Platform Layer will reconcile: "Tray logic applies, but sound plays".
                // Here 'SILENT' mostly means "Don't Heads-Up".
                return { shouldNotify: true, mode: 'SILENT' };
            case HazardLevel.LOW:
            default:
                return { shouldNotify: false, mode: 'NONE' };
        }
    }
};
