import { Alert, AlertSource, HazardLevel } from '../types/alerts';

/**
 * Demo alerts for development and hackathon demonstrations.
 * These are realistic alerts based on Nepal's geography and disaster scenarios.
 */

const now = Date.now();
const oneHour = 60 * 60 * 1000;

export const DEMO_ALERTS: Alert[] = [
    {
        id: 'demo-earthquake-ktm-001',
        source: AlertSource.CELL,
        hazardLevel: HazardLevel.CRITICAL,
        title: 'Earthquake Alert - Kathmandu Valley',
        description: 'A magnitude 6.2 earthquake has been detected near Kathmandu. Seek open ground immediately. Stay away from buildings and power lines. Aftershocks expected.',
        location: {
            latitude: 27.7172,
            longitude: 85.3240,
            radius: 25000, // 25km radius
        },
        timestamp: now - 5 * 60 * 1000, // 5 minutes ago
        expiresAt: now + 2 * oneHour,
        verified: true,
    },
    {
        id: 'demo-flood-bagmati-002',
        source: AlertSource.CELL,
        hazardLevel: HazardLevel.SEVERE,
        title: 'Flash Flood Warning - Bagmati River',
        description: 'Heavy rainfall has caused rapid water level rise in Bagmati River. Residents in low-lying areas should evacuate immediately. Avoid crossing bridges.',
        location: {
            latitude: 27.6915,
            longitude: 85.3158,
            radius: 15000, // 15km radius
        },
        timestamp: now - 15 * 60 * 1000, // 15 minutes ago
        expiresAt: now + 4 * oneHour,
        verified: true,
    },
    {
        id: 'demo-weather-pokhara-003',
        source: AlertSource.CELL,
        hazardLevel: HazardLevel.LOW,
        title: 'Weather Advisory - Pokhara Region',
        description: 'Moderate rainfall expected in Pokhara and surrounding areas over the next 6 hours. Carry umbrellas and exercise caution on roads.',
        location: {
            latitude: 28.2096,
            longitude: 83.9856,
            radius: 20000, // 20km radius
        },
        timestamp: now - 30 * 60 * 1000, // 30 minutes ago
        expiresAt: now + 6 * oneHour,
        verified: true,
    },
];
