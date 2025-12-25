import notifee, { AndroidImportance } from '@notifee/react-native';
import { Platform } from 'react-native';

export const CHANNEL_IDS = {
    CRITICAL: 'rahat_critical_features',
    HIGH: 'rahat_high_features',
    SILENT: 'rahat_silent_features',
};

/**
 * Creates the necessary Android Notification Channels for the app.
 * This function is safe to call multiple times (channels are upserted).
 */
export async function setupNotificationChannels(): Promise<void> {
    // 1. Guard against non-Android platforms
    if (Platform.OS !== 'android') {
        return;
    }

    // 2. Critical Channel (Alarm Sound, High Importance)
    await notifee.createChannel({
        id: CHANNEL_IDS.CRITICAL,
        name: 'Critical Alerts',
        description: 'Life-safety warnings like Earthquakes or Dam Failures',
        importance: AndroidImportance.HIGH,
        sound: 'default', // TODO: Custom alarm sound in future
        vibration: true,
        bypassDnd: true, // Requires special permission, request later
    });

    // 3. High Channel (Default Sound, Default Importance)
    await notifee.createChannel({
        id: CHANNEL_IDS.HIGH,
        name: 'Severe Alerts',
        description: 'Important warnings like Flash Floods',
        importance: AndroidImportance.DEFAULT,
        sound: 'default',
        vibration: true,
    });

    // 4. Silent Channel (No Sound, Low Importance)
    await notifee.createChannel({
        id: CHANNEL_IDS.SILENT,
        name: 'Silent Updates',
        description: 'Non-urgent updates and active monitoring',
        importance: AndroidImportance.LOW,
        sound: undefined,
        vibration: false,
    });
}
