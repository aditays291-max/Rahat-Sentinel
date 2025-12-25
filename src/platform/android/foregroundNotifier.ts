import { AppState, Platform } from 'react-native';
import notifee from '@notifee/react-native';
import { Alert } from '../../types/alerts';
import { DeliveryDecision } from '../../features/alerts/delivery/deliveryPolicy';
import { DeliveryContextDecision } from '../../features/alerts/delivery/deliveryContext';
import { CHANNEL_IDS } from './notificationChannels';

/**
 * Executes a notification immediately while the app is in the FOREGROUND.
 * 
 * WHY:
 * - We need different behavior for "Heads Up" vs "Silent" alerts when the user is already looking at the app.
 * - This function is purely an execution adapter; decisions are made upstream (DeliveryContext).
 * 
 * RULES:
 * 1. Only run on Android (iOS has different foreground handling logic/delegate).
 * 2. Only run if AppState is 'active' (Foreground).
 * 3. Never throw errors (swallow them to prevent app crashes).
 */
export async function notifyInForeground(
    alert: Alert,
    _deliveryDecision: DeliveryDecision, // Unused for now, but commonly passed in the pipeline
    contextDecision: DeliveryContextDecision
): Promise<void> {
    // 1. Safety Guard: Android Only
    if (Platform.OS !== 'android') {
        return;
    }

    // 2. Safety Guard: Explicit Foreground Check
    // We only want to 'force' a notification if the app is truly active.
    // If it's background/inactive, background handlers should pick it up (Scope: NOT implemented here).
    if (AppState.currentState !== 'active') {
        return;
    }

    // 3. Logic: Check specific context decision
    if (contextDecision.mode === 'NONE') {
        return;
    }

    try {
        let channelId = CHANNEL_IDS.SILENT;

        // Map decision mode to Channel
        // HEADS_UP -> CRITICAL channel (Bypass DND, High Importance)
        // SILENT -> SILENT channel (Low Importance, no sound)
        if (contextDecision.mode === 'HEADS_UP') {
            channelId = CHANNEL_IDS.CRITICAL;
        } else if (contextDecision.mode === 'SILENT') {
            channelId = CHANNEL_IDS.SILENT;
        }

        // 4. Execute Notifee Call
        await notifee.displayNotification({
            id: alert.id,
            title: alert.title,
            body: alert.description,
            android: {
                channelId,
                // Ensure the timestamp matches the alert
                timestamp: alert.timestamp, // ms
                showTimestamp: true,
                pressAction: {
                    id: 'default',
                    launchActivity: 'default',
                },
            },
        });
    } catch (error) {
        // 5. Safety Guard: No Retries, No Crashes
        // If Notifee fails (e.g. invalid channel), we log and swallow.
        console.error('[ForegroundNotifier] Failed to display notification:', error);
    }
}
