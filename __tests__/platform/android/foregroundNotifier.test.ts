/**
 * Unit tests for foregroundNotifier.ts
 * 
 * Tests the foreground notification execution module that displays
 * notifications when the app is active and visible to the user.
 */

import { AppState, Platform } from 'react-native';
import notifee from '@notifee/react-native';
import { notifyInForeground } from '../../../src/platform/android/foregroundNotifier';
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

// Mock AppState
jest.mock('react-native/Libraries/AppState/AppState', () => ({
    currentState: 'active',
    addEventListener: jest.fn(),
    removeEventListener: jest.fn(),
}));

describe('foregroundNotifier', () => {
    // Helper functions
    const createTestAlert = (overrides: Partial<Alert> = {}): Alert => ({
        id: 'fg-alert-789',
        source: AlertSource.CELL,
        hazardLevel: HazardLevel.SEVERE,
        location: {
            latitude: 40.7128,
            longitude: -74.0060,
            radius: 5000,
        },
        title: 'Active Alert',
        description: 'Alert while app is in foreground',
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
        (Platform as any).OS = 'android';
        (AppState as any).currentState = 'active';
    });

    describe('Platform guards', () => {
        it('should not execute on iOS platform', async () => {
            // Given: iOS platform
            (Platform as any).OS = 'ios';
            const alert = createTestAlert();
            const deliveryDecision = createDeliveryDecision();
            const contextDecision = createContextDecision();

            // When: We attempt to notify in foreground
            await notifyInForeground(alert, deliveryDecision, contextDecision);

            // Then: Notifee should not be called
            expect(notifee.displayNotification).not.toHaveBeenCalled();
        });

        it('should execute on Android platform when app is active', async () => {
            // Given: Android platform and active app state
            (Platform as any).OS = 'android';
            (AppState as any).currentState = 'active';
            const alert = createTestAlert();
            const deliveryDecision = createDeliveryDecision();
            const contextDecision = createContextDecision();

            // When: We notify in foreground
            await notifyInForeground(alert, deliveryDecision, contextDecision);

            // Then: Notifee should be called
            expect(notifee.displayNotification).toHaveBeenCalled();
        });
    });

    describe('AppState guards', () => {
        it('should not execute when app is in background state', async () => {
            // Given: App in background
            (AppState as any).currentState = 'background';
            const alert = createTestAlert();
            const deliveryDecision = createDeliveryDecision();
            const contextDecision = createContextDecision();

            // When: We attempt to notify
            await notifyInForeground(alert, deliveryDecision, contextDecision);

            // Then: Notifee should not be called
            expect(notifee.displayNotification).not.toHaveBeenCalled();
        });

        it('should not execute when app is inactive', async () => {
            // Given: App is inactive
            (AppState as any).currentState = 'inactive';
            const alert = createTestAlert();
            const deliveryDecision = createDeliveryDecision();
            const contextDecision = createContextDecision();

            // When: We attempt to notify
            await notifyInForeground(alert, deliveryDecision, contextDecision);

            // Then: Notifee should not be called
            expect(notifee.displayNotification).not.toHaveBeenCalled();
        });

        it('should execute only when app state is active', async () => {
            // Given: App is active
            (AppState as any).currentState = 'active';
            const alert = createTestAlert();
            const deliveryDecision = createDeliveryDecision();
            const contextDecision = createContextDecision();

            // When: We notify
            await notifyInForeground(alert, deliveryDecision, contextDecision);

            // Then: Notifee should be called
            expect(notifee.displayNotification).toHaveBeenCalled();
        });

        it('should check AppState.currentState at invocation time', async () => {
            // This ensures the check is dynamic, not cached
            const alert = createTestAlert();
            const deliveryDecision = createDeliveryDecision();
            const contextDecision = createContextDecision();

            // First call with active state
            (AppState as any).currentState = 'active';
            await notifyInForeground(alert, deliveryDecision, contextDecision);
            expect(notifee.displayNotification).toHaveBeenCalledTimes(1);

            // Second call with background state
            (AppState as any).currentState = 'background';
            await notifyInForeground(alert, deliveryDecision, contextDecision);
            expect(notifee.displayNotification).toHaveBeenCalledTimes(1); // Still 1

            // Third call with active again
            (AppState as any).currentState = 'active';
            await notifyInForeground(alert, deliveryDecision, contextDecision);
            expect(notifee.displayNotification).toHaveBeenCalledTimes(2); // Now 2
        });
    });

    describe('Context decision handling', () => {
        it('should not notify when context mode is NONE', async () => {
            // Given: Context decision is NONE
            const alert = createTestAlert();
            const deliveryDecision = createDeliveryDecision();
            const contextDecision = createContextDecision({ mode: 'NONE' });

            // When: We attempt to notify
            await notifyInForeground(alert, deliveryDecision, contextDecision);

            // Then: No notification should be displayed
            expect(notifee.displayNotification).not.toHaveBeenCalled();
        });

        it('should notify with HEADS_UP mode', async () => {
            // Given: HEADS_UP mode
            const alert = createTestAlert();
            const deliveryDecision = createDeliveryDecision();
            const contextDecision = createContextDecision({ mode: 'HEADS_UP' });

            // When: We notify
            await notifyInForeground(alert, deliveryDecision, contextDecision);

            // Then: Notification should be displayed
            expect(notifee.displayNotification).toHaveBeenCalled();
        });

        it('should notify with SILENT mode', async () => {
            // Given: SILENT mode
            const alert = createTestAlert();
            const deliveryDecision = createDeliveryDecision();
            const contextDecision = createContextDecision({ mode: 'SILENT' });

            // When: We notify
            await notifyInForeground(alert, deliveryDecision, contextDecision);

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
            await notifyInForeground(alert, deliveryDecision, contextDecision);

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
            await notifyInForeground(alert, deliveryDecision, contextDecision);

            // Then: SILENT channel should be used
            expect(notifee.displayNotification).toHaveBeenCalledWith(
                expect.objectContaining({
                    android: expect.objectContaining({
                        channelId: CHANNEL_IDS.SILENT,
                    }),
                })
            );
        });

        it('should default to SILENT channel for unknown modes', async () => {
            // Given: Unknown mode (edge case)
            const alert = createTestAlert();
            const deliveryDecision = createDeliveryDecision();
            const contextDecision = createContextDecision({ mode: 'UNKNOWN' as any });

            // When: We notify
            await notifyInForeground(alert, deliveryDecision, contextDecision);

            // Then: Should default to SILENT
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
        it('should include alert ID', async () => {
            // Given: Alert with specific ID
            const alert = createTestAlert({ id: 'foreground-alert-123' });
            const deliveryDecision = createDeliveryDecision();
            const contextDecision = createContextDecision();

            // When: We notify
            await notifyInForeground(alert, deliveryDecision, contextDecision);

            // Then: ID should be included
            expect(notifee.displayNotification).toHaveBeenCalledWith(
                expect.objectContaining({
                    id: 'foreground-alert-123',
                })
            );
        });

        it('should include alert title', async () => {
            // Given: Alert with title
            const alert = createTestAlert({ title: 'Tornado Warning' });
            const deliveryDecision = createDeliveryDecision();
            const contextDecision = createContextDecision();

            // When: We notify
            await notifyInForeground(alert, deliveryDecision, contextDecision);

            // Then: Title should be displayed
            expect(notifee.displayNotification).toHaveBeenCalledWith(
                expect.objectContaining({
                    title: 'Tornado Warning',
                })
            );
        });

        it('should include alert description as body', async () => {
            // Given: Alert with description
            const alert = createTestAlert({
                description: 'Take shelter immediately in a basement or interior room',
            });
            const deliveryDecision = createDeliveryDecision();
            const contextDecision = createContextDecision();

            // When: We notify
            await notifyInForeground(alert, deliveryDecision, contextDecision);

            // Then: Description should be body
            expect(notifee.displayNotification).toHaveBeenCalledWith(
                expect.objectContaining({
                    body: 'Take shelter immediately in a basement or interior room',
                })
            );
        });

        it('should NOT include serialized data in foreground notifications', async () => {
            // Unlike background notifications, foreground ones don't need data serialization
            // since the app is already running
            const alert = createTestAlert();
            const deliveryDecision = createDeliveryDecision();
            const contextDecision = createContextDecision();

            // When: We notify
            await notifyInForeground(alert, deliveryDecision, contextDecision);

            // Then: Data field should not be present
            const callArgs = (notifee.displayNotification as jest.Mock).mock.calls[0][0];
            expect(callArgs.data).toBeUndefined();
        });
    });

    describe('Android configuration', () => {
        it('should set timestamp from alert', async () => {
            // Given: Alert with specific timestamp
            const timestamp = 1650000000000;
            const alert = createTestAlert({ timestamp });
            const deliveryDecision = createDeliveryDecision();
            const contextDecision = createContextDecision();

            // When: We notify
            await notifyInForeground(alert, deliveryDecision, contextDecision);

            // Then: Timestamp should be set
            expect(notifee.displayNotification).toHaveBeenCalledWith(
                expect.objectContaining({
                    android: expect.objectContaining({
                        timestamp,
                        showTimestamp: true,
                    }),
                })
            );
        });

        it('should configure press action', async () => {
            // Given: Standard alert
            const alert = createTestAlert();
            const deliveryDecision = createDeliveryDecision();
            const contextDecision = createContextDecision();

            // When: We notify
            await notifyInForeground(alert, deliveryDecision, contextDecision);

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

        it('should show timestamp for all notifications', async () => {
            // Given: Any alert
            const alert = createTestAlert();
            const deliveryDecision = createDeliveryDecision();
            const contextDecision = createContextDecision();

            // When: We notify
            await notifyInForeground(alert, deliveryDecision, contextDecision);

            // Then: showTimestamp should be true
            expect(notifee.displayNotification).toHaveBeenCalledWith(
                expect.objectContaining({
                    android: expect.objectContaining({
                        showTimestamp: true,
                    }),
                })
            );
        });

        it('should NOT set ongoing flag for foreground notifications', async () => {
            // Unlike background, foreground notifications shouldn't pin
            const alert = createTestAlert();
            const deliveryDecision = createDeliveryDecision();
            const contextDecision = createContextDecision({ mode: 'HEADS_UP' });

            // When: We notify
            await notifyInForeground(alert, deliveryDecision, contextDecision);

            // Then: ongoing should not be present
            const callArgs = (notifee.displayNotification as jest.Mock).mock.calls[0][0];
            expect(callArgs.android.ongoing).toBeUndefined();
        });
    });

    describe('Error handling', () => {
        it('should catch and log notifee errors', async () => {
            // Given: Notifee throws an error
            const consoleErrorSpy = jest.spyOn(console, 'error').mockImplementation();
            const error = new Error('Notifee display error');
            (notifee.displayNotification as jest.Mock).mockRejectedValue(error);

            const alert = createTestAlert();
            const deliveryDecision = createDeliveryDecision();
            const contextDecision = createContextDecision();

            // When: We attempt to notify
            await notifyInForeground(alert, deliveryDecision, contextDecision);

            // Then: Error should be caught and logged
            expect(consoleErrorSpy).toHaveBeenCalledWith(
                '[ForegroundNotifier] Failed to display notification:',
                error
            );

            consoleErrorSpy.mockRestore();
        });

        it('should not throw error when notifee fails', async () => {
            // Given: Notifee throws an error
            jest.spyOn(console, 'error').mockImplementation();
            (notifee.displayNotification as jest.Mock).mockRejectedValue(
                new Error('Permission denied')
            );

            const alert = createTestAlert();
            const deliveryDecision = createDeliveryDecision();
            const contextDecision = createContextDecision();

            // When: We attempt to notify
            const notifyPromise = notifyInForeground(alert, deliveryDecision, contextDecision);

            // Then: Promise should resolve without throwing
            await expect(notifyPromise).resolves.toBeUndefined();
        });

        it('should handle channel not found errors gracefully', async () => {
            // Given: Invalid channel error
            const consoleErrorSpy = jest.spyOn(console, 'error').mockImplementation();
            (notifee.displayNotification as jest.Mock).mockRejectedValue(
                new Error('Channel not found: invalid_channel')
            );

            const alert = createTestAlert();
            const deliveryDecision = createDeliveryDecision();
            const contextDecision = createContextDecision();

            // When: We attempt to notify
            await notifyInForeground(alert, deliveryDecision, contextDecision);

            // Then: Should log and not crash
            expect(consoleErrorSpy).toHaveBeenCalled();
            consoleErrorSpy.mockRestore();
        });
    });

    describe('Multiple notification scenarios', () => {
        it('should display multiple notifications for different alerts', async () => {
            // Given: Multiple different alerts
            const alert1 = createTestAlert({ id: 'alert-1', title: 'First Alert' });
            const alert2 = createTestAlert({ id: 'alert-2', title: 'Second Alert' });
            const alert3 = createTestAlert({ id: 'alert-3', title: 'Third Alert' });
            const deliveryDecision = createDeliveryDecision();
            const contextDecision = createContextDecision();

            // When: We notify for each alert
            await notifyInForeground(alert1, deliveryDecision, contextDecision);
            await notifyInForeground(alert2, deliveryDecision, contextDecision);
            await notifyInForeground(alert3, deliveryDecision, contextDecision);

            // Then: All should be displayed
            expect(notifee.displayNotification).toHaveBeenCalledTimes(3);
        });

        it('should handle rapid successive notifications', async () => {
            // Given: Rapid notifications
            const alerts = Array.from({ length: 5 }, (_, i) =>
                createTestAlert({ id: `rapid-${i}` })
            );
            const deliveryDecision = createDeliveryDecision();
            const contextDecision = createContextDecision();

            // When: We rapidly notify
            await Promise.all(
                alerts.map(alert => notifyInForeground(alert, deliveryDecision, contextDecision))
            );

            // Then: All should be processed
            expect(notifee.displayNotification).toHaveBeenCalledTimes(5);
        });

        it('should allow updating same notification by ID', async () => {
            // Given: Same ID, different content
            const alert1 = createTestAlert({
                id: 'update-alert',
                title: 'Initial',
                description: 'First version',
            });
            const alert2 = createTestAlert({
                id: 'update-alert',
                title: 'Updated',
                description: 'Second version',
            });
            const deliveryDecision = createDeliveryDecision();
            const contextDecision = createContextDecision();

            // When: We notify twice with same ID
            await notifyInForeground(alert1, deliveryDecision, contextDecision);
            await notifyInForeground(alert2, deliveryDecision, contextDecision);

            // Then: Both calls should be made (notifee handles upsert)
            expect(notifee.displayNotification).toHaveBeenCalledTimes(2);
            expect(notifee.displayNotification).toHaveBeenNthCalledWith(
                2,
                expect.objectContaining({
                    id: 'update-alert',
                    title: 'Updated',
                })
            );
        });
    });

    describe('Hazard level scenarios', () => {
        it('should handle CRITICAL alerts with HEADS_UP', async () => {
            // Given: Critical alert
            const alert = createTestAlert({
                hazardLevel: HazardLevel.CRITICAL,
                title: 'CRITICAL: Dam Failure Imminent',
            });
            const deliveryDecision = createDeliveryDecision();
            const contextDecision = createContextDecision({ mode: 'HEADS_UP' });

            // When: We notify
            await notifyInForeground(alert, deliveryDecision, contextDecision);

            // Then: Should use CRITICAL channel
            expect(notifee.displayNotification).toHaveBeenCalledWith(
                expect.objectContaining({
                    android: expect.objectContaining({
                        channelId: CHANNEL_IDS.CRITICAL,
                    }),
                })
            );
        });

        it('should handle LOW alerts with SILENT mode', async () => {
            // Given: Low priority alert
            const alert = createTestAlert({
                hazardLevel: HazardLevel.LOW,
                title: 'Weather Update',
            });
            const deliveryDecision = createDeliveryDecision();
            const contextDecision = createContextDecision({ mode: 'SILENT' });

            // When: We notify
            await notifyInForeground(alert, deliveryDecision, contextDecision);

            // Then: Should use SILENT channel
            expect(notifee.displayNotification).toHaveBeenCalledWith(
                expect.objectContaining({
                    android: expect.objectContaining({
                        channelId: CHANNEL_IDS.SILENT,
                    }),
                })
            );
        });

        it('should handle MODERATE alerts appropriately', async () => {
            // Given: Moderate alert
            const alert = createTestAlert({
                hazardLevel: HazardLevel.MODERATE,
                title: 'Weather Advisory',
            });
            const deliveryDecision = createDeliveryDecision();
            const contextDecision = createContextDecision({ mode: 'SILENT' });

            // When: We notify
            await notifyInForeground(alert, deliveryDecision, contextDecision);

            // Then: Should process normally
            expect(notifee.displayNotification).toHaveBeenCalled();
        });
    });

    describe('Edge cases', () => {
        it('should handle empty title', async () => {
            // Given: Alert with empty title
            const alert = createTestAlert({ title: '' });
            const deliveryDecision = createDeliveryDecision();
            const contextDecision = createContextDecision();

            // When: We notify
            await notifyInForeground(alert, deliveryDecision, contextDecision);

            // Then: Should handle gracefully
            expect(notifee.displayNotification).toHaveBeenCalledWith(
                expect.objectContaining({ title: '' })
            );
        });

        it('should handle empty description', async () => {
            // Given: Alert with empty description
            const alert = createTestAlert({ description: '' });
            const deliveryDecision = createDeliveryDecision();
            const contextDecision = createContextDecision();

            // When: We notify
            await notifyInForeground(alert, deliveryDecision, contextDecision);

            // Then: Should handle gracefully
            expect(notifee.displayNotification).toHaveBeenCalledWith(
                expect.objectContaining({ body: '' })
            );
        });

        it('should handle unicode and emoji in content', async () => {
            // Given: Alert with unicode/emoji
            const alert = createTestAlert({
                title: '🚨 緊急警報 ⚠️',
                description: 'चेतावनी: खतरनाक स्थिति 危险情况',
            });
            const deliveryDecision = createDeliveryDecision();
            const contextDecision = createContextDecision();

            // When: We notify
            await notifyInForeground(alert, deliveryDecision, contextDecision);

            // Then: Unicode should be preserved
            expect(notifee.displayNotification).toHaveBeenCalledWith(
                expect.objectContaining({
                    title: '🚨 緊急警報 ⚠️',
                    body: 'चेतावनी: खतरनाक स्थिति 危险情况',
                })
            );
        });

        it('should handle alerts from all source types', async () => {
            // Given: Alerts from different sources
            const sources = [AlertSource.CELL, AlertSource.BLUETOOTH, AlertSource.SATELLITE];
            const deliveryDecision = createDeliveryDecision();
            const contextDecision = createContextDecision();

            // When: We notify for each source
            for (const source of sources) {
                const alert = createTestAlert({ source, id: `${source}-alert` });
                await notifyInForeground(alert, deliveryDecision, contextDecision);
            }

            // Then: All should be processed
            expect(notifee.displayNotification).toHaveBeenCalledTimes(3);
        });
    });

    describe('State transition scenarios', () => {
        it('should respect app state changes during execution', async () => {
            // Simulate app state changing during notification attempts
            const alert = createTestAlert();
            const deliveryDecision = createDeliveryDecision();
            const contextDecision = createContextDecision();

            // Active -> should notify
            (AppState as any).currentState = 'active';
            await notifyInForeground(alert, deliveryDecision, contextDecision);
            expect(notifee.displayNotification).toHaveBeenCalledTimes(1);

            // Background -> should not notify
            (AppState as any).currentState = 'background';
            await notifyInForeground(alert, deliveryDecision, contextDecision);
            expect(notifee.displayNotification).toHaveBeenCalledTimes(1);

            // Inactive -> should not notify
            (AppState as any).currentState = 'inactive';
            await notifyInForeground(alert, deliveryDecision, contextDecision);
            expect(notifee.displayNotification).toHaveBeenCalledTimes(1);

            // Active again -> should notify
            (AppState as any).currentState = 'active';
            await notifyInForeground(alert, deliveryDecision, contextDecision);
            expect(notifee.displayNotification).toHaveBeenCalledTimes(2);
        });
    });
});