import { RawCellBroadcastMessage } from './cellBroadcast.types';
import { Alert, AlertSource, HazardLevel } from '../../types/alerts';

export class CellBroadcastService {
    private static readonly DEFAULT_DURATION_MS = 3600000; // 1 hour

    /**
     * Normalizes a raw cell broadcast message into a canonical Alert object.
     * This function is pure and does not produce side effects.
     */
    public normalizeCellBroadcast(raw: RawCellBroadcastMessage): Alert {
        return {
            id: raw.messageId,
            source: AlertSource.CELL,
            hazardLevel: this.mapSeverityToHazardLevel(raw.severity),
            location: {
                latitude: raw.latitude ?? 0,
                longitude: raw.longitude ?? 0,
                radius: raw.radius ?? 0,
            },
            title: raw.title,
            description: raw.body,
            timestamp: raw.timestamp,
            expiresAt: raw.timestamp + CellBroadcastService.DEFAULT_DURATION_MS,
            verified: true, // CB messages are trusted by definition
        };
    }

    private mapSeverityToHazardLevel(severity: string | number): HazardLevel {
        // Convert to lowercase string for easier matching if it's a string, or handle numeric codes.
        // Common CB severities: 0-3, or "Extreme", "Severe", etc.
        const s = severity.toString().toLowerCase();

        if (s.includes('extreme') || s === '3') {
            return HazardLevel.CRITICAL;
        }
        if (s.includes('severe') || s === '2') {
            return HazardLevel.SEVERE;
        }
        if (s.includes('moderate') || s === '1') {
            return HazardLevel.MODERATE;
        }
        // Default fallback
        return HazardLevel.LOW;
    }
}
