import { MeshPayload } from './meshTypes';
import { Alert, AlertSource, HazardLevel } from '../../types/alerts';
import { useAlertStore } from '../../store/alertStore';

/**
 * Entry point for receiving a raw mesh payload from the Bluetooth layer.
 * This function will handle initial validation and dispatch processing.
 *
 * @param payload - The raw data packet received from another device
 * @returns void - Logic will handle state updates internally
 */
export const receiveMeshPayload = (payload: MeshPayload): void => {
    // 1. De-duplication Check
    const existingAlerts = useAlertStore.getState().alerts;
    if (existingAlerts.some((a) => a.id === payload.id)) {
        // FLOOD PROTECTION: Mesh networks often "flood" messages to ensure delivery.
        // We have already processed this exact alert ID.
        // Silently drop it to prevent store duplication and user notification spam.
        return;
    }

    const alert = normalizeMeshPayload(payload);

    if (alert) {
        // Dispatch to global store
        // access getState() to use outside of React components
        useAlertStore.getState().addAlert(alert);
    }

    // TODO: In the future, relay logic will be triggered here
    // Verify TTL > 0 and decrement before rebroadcasting
    // if (shouldRelayPayload(payload)) { ... }
};

/**
 * Transforms a raw MeshPayload into a standardized Alert
 * that the application can consume and display.
 *
 * @param payload - The raw mesh payload containing unknown alertData
 * @returns Alert | null - The valid alert object or null if parsing fails
 */
export const normalizeMeshPayload = (payload: MeshPayload): Alert | null => {
    try {
        const { alertData, id, createdAt } = payload;

        // Defensive check: ensure alertData is an object
        if (typeof alertData !== 'object' || alertData === null) {
            return null;
        }

        // Cast to expected shape (defensively)
        const data = alertData as Partial<Alert>;

        // Required field validation
        if (!data.title || !data.description || !data.hazardLevel || !data.location) {
            return null;
        }

        // Defensive Location Checks
        const lat = Number(data.location.latitude);
        const lon = Number(data.location.longitude);
        const rad = Number(data.location.radius);

        // Ensure numbers are finite and valid
        if (!Number.isFinite(lat) || !Number.isFinite(lon) || !Number.isFinite(rad)) {
            return null;
        }

        // Construct valid Alert object
        const alert: Alert = {
            id: id, // Use mesh ID for alert ID to ensure consistency
            source: AlertSource.BLUETOOTH, // Always forced source
            hazardLevel: [
                HazardLevel.LOW,
                HazardLevel.MODERATE,
                HazardLevel.SEVERE,
                HazardLevel.CRITICAL,
            ].includes(data.hazardLevel as HazardLevel)
                ? (data.hazardLevel as HazardLevel)
                : HazardLevel.LOW, // Fallback safely
            location: {
                latitude: lat,
                longitude: lon,
                radius: rad,
            },
            title: String(data.title).substring(0, 100), // Sanitize length
            description: String(data.description).substring(0, 500),
            timestamp: createdAt, // Use mesh creation time
            expiresAt: createdAt + 1000 * 60 * 60 * 24, // Default 24h expiry if not set
            verified: false, // Bluetooth alerts are always unverified by default
        };

        return alert;
    } catch (error) {
        console.error('Failed to normalize mesh payload:', error);
        return null;
    }
};

/**
 * Determines if a received payload should be relayed to other devices.
 * Checks Time-To-Live (TTL).
 *
 * @param payload - The received mesh payload
 * @returns boolean - True if the message should be rebroadcasted
 */
export const shouldRelayPayload = (payload: MeshPayload): boolean => {
    // Basic TTL check
    if (typeof payload.ttl === 'number' && payload.ttl > 0) {
        return true;
    }
    return false;
};
