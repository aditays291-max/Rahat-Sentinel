/**
 * Unit tests for locationContext.ts
 * 
 * Tests the in-memory location storage module that provides separation
 * between GPS data source and application state management.
 */

import {
    UserLocation,
    getCurrentUserLocation,
    updateUserLocation,
} from '../../../src/features/locationAwareness/locationContext';

describe('locationContext', () => {
    describe('getCurrentUserLocation', () => {
        it('should return null initially when no location has been set', () => {
            // Given: No location has been updated yet
            // When: We query the current location
            const location = getCurrentUserLocation();

            // Then: It should return null
            expect(location).toBeNull();
        });

        it('should return the most recently updated location', () => {
            // Given: A valid location
            const testLocation: UserLocation = {
                latitude: 40.7128,
                longitude: -74.0060,
                accuracy: 10,
            };

            // When: We update and then retrieve the location
            updateUserLocation(testLocation);
            const retrievedLocation = getCurrentUserLocation();

            // Then: It should return the same location
            expect(retrievedLocation).toEqual(testLocation);
        });

        it('should return updated location after multiple updates', () => {
            // Given: Multiple location updates
            const firstLocation: UserLocation = {
                latitude: 40.7128,
                longitude: -74.0060,
            };
            const secondLocation: UserLocation = {
                latitude: 34.0522,
                longitude: -118.2437,
            };

            // When: We update location twice
            updateUserLocation(firstLocation);
            updateUserLocation(secondLocation);
            const currentLocation = getCurrentUserLocation();

            // Then: It should return the most recent location
            expect(currentLocation).toEqual(secondLocation);
        });
    });

    describe('updateUserLocation', () => {
        it('should successfully update location with valid coordinates', () => {
            // Given: Valid latitude and longitude
            const validLocation: UserLocation = {
                latitude: 51.5074,
                longitude: -0.1278,
            };

            // When: We update the location
            updateUserLocation(validLocation);

            // Then: The location should be stored
            expect(getCurrentUserLocation()).toEqual(validLocation);
        });

        it('should store location with accuracy when provided', () => {
            // Given: A location with accuracy information
            const locationWithAccuracy: UserLocation = {
                latitude: 48.8566,
                longitude: 2.3522,
                accuracy: 5.5,
            };

            // When: We update the location
            updateUserLocation(locationWithAccuracy);

            // Then: All properties should be preserved
            const stored = getCurrentUserLocation();
            expect(stored).toEqual(locationWithAccuracy);
            expect(stored?.accuracy).toBe(5.5);
        });

        it('should handle zero coordinates as valid', () => {
            // Given: Location at (0, 0) - valid equator/prime meridian
            const zeroLocation: UserLocation = {
                latitude: 0,
                longitude: 0,
            };

            // When: We update with zero coordinates
            updateUserLocation(zeroLocation);

            // Then: The location should be accepted
            expect(getCurrentUserLocation()).toEqual(zeroLocation);
        });

        it('should handle extreme valid latitude values', () => {
            // Given: Locations at valid latitude extremes
            const northPole: UserLocation = {
                latitude: 90,
                longitude: 0,
            };
            const southPole: UserLocation = {
                latitude: -90,
                longitude: 0,
            };

            // When: We update with extreme latitudes
            updateUserLocation(northPole);
            expect(getCurrentUserLocation()).toEqual(northPole);

            updateUserLocation(southPole);
            expect(getCurrentUserLocation()).toEqual(southPole);
        });

        it('should handle extreme valid longitude values', () => {
            // Given: Locations at valid longitude extremes
            const eastExtreme: UserLocation = {
                latitude: 0,
                longitude: 180,
            };
            const westExtreme: UserLocation = {
                latitude: 0,
                longitude: -180,
            };

            // When: We update with extreme longitudes
            updateUserLocation(eastExtreme);
            expect(getCurrentUserLocation()).toEqual(eastExtreme);

            updateUserLocation(westExtreme);
            expect(getCurrentUserLocation()).toEqual(westExtreme);
        });

        it('should reject null location object', () => {
            // Given: A valid initial location
            const initialLocation: UserLocation = {
                latitude: 10,
                longitude: 20,
            };
            updateUserLocation(initialLocation);

            // When: We try to update with null
            updateUserLocation(null as any);

            // Then: The previous location should remain unchanged
            expect(getCurrentUserLocation()).toEqual(initialLocation);
        });

        it('should reject undefined location object', () => {
            // Given: A valid initial location
            const initialLocation: UserLocation = {
                latitude: 15,
                longitude: 25,
            };
            updateUserLocation(initialLocation);

            // When: We try to update with undefined
            updateUserLocation(undefined as any);

            // Then: The previous location should remain unchanged
            expect(getCurrentUserLocation()).toEqual(initialLocation);
        });

        it('should reject location with non-number latitude', () => {
            // Given: An initial valid location
            const validLocation: UserLocation = {
                latitude: 30,
                longitude: 40,
            };
            updateUserLocation(validLocation);

            // When: We try to update with invalid latitude type
            const invalidLocation = {
                latitude: '30' as any,
                longitude: 40,
            };
            updateUserLocation(invalidLocation);

            // Then: The update should be rejected
            expect(getCurrentUserLocation()).toEqual(validLocation);
        });

        it('should reject location with non-number longitude', () => {
            // Given: An initial valid location
            const validLocation: UserLocation = {
                latitude: 35,
                longitude: 45,
            };
            updateUserLocation(validLocation);

            // When: We try to update with invalid longitude type
            const invalidLocation = {
                latitude: 35,
                longitude: '45' as any,
            };
            updateUserLocation(invalidLocation);

            // Then: The update should be rejected
            expect(getCurrentUserLocation()).toEqual(validLocation);
        });

        it('should reject location with missing latitude', () => {
            // Given: An initial valid location
            const validLocation: UserLocation = {
                latitude: 50,
                longitude: 60,
            };
            updateUserLocation(validLocation);

            // When: We try to update with missing latitude
            const invalidLocation = {
                longitude: 60,
            } as any;
            updateUserLocation(invalidLocation);

            // Then: The update should be rejected
            expect(getCurrentUserLocation()).toEqual(validLocation);
        });

        it('should reject location with missing longitude', () => {
            // Given: An initial valid location
            const validLocation: UserLocation = {
                latitude: 55,
                longitude: 65,
            };
            updateUserLocation(validLocation);

            // When: We try to update with missing longitude
            const invalidLocation = {
                latitude: 55,
            } as any;
            updateUserLocation(invalidLocation);

            // Then: The update should be rejected
            expect(getCurrentUserLocation()).toEqual(validLocation);
        });

        it('should reject location with NaN latitude', () => {
            // Given: An initial valid location
            const validLocation: UserLocation = {
                latitude: 60,
                longitude: 70,
            };
            updateUserLocation(validLocation);

            // When: We try to update with NaN latitude
            const invalidLocation: UserLocation = {
                latitude: NaN,
                longitude: 70,
            };
            updateUserLocation(invalidLocation);

            // Then: The update should be rejected
            expect(getCurrentUserLocation()).toEqual(validLocation);
        });

        it('should reject location with NaN longitude', () => {
            // Given: An initial valid location
            const validLocation: UserLocation = {
                latitude: 65,
                longitude: 75,
            };
            updateUserLocation(validLocation);

            // When: We try to update with NaN longitude
            const invalidLocation: UserLocation = {
                latitude: 65,
                longitude: NaN,
            };
            updateUserLocation(invalidLocation);

            // Then: The update should be rejected
            expect(getCurrentUserLocation()).toEqual(validLocation);
        });

        it('should handle rapid successive updates correctly', () => {
            // Given: Multiple rapid location updates (simulating GPS)
            const locations: UserLocation[] = [
                { latitude: 1, longitude: 1 },
                { latitude: 1.001, longitude: 1.001 },
                { latitude: 1.002, longitude: 1.002 },
                { latitude: 1.003, longitude: 1.003 },
                { latitude: 1.004, longitude: 1.004 },
            ];

            // When: We rapidly update locations
            locations.forEach(loc => updateUserLocation(loc));

            // Then: The last location should be stored
            expect(getCurrentUserLocation()).toEqual(locations[locations.length - 1]);
        });

        it('should preserve accuracy value through updates', () => {
            // Given: Locations with different accuracy values
            const highAccuracy: UserLocation = {
                latitude: 10,
                longitude: 20,
                accuracy: 3,
            };
            const lowAccuracy: UserLocation = {
                latitude: 11,
                longitude: 21,
                accuracy: 50,
            };

            // When: We update with different accuracy values
            updateUserLocation(highAccuracy);
            expect(getCurrentUserLocation()?.accuracy).toBe(3);

            updateUserLocation(lowAccuracy);
            expect(getCurrentUserLocation()?.accuracy).toBe(50);
        });

        it('should handle location without accuracy property', () => {
            // Given: A location without accuracy
            const locationNoAccuracy: UserLocation = {
                latitude: 20,
                longitude: 30,
            };

            // When: We update without accuracy
            updateUserLocation(locationNoAccuracy);

            // Then: Location should be stored without accuracy
            const stored = getCurrentUserLocation();
            expect(stored?.latitude).toBe(20);
            expect(stored?.longitude).toBe(30);
            expect(stored?.accuracy).toBeUndefined();
        });

        it('should handle negative coordinates correctly', () => {
            // Given: Locations in southern/western hemispheres
            const negativeLocation: UserLocation = {
                latitude: -33.8688,
                longitude: -151.2093,
            };

            // When: We update with negative coordinates
            updateUserLocation(negativeLocation);

            // Then: Negative values should be preserved
            expect(getCurrentUserLocation()).toEqual(negativeLocation);
        });

        it('should handle decimal precision in coordinates', () => {
            // Given: A location with high decimal precision
            const preciseLocation: UserLocation = {
                latitude: 40.71278934829,
                longitude: -74.00601924821,
                accuracy: 2.5,
            };

            // When: We update with precise coordinates
            updateUserLocation(preciseLocation);

            // Then: Precision should be maintained
            expect(getCurrentUserLocation()).toEqual(preciseLocation);
        });
    });

    describe('integration scenarios', () => {
        it('should handle complete GPS update workflow', () => {
            // Simulate a typical GPS update workflow
            
            // Initial state - no location
            expect(getCurrentUserLocation()).toBeNull();

            // GPS acquires first fix
            const firstFix: UserLocation = {
                latitude: 37.7749,
                longitude: -122.4194,
                accuracy: 100,
            };
            updateUserLocation(firstFix);
            expect(getCurrentUserLocation()).toEqual(firstFix);

            // GPS improves accuracy
            const betterFix: UserLocation = {
                latitude: 37.7749,
                longitude: -122.4194,
                accuracy: 15,
            };
            updateUserLocation(betterFix);
            expect(getCurrentUserLocation()?.accuracy).toBe(15);

            // User moves to new location
            const movedLocation: UserLocation = {
                latitude: 37.7750,
                longitude: -122.4195,
                accuracy: 12,
            };
            updateUserLocation(movedLocation);
            expect(getCurrentUserLocation()).toEqual(movedLocation);
        });

        it('should maintain data integrity under edge case inputs', () => {
            // Test various edge cases in sequence
            const testCases: Array<{location: any, shouldUpdate: boolean}> = [
                { location: { latitude: 0, longitude: 0 }, shouldUpdate: true },
                { location: null, shouldUpdate: false },
                { location: { latitude: '10', longitude: 20 }, shouldUpdate: false },
                { location: { latitude: 90, longitude: 180 }, shouldUpdate: true },
                { location: undefined, shouldUpdate: false },
                { location: { latitude: -90, longitude: -180 }, shouldUpdate: true },
            ];

            let expectedLocation: UserLocation | null = null;

            testCases.forEach(({ location, shouldUpdate }) => {
                updateUserLocation(location);
                if (shouldUpdate) {
                    expectedLocation = location;
                }
                expect(getCurrentUserLocation()).toEqual(expectedLocation);
            });
        });
    });
});