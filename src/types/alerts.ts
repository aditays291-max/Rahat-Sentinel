export enum AlertSource {
    CELL = 'CELL',
    BLUETOOTH = 'BLUETOOTH',
    SATELLITE = 'SATELLITE',
}

export enum HazardLevel {
    LOW = 'LOW',
    MODERATE = 'MODERATE',
    SEVERE = 'SEVERE',
    CRITICAL = 'CRITICAL',
}

export interface AlertLocation {
    latitude: number;
    longitude: number;
    radius: number; // in meters
}

export interface Alert {
    id: string;
    source: AlertSource;
    hazardLevel: HazardLevel;
    location: AlertLocation;
    title: string;
    description: string;
    timestamp: number;
    expiresAt: number;
    verified: boolean;
}
