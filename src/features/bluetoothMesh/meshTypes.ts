/**
 * Represents the raw payload transmitted over the Bluetooth Mesh network.
 * This structure is optimized for P2P transmission.
 */
export interface MeshPayload {
    /**
     * Unique identifier for the mesh message (UUID v4).
     * This ID maps directly to the global Alert 'id' to ensure consistency across devices.
     */
    id: string;
    /** ID of the device that originally generated this message */
    originDeviceId: string;
    /** Time To Live: Remaining hops this message can take */
    ttl: number;
    /** Unix timestamp when the message was created */
    createdAt: number;
    /** Raw alert data (to be parsed/normalized) */
    alertData: unknown;
}

/**
 * Represents a normalized alert structure for the application.
 * This matches the global Alert contract used in the app's store/UI.
 *
 * Mapping Strategy:
 * - id -> Unique Alert ID
 * - type -> 'critical', 'warning', 'info'
 * - title -> Short summary
 * - description -> Detailed info
 * - level -> Severity level (1-5)
 * - source -> 'bluetooth'
 * - timestamp -> Creation time
 */
export interface NormalizedMeshAlert {
    id: string;
    type: 'critical' | 'warning' | 'info';
    title: string;
    description: string;
    level: number;
    source: 'bluetooth';
    timestamp: number;
}
