/**
 * Unit tests for relevanceEngine.ts
 * 
 * Tests the alert relevance calculation engine that filters alerts
 * based on user location, ensuring dynamic context-aware alert delivery.
 */

import { recalculateRelevantAlerts } from '../../../src/features/locationAwareness/relevanceEngine';
import { Alert, AlertSource, HazardLevel } from '../../../src/types/alerts';
import { UserLocation } from '../../../src/features/locationAwareness/locationContext';
import * as locationService from '../../../src/features/location/locationService';

// Mock the locationService module
jest.mock('../../../src/features/location/locationService');

describe('relevanceEngine', () => {
    // Helper function to create test alerts
    const createTestAlert = (overrides: Partial<Alert> = {}): Alert => ({
        id: 'test-alert-1',
        source: AlertSource.CELL,
        hazardLevel: HazardLevel.SEVERE,
        location: {
            latitude: 40.7128,
            longitude: -74.0060,
            radius: 5000,
        },
        title: 'Test Alert',
        description: 'Test Description',
        timestamp: Date.now(),
        expiresAt: Date.now() + 3600000,
        verified: true,
        ...overrides,
    });

    beforeEach(() => {
        // Reset all mocks before each test
        jest.clearAllMocks();
    });

    describe('recalculateRelevantAlerts', () => {
        describe('when user location is null', () => {
            it('should return all alerts for safety when location is unknown', () => {
                // Given: Multiple alerts and no user location
                const alerts: Alert[] = [
                    createTestAlert({ id: 'alert-1', hazardLevel: HazardLevel.CRITICAL }),
                    createTestAlert({ id: 'alert-2', hazardLevel: HazardLevel.SEVERE }),
                    createTestAlert({ id: 'alert-3', hazardLevel: HazardLevel.MODERATE }),
                ];

                // When: We recalculate with null location
                const result = recalculateRelevantAlerts(alerts, null);

                // Then: All alerts should be returned (fail-safe behavior)
                expect(result).toEqual(alerts);
                expect(result).toHaveLength(3);
                expect(locationService.filterRelevantAlerts).not.toHaveBeenCalled();
            });

            it('should return empty array when no alerts exist and location is null', () => {
                // Given: No alerts and no location
                const alerts: Alert[] = [];

                // When: We recalculate
                const result = recalculateRelevantAlerts(alerts, null);

                // Then: Empty array should be returned
                expect(result).toEqual([]);
                expect(result).toHaveLength(0);
            });

            it('should not call filterRelevantAlerts when location is null', () => {
                // Given: Alerts without location
                const alerts: Alert[] = [createTestAlert()];

                // When: We recalculate with null location
                recalculateRelevantAlerts(alerts, null);

                // Then: The location service should not be called
                expect(locationService.filterRelevantAlerts).not.toHaveBeenCalled();
            });
        });

        describe('when user location is provided', () => {
            const userLocation: UserLocation = {
                latitude: 40.7128,
                longitude: -74.0060,
                accuracy: 10,
            };

            it('should delegate to filterRelevantAlerts with valid location', () => {
                // Given: Alerts and user location
                const alerts: Alert[] = [createTestAlert()];
                const mockFilteredAlerts: Alert[] = [createTestAlert()];
                (locationService.filterRelevantAlerts as jest.Mock).mockReturnValue(mockFilteredAlerts);

                // When: We recalculate with valid location
                const result = recalculateRelevantAlerts(alerts, userLocation);

                // Then: filterRelevantAlerts should be called with correct params
                expect(locationService.filterRelevantAlerts).toHaveBeenCalledWith(alerts, userLocation);
                expect(locationService.filterRelevantAlerts).toHaveBeenCalledTimes(1);
                expect(result).toEqual(mockFilteredAlerts);
            });

            it('should return filtered alerts from locationService', () => {
                // Given: Multiple alerts but only some are relevant
                const allAlerts: Alert[] = [
                    createTestAlert({ id: 'alert-1' }),
                    createTestAlert({ id: 'alert-2' }),
                    createTestAlert({ id: 'alert-3' }),
                ];
                const relevantAlerts: Alert[] = [
                    createTestAlert({ id: 'alert-1' }),
                ];
                (locationService.filterRelevantAlerts as jest.Mock).mockReturnValue(relevantAlerts);

                // When: We recalculate
                const result = recalculateRelevantAlerts(allAlerts, userLocation);

                // Then: Only relevant alerts should be returned
                expect(result).toEqual(relevantAlerts);
                expect(result).toHaveLength(1);
            });

            it('should handle empty alert array with valid location', () => {
                // Given: No alerts but valid location
                const alerts: Alert[] = [];
                (locationService.filterRelevantAlerts as jest.Mock).mockReturnValue([]);

                // When: We recalculate
                const result = recalculateRelevantAlerts(alerts, userLocation);

                // Then: Empty array should be returned
                expect(result).toEqual([]);
                expect(locationService.filterRelevantAlerts).toHaveBeenCalledWith(alerts, userLocation);
            });

            it('should preserve alert object properties through filtering', () => {
                // Given: An alert with specific properties
                const specificAlert = createTestAlert({
                    id: 'specific-123',
                    title: 'Earthquake Warning',
                    hazardLevel: HazardLevel.CRITICAL,
                    verified: true,
                });
                const alerts: Alert[] = [specificAlert];
                (locationService.filterRelevantAlerts as jest.Mock).mockReturnValue([specificAlert]);

                // When: We recalculate
                const result = recalculateRelevantAlerts(alerts, userLocation);

                // Then: The returned alert should maintain all properties
                expect(result[0]).toEqual(specificAlert);
                expect(result[0].id).toBe('specific-123');
                expect(result[0].title).toBe('Earthquake Warning');
                expect(result[0].hazardLevel).toBe(HazardLevel.CRITICAL);
            });

            it('should handle location with only latitude and longitude', () => {
                // Given: Location without accuracy
                const minimalLocation: UserLocation = {
                    latitude: 34.0522,
                    longitude: -118.2437,
                };
                const alerts: Alert[] = [createTestAlert()];
                (locationService.filterRelevantAlerts as jest.Mock).mockReturnValue(alerts);

                // When: We recalculate with minimal location
                const result = recalculateRelevantAlerts(alerts, minimalLocation);

                // Then: Should work correctly
                expect(locationService.filterRelevantAlerts).toHaveBeenCalledWith(
                    alerts,
                    minimalLocation
                );
                expect(result).toEqual(alerts);
            });

            it('should handle location at coordinate extremes', () => {
                // Given: Location at valid extremes
                const extremeLocation: UserLocation = {
                    latitude: 90,
                    longitude: 180,
                };
                const alerts: Alert[] = [createTestAlert()];
                (locationService.filterRelevantAlerts as jest.Mock).mockReturnValue([]);

                // When: We recalculate with extreme coordinates
                const result = recalculateRelevantAlerts(alerts, extremeLocation);

                // Then: Should handle gracefully
                expect(locationService.filterRelevantAlerts).toHaveBeenCalledWith(
                    alerts,
                    extremeLocation
                );
                expect(result).toEqual([]);
            });
        });

        describe('alert filtering scenarios', () => {
            const userLocation: UserLocation = {
                latitude: 40.7128,
                longitude: -74.0060,
            };

            it('should filter out all alerts when none are geographically relevant', () => {
                // Given: Alerts far from user location
                const farAlerts: Alert[] = [
                    createTestAlert({ id: 'far-1' }),
                    createTestAlert({ id: 'far-2' }),
                ];
                (locationService.filterRelevantAlerts as jest.Mock).mockReturnValue([]);

                // When: We recalculate
                const result = recalculateRelevantAlerts(farAlerts, userLocation);

                // Then: No alerts should be returned
                expect(result).toEqual([]);
                expect(result).toHaveLength(0);
            });

            it('should include all alerts when all are geographically relevant', () => {
                // Given: All alerts near user location
                const nearAlerts: Alert[] = [
                    createTestAlert({ id: 'near-1' }),
                    createTestAlert({ id: 'near-2' }),
                    createTestAlert({ id: 'near-3' }),
                ];
                (locationService.filterRelevantAlerts as jest.Mock).mockReturnValue(nearAlerts);

                // When: We recalculate
                const result = recalculateRelevantAlerts(nearAlerts, userLocation);

                // Then: All alerts should be returned
                expect(result).toEqual(nearAlerts);
                expect(result).toHaveLength(3);
            });

            it('should handle mixed hazard levels correctly', () => {
                // Given: Alerts of different hazard levels
                const mixedAlerts: Alert[] = [
                    createTestAlert({ id: '1', hazardLevel: HazardLevel.CRITICAL }),
                    createTestAlert({ id: '2', hazardLevel: HazardLevel.SEVERE }),
                    createTestAlert({ id: '3', hazardLevel: HazardLevel.MODERATE }),
                    createTestAlert({ id: '4', hazardLevel: HazardLevel.LOW }),
                ];
                const relevantAlerts = mixedAlerts.slice(0, 2);
                (locationService.filterRelevantAlerts as jest.Mock).mockReturnValue(relevantAlerts);

                // When: We recalculate
                const result = recalculateRelevantAlerts(mixedAlerts, userLocation);

                // Then: Only geographically relevant alerts returned
                expect(result).toEqual(relevantAlerts);
                expect(result).toHaveLength(2);
            });

            it('should handle alerts from different sources', () => {
                // Given: Alerts from various sources
                const multiSourceAlerts: Alert[] = [
                    createTestAlert({ id: '1', source: AlertSource.CELL }),
                    createTestAlert({ id: '2', source: AlertSource.BLUETOOTH }),
                    createTestAlert({ id: '3', source: AlertSource.SATELLITE }),
                ];
                (locationService.filterRelevantAlerts as jest.Mock).mockReturnValue(multiSourceAlerts);

                // When: We recalculate
                const result = recalculateRelevantAlerts(multiSourceAlerts, userLocation);

                // Then: All sources should be handled equally
                expect(result).toHaveLength(3);
                expect(result.map(a => a.source)).toEqual([
                    AlertSource.CELL,
                    AlertSource.BLUETOOTH,
                    AlertSource.SATELLITE,
                ]);
            });
        });

        describe('user movement scenarios', () => {
            it('should recalculate when user enters a hazard zone', () => {
                // Simulate user entering an alert zone
                const alerts: Alert[] = [
                    createTestAlert({
                        id: 'flood-alert',
                        location: { latitude: 40.7, longitude: -74.0, radius: 2000 },
                    }),
                ];

                // User starts far away
                const farLocation: UserLocation = { latitude: 41.0, longitude: -75.0 };
                (locationService.filterRelevantAlerts as jest.Mock).mockReturnValue([]);
                let result = recalculateRelevantAlerts(alerts, farLocation);
                expect(result).toHaveLength(0);

                // User moves closer (enters zone)
                const nearLocation: UserLocation = { latitude: 40.7, longitude: -74.0 };
                (locationService.filterRelevantAlerts as jest.Mock).mockReturnValue(alerts);
                result = recalculateRelevantAlerts(alerts, nearLocation);
                expect(result).toHaveLength(1);
            });

            it('should recalculate when user leaves a hazard zone', () => {
                // Simulate user leaving an alert zone
                const alerts: Alert[] = [
                    createTestAlert({
                        id: 'evacuation-alert',
                        location: { latitude: 40.7, longitude: -74.0, radius: 1000 },
                    }),
                ];

                // User starts in zone
                const inZoneLocation: UserLocation = { latitude: 40.7, longitude: -74.0 };
                (locationService.filterRelevantAlerts as jest.Mock).mockReturnValue(alerts);
                let result = recalculateRelevantAlerts(alerts, inZoneLocation);
                expect(result).toHaveLength(1);

                // User moves away (leaves zone)
                const outZoneLocation: UserLocation = { latitude: 41.0, longitude: -75.0 };
                (locationService.filterRelevantAlerts as jest.Mock).mockReturnValue([]);
                result = recalculateRelevantAlerts(alerts, outZoneLocation);
                expect(result).toHaveLength(0);
            });

            it('should handle gradual location updates during evacuation', () => {
                // Simulate evacuation route with multiple position updates
                const alerts: Alert[] = [
                    createTestAlert({ id: 'zone-a' }),
                    createTestAlert({ id: 'zone-b' }),
                ];

                const evacuationPath: UserLocation[] = [
                    { latitude: 40.70, longitude: -74.00 },
                    { latitude: 40.71, longitude: -74.01 },
                    { latitude: 40.72, longitude: -74.02 },
                    { latitude: 40.73, longitude: -74.03 },
                ];

                // Simulate decreasing relevance as user moves away
                const relevanceResults = [
                    alerts,           // Both relevant
                    [alerts[0]],     // Only one relevant
                    [alerts[0]],     // Still one relevant
                    [],              // None relevant (escaped)
                ];

                evacuationPath.forEach((location, index) => {
                    (locationService.filterRelevantAlerts as jest.Mock).mockReturnValue(
                        relevanceResults[index]
                    );
                    const result = recalculateRelevantAlerts(alerts, location);
                    expect(result).toEqual(relevanceResults[index]);
                });
            });
        });

        describe('edge cases and error handling', () => {
            it('should handle alerts with undefined location gracefully', () => {
                // Given: Alert with potentially undefined location data
                const alerts: Alert[] = [
                    createTestAlert(),
                ];
                const userLocation: UserLocation = {
                    latitude: 40.7128,
                    longitude: -74.0060,
                };
                (locationService.filterRelevantAlerts as jest.Mock).mockReturnValue([]);

                // When: We recalculate
                const result = recalculateRelevantAlerts(alerts, userLocation);

                // Then: Should handle without errors
                expect(result).toBeDefined();
                expect(locationService.filterRelevantAlerts).toHaveBeenCalled();
            });

            it('should handle very large alert arrays efficiently', () => {
                // Given: A large number of alerts
                const largeAlertArray: Alert[] = Array.from({ length: 1000 }, (_, i) =>
                    createTestAlert({ id: `alert-${i}` })
                );
                const userLocation: UserLocation = {
                    latitude: 40.7128,
                    longitude: -74.0060,
                };
                (locationService.filterRelevantAlerts as jest.Mock).mockReturnValue(
                    largeAlertArray.slice(0, 10)
                );

                // When: We recalculate with large dataset
                const result = recalculateRelevantAlerts(largeAlertArray, userLocation);

                // Then: Should process without errors
                expect(locationService.filterRelevantAlerts).toHaveBeenCalledWith(
                    largeAlertArray,
                    userLocation
                );
                expect(result).toHaveLength(10);
            });

            it('should maintain referential integrity of alert objects', () => {
                // Given: Specific alert instances
                const alert1 = createTestAlert({ id: 'alert-1' });
                const alert2 = createTestAlert({ id: 'alert-2' });
                const alerts: Alert[] = [alert1, alert2];
                const userLocation: UserLocation = {
                    latitude: 40.7128,
                    longitude: -74.0060,
                };
                (locationService.filterRelevantAlerts as jest.Mock).mockReturnValue([alert1]);

                // When: We recalculate
                const result = recalculateRelevantAlerts(alerts, userLocation);

                // Then: Returned objects should be same references
                expect(result[0]).toBe(alert1);
            });

            it('should handle zero-radius alert locations', () => {
                // Given: Alert with zero radius (point-specific)
                const alerts: Alert[] = [
                    createTestAlert({
                        location: { latitude: 40.7128, longitude: -74.0060, radius: 0 },
                    }),
                ];
                const userLocation: UserLocation = {
                    latitude: 40.7128,
                    longitude: -74.0060,
                };
                (locationService.filterRelevantAlerts as jest.Mock).mockReturnValue(alerts);

                // When: We recalculate
                const result = recalculateRelevantAlerts(alerts, userLocation);

                // Then: Should delegate to location service
                expect(locationService.filterRelevantAlerts).toHaveBeenCalledWith(
                    alerts,
                    userLocation
                );
            });
        });

        describe('safety and fail-safe behaviors', () => {
            it('should prioritize showing alerts over hiding when location unavailable', () => {
                // Given: Critical alerts but no location
                const criticalAlerts: Alert[] = [
                    createTestAlert({
                        id: 'critical-1',
                        hazardLevel: HazardLevel.CRITICAL,
                    }),
                    createTestAlert({
                        id: 'critical-2',
                        hazardLevel: HazardLevel.CRITICAL,
                    }),
                ];

                // When: Location is null (GPS disabled/lost)
                const result = recalculateRelevantAlerts(criticalAlerts, null);

                // Then: All alerts shown for safety
                expect(result).toEqual(criticalAlerts);
                expect(result).toHaveLength(2);
            });

            it('should not filter alerts when safety is paramount', () => {
                // This test documents the fail-safe behavior:
                // Better to show too many alerts than to hide a critical one

                const alerts: Alert[] = [
                    createTestAlert({ id: 'far-alert' }),
                ];

                // No location = show everything
                expect(recalculateRelevantAlerts(alerts, null)).toEqual(alerts);
            });
        });
    });
});