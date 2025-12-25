import notifee from '@notifee/react-native';
import { Platform } from 'react-native';
import { Alert } from '../../types/alerts';
import { DeliveryDecision } from '../../features/alerts/delivery/deliveryPolicy';
import { DeliveryContextDecision } from '../../features/alerts/delivery/deliveryContext';
import { CHANNEL_IDS } from './notificationChannels';

/**
 * Executes a notification while the app is in the BACKGROUND or KILLED.
 *
 * RULES:
 * 1. Headless Safe: No UI refs, no Hooks, no Stores.
 * 2. Idempotent: Can be called multiple times without side effects (Notifee upserts by ID).
 * 3. Minimal Logic: Decisions must be pre-calculated.
 */
export async function notifyInBackground(
    alert: Alert,
    _deliveryDecision: DeliveryDecision,
    contextDecision: DeliveryContextDecision
): Promise<void> {
    // 1. Safety Guard: Android Only
    if (Platform.OS !== 'android') {
        return;
    }

    // 2. Logic: Check specific context decision
    // If logic says "NONE" (e.g. Low hazard in background), we abort.
    if (contextDecision.mode === 'NONE') {
        return;
    }

    try {
        let channelId = CHANNEL_IDS.SILENT;

        // Map decision mode to Channel
        // HEADS_UP -> CRITICAL channel (High Importance, Sound)
        // SILENT -> SILENT channel (Low Importance)
        if (contextDecision.mode === 'HEADS_UP') {
            channelId = CHANNEL_IDS.CRITICAL;
        } else if (contextDecision.mode === 'SILENT') {
            channelId = CHANNEL_IDS.SILENT;
        }

        // 3. Execute Notifee Call
        // Note: In background, we rely heavily on the Channel config (Sound/Vibration).
        await notifee.displayNotification({
            id: alert.id,
            title: alert.title,
            body: alert.description,
            data: {
                // serialized data for re-hydration if user taps
                alert: JSON.stringify(alert),
                contextMode: contextDecision.mode,
            },
            android: {
                channelId,
                timestamp: alert.timestamp, // ms
                showTimestamp: true,
                pressAction: {
                    id: 'default',
                    launchActivity: 'default',
                },
                // Background specific: Ensure we don't wake screen if SILENT
                ongoing: contextDecision.mode === 'HEADS_UP', // Pin high priority alerts? Maybe too aggressive.
                // improved: autoCancel default is true.
            },
        });
    } catch (error) {
        // 4. Safety Guard: Swallow errors to prevent native crashes in headless mode
        console.error('[BackgroundNotifier] Failed to display notification:', error);
    }
}
