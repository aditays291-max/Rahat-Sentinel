/**
 * Unit tests for backgroundNotifier.ts
 * 
 * Tests the background notification execution module that displays
 * notifications when the app is in background or killed state.
 */

import { Platform } from 'react-native';
import notifee from '@notifee/react-native';
import { notifyInBackground } from '../../../src/platform/android/backgroundNotifier';
import { Alert, AlertSource, HazardLevel } from '../../../src/types/alerts';
import { DeliveryDecision } from '../../../src/features/alerts/delivery/deliveryPolicy';
import { DeliveryContextDecision } from '../../../src/features/alerts/delivery/deliveryContext';
import { CHANNEL_IDS } from '../../../src/platform/android/notificationChannels';

// Mock dependencies
jest.mock('@notifee/react-native');
jest.mock('react-native/Libraries/Utilities/Platform', () => ({
    OS: 'android',
    select: jest.fn((obj) => obj.android),
}));

describe('backgroundNotifier', () => {
    // Helper to create test alert
    const createTestAlert = (overrides: Partial<Alert> = {}): Alert => ({
        id: 'test-alert-123',
        source: AlertSource.CELL,
        hazardLevel: HazardLevel.SEVERE,
        location: {
            latitude: 40.7128,
            longitude: -74.0060,
            radius: 5000,
        },
        title: 'Severe Weather Alert',
        description: 'Flash flood warning in your area',
        timestamp: Date.now(),
        expiresAt: Date.now() + 3600000,
        verified: true,
        ...overrides,
    });

    const createDeliveryDecision = (overrides: Partial<DeliveryDecision> = {}): DeliveryDecision => ({
        notify: true,
        priority: 'HIGH',
        soundProfile: 'ALERT',
        ...overrides,
    });

    const createContextDecision = (
        overrides: Partial<DeliveryContextDecision> = {}
    ): DeliveryContextDecision => ({
        shouldNotify: true,
        mode: 'HEADS_UP',
        ...overrides,
    });

    beforeEach(() => {
        jest.clearAllMocks();
        // Reset Platform.OS to android
        (Platform as any).OS = 'android';
    });

    describe('Platform guards', () => {
        it('should not execute on iOS platform', async () => {
            // Given: iOS platform
            (Platform as any).OS = 'ios';
            const alert = createTestAlert();
            const deliveryDecision = createDeliveryDecision();
            const contextDecision = createContextDecision();

            // When: We attempt to notify in background
            await notifyInBackground(alert, deliveryDecision, contextDecision);

            // Then: Notifee should not be called
            expect(notifee.displayNotification).not.toHaveBeenCalled();
        });

        it('should execute on Android platform', async () => {
            // Given: Android platform
            (Platform as any).OS = 'android';
            const alert = createTestAlert();
            const deliveryDecision = createDeliveryDecision();
            const contextDecision = createContextDecision();

            // When: We notify in background
            await notifyInBackground(alert, deliveryDecision, contextDecision);

            // Then: Notifee should be called
            expect(notifee.displayNotification).toHaveBeenCalled();
        });
    });

    describe('Context decision handling', () => {
        it('should not notify when context decision mode is NONE', async () => {
            // Given: Context decision says NONE
            const alert = createTestAlert();
            const deliveryDecision = createDeliveryDecision();
            const contextDecision = createContextDecision({ mode: 'NONE' });

            // When: We attempt to notify
            await notifyInBackground(alert, deliveryDecision, contextDecision);

            // Then: No notification should be displayed
            expect(notifee.displayNotification).not.toHaveBeenCalled();
        });

        it('should notify when context decision mode is HEADS_UP', async () => {
            // Given: Context decision says HEADS_UP
            const alert = createTestAlert();
            const deliveryDecision = createDeliveryDecision();
            const contextDecision = createContextDecision({ mode: 'HEADS_UP' });

            // When: We notify
            await notifyInBackground(alert, deliveryDecision, contextDecision);

            // Then: Notification should be displayed
            expect(notifee.displayNotification).toHaveBeenCalled();
        });

        it('should notify when context decision mode is SILENT', async () => {
            // Given: Context decision says SILENT
            const alert = createTestAlert();
            const deliveryDecision = createDeliveryDecision();
            const contextDecision = createContextDecision({ mode: 'SILENT' });

            // When: We notify
            await notifyInBackground(alert, deliveryDecision, contextDecision);

            // Then: Notification should be displayed
            expect(notifee.displayNotification).toHaveBeenCalled();
        });
    });

    describe('Channel selection', () => {
        it('should use CRITICAL channel for HEADS_UP mode', async () => {
            // Given: HEADS_UP context decision
            const alert = createTestAlert();
            const deliveryDecision = createDeliveryDecision();
            const contextDecision = createContextDecision({ mode: 'HEADS_UP' });

            // When: We notify
            await notifyInBackground(alert, deliveryDecision, contextDecision);

            // Then: CRITICAL channel should be used
            expect(notifee.displayNotification).toHaveBeenCalledWith(
                expect.objectContaining({
                    android: expect.objectContaining({
                        channelId: CHANNEL_IDS.CRITICAL,
                    }),
                })
            );
        });

        it('should use SILENT channel for SILENT mode', async () => {
            // Given: SILENT context decision
            const alert = createTestAlert();
            const deliveryDecision = createDeliveryDecision();
            const contextDecision = createContextDecision({ mode: 'SILENT' });

            // When: We notify
            await notifyInBackground(alert, deliveryDecision, contextDecision);

            // Then: SILENT channel should be used
            expect(notifee.displayNotification).toHaveBeenCalledWith(
                expect.objectContaining({
                    android: expect.objectContaining({
                        channelId: CHANNEL_IDS.SILENT,
                    }),
                })
            );
        });
    });

    describe('Notification content', () => {
        it('should include alert ID in notification', async () => {
            // Given: Alert with specific ID
            const alert = createTestAlert({ id: 'unique-alert-456' });
            const deliveryDecision = createDeliveryDecision();
            const contextDecision = createContextDecision();

            // When: We notify
            await notifyInBackground(alert, deliveryDecision, contextDecision);

            // Then: Notification should have the alert ID
            expect(notifee.displayNotification).toHaveBeenCalledWith(
                expect.objectContaining({
                    id: 'unique-alert-456',
                })
            );
        });

        it('should include alert title in notification', async () => {
            // Given: Alert with specific title
            const alert = createTestAlert({ title: 'Earthquake Warning' });
            const deliveryDecision = createDeliveryDecision();
            const contextDecision = createContextDecision();

            // When: We notify
            await notifyInBackground(alert, deliveryDecision, contextDecision);

            // Then: Notification should display the title
            expect(notifee.displayNotification).toHaveBeenCalledWith(
                expect.objectContaining({
                    title: 'Earthquake Warning',
                })
            );
        });

        it('should include alert description as notification body', async () => {
            // Given: Alert with specific description
            const alert = createTestAlert({
                description: 'Magnitude 6.5 earthquake detected. Take cover immediately.',
            });
            const deliveryDecision = createDeliveryDecision();
            const contextDecision = createContextDecision();

            // When: We notify
            await notifyInBackground(alert, deliveryDecision, contextDecision);

            // Then: Notification should display the description
            expect(notifee.displayNotification).toHaveBeenCalledWith(
                expect.objectContaining({
                    body: 'Magnitude 6.5 earthquake detected. Take cover immediately.',
                })
            );
        });

        it('should serialize alert data for re-hydration', async () => {
            // Given: Complete alert object
            const alert = createTestAlert();
            const deliveryDecision = createDeliveryDecision();
            const contextDecision = createContextDecision();

            // When: We notify
            await notifyInBackground(alert, deliveryDecision, contextDecision);

            // Then: Alert should be serialized in notification data
            expect(notifee.displayNotification).toHaveBeenCalledWith(
                expect.objectContaining({
                    data: expect.objectContaining({
                        alert: JSON.stringify(alert),
                        contextMode: 'HEADS_UP',
                    }),
                })
            );
        });

        it('should include context mode in notification data', async () => {
            // Given: SILENT mode
            const alert = createTestAlert();
            const deliveryDecision = createDeliveryDecision();
            const contextDecision = createContextDecision({ mode: 'SILENT' });

            // When: We notify
            await notifyInBackground(alert, deliveryDecision, contextDecision);

            // Then: Context mode should be included
            expect(notifee.displayNotification).toHaveBeenCalledWith(
                expect.objectContaining({
                    data: expect.objectContaining({
                        contextMode: 'SILENT',
                    }),
                })
            );
        });
    });

    describe('Android-specific configuration', () => {
        it('should set timestamp from alert', async () => {
            // Given: Alert with specific timestamp
            const timestamp = 1640000000000;
            const alert = createTestAlert({ timestamp });
            const deliveryDecision = createDeliveryDecision();
            const contextDecision = createContextDecision();

            // When: We notify
            await notifyInBackground(alert, deliveryDecision, contextDecision);

            // Then: Timestamp should be set correctly
            expect(notifee.displayNotification).toHaveBeenCalledWith(
                expect.objectContaining({
                    android: expect.objectContaining({
                        timestamp,
                        showTimestamp: true,
                    }),
                })
            );
        });

        it('should configure press action for default launch', async () => {
            // Given: Standard alert
            const alert = createTestAlert();
            const deliveryDecision = createDeliveryDecision();
            const contextDecision = createContextDecision();

            // When: We notify
            await notifyInBackground(alert, deliveryDecision, contextDecision);

            // Then: Press action should be configured
            expect(notifee.displayNotification).toHaveBeenCalledWith(
                expect.objectContaining({
                    android: expect.objectContaining({
                        pressAction: {
                            id: 'default',
                            launchActivity: 'default',
                        },
                    }),
                })
            );
        });

        it('should set ongoing flag for HEADS_UP notifications', async () => {
            // Given: HEADS_UP mode
            const alert = createTestAlert();
            const deliveryDecision = createDeliveryDecision();
            const contextDecision = createContextDecision({ mode: 'HEADS_UP' });

            // When: We notify
            await notifyInBackground(alert, deliveryDecision, contextDecision);

            // Then: Ongoing flag should be true
            expect(notifee.displayNotification).toHaveBeenCalledWith(
                expect.objectContaining({
                    android: expect.objectContaining({
                        ongoing: true,
                    }),
                })
            );
        });

        it('should not set ongoing flag for SILENT notifications', async () => {
            // Given: SILENT mode
            const alert = createTestAlert();
            const deliveryDecision = createDeliveryDecision();
            const contextDecision = createContextDecision({ mode: 'SILENT' });

            // When: We notify
            await notifyInBackground(alert, deliveryDecision, contextDecision);

            // Then: Ongoing flag should be false
            expect(notifee.displayNotification).toHaveBeenCalledWith(
                expect.objectContaining({
                    android: expect.objectContaining({
                        ongoing: false,
                    }),
                })
            );
        });
    });

    describe('Error handling', () => {
        it('should catch and log errors from notifee.displayNotification', async () => {
            // Given: Notifee throws an error
            const consoleErrorSpy = jest.spyOn(console, 'error').mockImplementation();
            const error = new Error('Notifee error');
            (notifee.displayNotification as jest.Mock).mockRejectedValue(error);

            const alert = createTestAlert();
            const deliveryDecision = createDeliveryDecision();
            const contextDecision = createContextDecision();

            // When: We attempt to notify
            await notifyInBackground(alert, deliveryDecision, contextDecision);

            // Then: Error should be caught and logged
            expect(consoleErrorSpy).toHaveBeenCalledWith(
                '[BackgroundNotifier] Failed to display notification:',
                error
            );

            consoleErrorSpy.mockRestore();
        });

        it('should not throw error when notifee fails', async () => {
            // Given: Notifee throws an error
            jest.spyOn(console, 'error').mockImplementation();
            (notifee.displayNotification as jest.Mock).mockRejectedValue(
                new Error('Channel not found')
            );

            const alert = createTestAlert();
            const deliveryDecision = createDeliveryDecision();
            const contextDecision = createContextDecision();

            // When: We attempt to notify
            const notifyPromise = notifyInBackground(alert, deliveryDecision, contextDecision);

            // Then: Promise should resolve without throwing
            await expect(notifyPromise).resolves.toBeUndefined();
        });

        it('should handle invalid channel ID errors gracefully', async () => {
            // Given: Notifee rejects with channel error
            const consoleErrorSpy = jest.spyOn(console, 'error').mockImplementation();
            (notifee.displayNotification as jest.Mock).mockRejectedValue(
                new Error('Invalid channel ID')
            );

            const alert = createTestAlert();
            const deliveryDecision = createDeliveryDecision();
            const contextDecision = createContextDecision();

            // When: We attempt to notify
            await notifyInBackground(alert, deliveryDecision, contextDecision);

            // Then: Should log and continue
            expect(consoleErrorSpy).toHaveBeenCalled();
            consoleErrorSpy.mockRestore();
        });
    });

    describe('Idempotency', () => {
        it('should allow multiple calls with same alert ID', async () => {
            // Given: Same alert called multiple times
            const alert = createTestAlert({ id: 'same-alert' });
            const deliveryDecision = createDeliveryDecision();
            const contextDecision = createContextDecision();

            // When: We notify multiple times
            await notifyInBackground(alert, deliveryDecision, contextDecision);
            await notifyInBackground(alert, deliveryDecision, contextDecision);
            await notifyInBackground(alert, deliveryDecision, contextDecision);

            // Then: Notifee should be called each time (upserts by ID)
            expect(notifee.displayNotification).toHaveBeenCalledTimes(3);
        });

        it('should update existing notification when called with same ID', async () => {
            // This tests that Notifee's upsert behavior is leveraged
            // Given: Alert with same ID but different content
            const alert1 = createTestAlert({
                id: 'update-test',
                title: 'Initial Title',
            });
            const alert2 = createTestAlert({
                id: 'update-test',
                title: 'Updated Title',
            });
            const deliveryDecision = createDeliveryDecision();
            const contextDecision = createContextDecision();

            // When: We notify twice with same ID
            await notifyInBackground(alert1, deliveryDecision, contextDecision);
            await notifyInBackground(alert2, deliveryDecision, contextDecision);

            // Then: Both calls should go through
            expect(notifee.displayNotification).toHaveBeenNthCalledWith(
                1,
                expect.objectContaining({ id: 'update-test', title: 'Initial Title' })
            );
            expect(notifee.displayNotification).toHaveBeenNthCalledWith(
                2,
                expect.objectContaining({ id: 'update-test', title: 'Updated Title' })
            );
        });
    });

    describe('Different alert types', () => {
        it('should handle CRITICAL hazard level alerts', async () => {
            // Given: Critical alert
            const alert = createTestAlert({
                hazardLevel: HazardLevel.CRITICAL,
                title: 'IMMEDIATE DANGER',
            });
            const deliveryDecision = createDeliveryDecision();
            const contextDecision = createContextDecision({ mode: 'HEADS_UP' });

            // When: We notify
            await notifyInBackground(alert, deliveryDecision, contextDecision);

            // Then: Should use CRITICAL channel
            expect(notifee.displayNotification).toHaveBeenCalledWith(
                expect.objectContaining({
                    title: 'IMMEDIATE DANGER',
                    android: expect.objectContaining({
                        channelId: CHANNEL_IDS.CRITICAL,
                    }),
                })
            );
        });

        it('should handle LOW hazard level alerts with SILENT mode', async () => {
            // Given: Low hazard alert
            const alert = createTestAlert({
                hazardLevel: HazardLevel.LOW,
                title: 'Weather Advisory',
            });
            const deliveryDecision = createDeliveryDecision();
            const contextDecision = createContextDecision({ mode: 'SILENT' });

            // When: We notify
            await notifyInBackground(alert, deliveryDecision, contextDecision);

            // Then: Should use SILENT channel
            expect(notifee.displayNotification).toHaveBeenCalledWith(
                expect.objectContaining({
                    title: 'Weather Advisory',
                    android: expect.objectContaining({
                        channelId: CHANNEL_IDS.SILENT,
                    }),
                })
            );
        });

        it('should handle alerts from different sources', async () => {
            // Given: Alerts from various sources
            const sources = [AlertSource.CELL, AlertSource.BLUETOOTH, AlertSource.SATELLITE];
            const deliveryDecision = createDeliveryDecision();
            const contextDecision = createContextDecision();

            // When: We notify for each source
            for (const source of sources) {
                const alert = createTestAlert({ source });
                await notifyInBackground(alert, deliveryDecision, contextDecision);
            }

            // Then: All should be processed
            expect(notifee.displayNotification).toHaveBeenCalledTimes(3);
        });
    });

    describe('Edge cases', () => {
        it('should handle alerts with very long titles', async () => {
            // Given: Alert with long title
            const longTitle = 'A'.repeat(500);
            const alert = createTestAlert({ title: longTitle });
            const deliveryDecision = createDeliveryDecision();
            const contextDecision = createContextDecision();

            // When: We notify
            await notifyInBackground(alert, deliveryDecision, contextDecision);

            // Then: Should handle without errors
            expect(notifee.displayNotification).toHaveBeenCalledWith(
                expect.objectContaining({
                    title: longTitle,
                })
            );
        });

        it('should handle alerts with very long descriptions', async () => {
            // Given: Alert with long description
            const longDescription = 'B'.repeat(1000);
            const alert = createTestAlert({ description: longDescription });
            const deliveryDecision = createDeliveryDecision();
            const contextDecision = createContextDecision();

            // When: We notify
            await notifyInBackground(alert, deliveryDecision, contextDecision);

            // Then: Should handle without errors
            expect(notifee.displayNotification).toHaveBeenCalledWith(
                expect.objectContaining({
                    body: longDescription,
                })
            );
        });

        it('should handle alerts with special characters in title', async () => {
            // Given: Alert with special characters
            const alert = createTestAlert({
                title: '🚨 ALERT: Evacuation Required! @#$%^&*()',
            });
            const deliveryDecision = createDeliveryDecision();
            const contextDecision = createContextDecision();

            // When: We notify
            await notifyInBackground(alert, deliveryDecision, contextDecision);

            // Then: Special characters should be preserved
            expect(notifee.displayNotification).toHaveBeenCalledWith(
                expect.objectContaining({
                    title: '🚨 ALERT: Evacuation Required! @#$%^&*()',
                })
            );
        });

        it('should handle alerts with timestamp at epoch', async () => {
            // Given: Alert with timestamp of 0
            const alert = createTestAlert({ timestamp: 0 });
            const deliveryDecision = createDeliveryDecision();
            const contextDecision = createContextDecision();

            // When: We notify
            await notifyInBackground(alert, deliveryDecision, contextDecision);

            // Then: Should handle edge case timestamp
            expect(notifee.displayNotification).toHaveBeenCalledWith(
                expect.objectContaining({
                    android: expect.objectContaining({
                        timestamp: 0,
                    }),
                })
            );
        });

        it('should handle alerts with far future timestamps', async () => {
            // Given: Alert with far future timestamp
            const futureTimestamp = Date.now() + 365 * 24 * 60 * 60 * 1000; // 1 year
            const alert = createTestAlert({ timestamp: futureTimestamp });
            const deliveryDecision = createDeliveryDecision();
            const contextDecision = createContextDecision();

            // When: We notify
            await notifyInBackground(alert, deliveryDecision, contextDecision);

            // Then: Should handle future timestamp
            expect(notifee.displayNotification).toHaveBeenCalledWith(
                expect.objectContaining({
                    android: expect.objectContaining({
                        timestamp: futureTimestamp,
                    }),
                })
            );
        });
    });
});