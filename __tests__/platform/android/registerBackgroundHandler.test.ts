/**
 * Unit tests for registerBackgroundHandler.ts
 * 
 * Tests the background event handler registration that processes
 * user interactions with notifications when the app is in background.
 */

import notifee, { EventType } from '@notifee/react-native';
import { registerBackgroundNotificationHandler } from '../../../src/platform/android/registerBackgroundHandler';

// Mock notifee
jest.mock('@notifee/react-native', () => ({
    onBackgroundEvent: jest.fn(),
    EventType: {
        ACTION_PRESS: 'ACTION_PRESS',
        DISMISSED: 'DISMISSED',
        DELIVERED: 'DELIVERED',
    },
}));

describe('registerBackgroundHandler', () => {
    let backgroundEventHandler: ((event: any) => Promise<void>) | null = null;

    beforeEach(() => {
        jest.clearAllMocks();
        backgroundEventHandler = null;

        // Capture the handler passed to onBackgroundEvent
        (notifee.onBackgroundEvent as jest.Mock).mockImplementation((handler) => {
            backgroundEventHandler = handler;
        });
    });

    describe('Registration', () => {
        it('should register background event handler with notifee', () => {
            // When: We register the handler
            registerBackgroundNotificationHandler();

            // Then: onBackgroundEvent should be called
            expect(notifee.onBackgroundEvent).toHaveBeenCalledTimes(1);
            expect(notifee.onBackgroundEvent).toHaveBeenCalledWith(expect.any(Function));
        });

        it('should capture the registered handler', () => {
            // When: We register
            registerBackgroundNotificationHandler();

            // Then: Handler should be captured
            expect(backgroundEventHandler).not.toBeNull();
            expect(typeof backgroundEventHandler).toBe('function');
        });

        it('should allow multiple registrations', () => {
            // When: We register multiple times
            registerBackgroundNotificationHandler();
            registerBackgroundNotificationHandler();
            registerBackgroundNotificationHandler();

            // Then: Each call should register
            expect(notifee.onBackgroundEvent).toHaveBeenCalledTimes(3);
        });
    });

    describe('Event handling - ACTION_PRESS', () => {
        beforeEach(() => {
            registerBackgroundNotificationHandler();
        });

        it('should handle ACTION_PRESS event with default press action', async () => {
            // Given: ACTION_PRESS event with default action
            const consoleLogSpy = jest.spyOn(console, 'log').mockImplementation();
            const event = {
                type: EventType.ACTION_PRESS,
                detail: {
                    notification: {
                        id: 'test-notification-123',
                        title: 'Test Alert',
                        body: 'Test body',
                    },
                    pressAction: {
                        id: 'default',
                    },
                },
            };

            // When: Event is triggered
            await backgroundEventHandler?.(event);

            // Then: Should log the notification press
            expect(consoleLogSpy).toHaveBeenCalledWith(
                '[BackgroundHandler] User pressed notification:',
                'test-notification-123'
            );

            consoleLogSpy.mockRestore();
        });

        it('should handle ACTION_PRESS without notification ID', async () => {
            // Given: Event without notification ID
            const consoleLogSpy = jest.spyOn(console, 'log').mockImplementation();
            const event = {
                type: EventType.ACTION_PRESS,
                detail: {
                    notification: undefined,
                    pressAction: {
                        id: 'default',
                    },
                },
            };

            // When: Event is triggered
            await backgroundEventHandler?.(event);

            // Then: Should handle undefined gracefully
            expect(consoleLogSpy).toHaveBeenCalledWith(
                '[BackgroundHandler] User pressed notification:',
                undefined
            );

            consoleLogSpy.mockRestore();
        });

        it('should handle ACTION_PRESS with notification containing data', async () => {
            // Given: Event with notification data
            const consoleLogSpy = jest.spyOn(console, 'log').mockImplementation();
            const alertData = {
                id: 'alert-456',
                title: 'Earthquake Alert',
                hazardLevel: 'CRITICAL',
            };
            const event = {
                type: EventType.ACTION_PRESS,
                detail: {
                    notification: {
                        id: 'notif-789',
                        data: {
                            alert: JSON.stringify(alertData),
                            contextMode: 'HEADS_UP',
                        },
                    },
                    pressAction: {
                        id: 'default',
                    },
                },
            };

            // When: Event is triggered
            await backgroundEventHandler?.(event);

            // Then: Should log notification press
            expect(consoleLogSpy).toHaveBeenCalled();

            consoleLogSpy.mockRestore();
        });

        it('should ignore ACTION_PRESS with non-default press action', async () => {
            // Given: ACTION_PRESS with different action ID
            const consoleLogSpy = jest.spyOn(console, 'log').mockImplementation();
            const event = {
                type: EventType.ACTION_PRESS,
                detail: {
                    notification: {
                        id: 'test-notification',
                    },
                    pressAction: {
                        id: 'custom-action',
                    },
                },
            };

            // When: Event is triggered
            await backgroundEventHandler?.(event);

            // Then: Should not log (condition not met)
            expect(consoleLogSpy).not.toHaveBeenCalled();

            consoleLogSpy.mockRestore();
        });

        it('should handle ACTION_PRESS without pressAction', async () => {
            // Given: ACTION_PRESS without pressAction
            const consoleLogSpy = jest.spyOn(console, 'log').mockImplementation();
            const event = {
                type: EventType.ACTION_PRESS,
                detail: {
                    notification: {
                        id: 'test-notification',
                    },
                    pressAction: undefined,
                },
            };

            // When: Event is triggered
            await backgroundEventHandler?.(event);

            // Then: Should not crash, but not log
            expect(consoleLogSpy).not.toHaveBeenCalled();

            consoleLogSpy.mockRestore();
        });

        it('should handle ACTION_PRESS with null pressAction', async () => {
            // Given: ACTION_PRESS with null pressAction
            const consoleLogSpy = jest.spyOn(console, 'log').mockImplementation();
            const event = {
                type: EventType.ACTION_PRESS,
                detail: {
                    notification: {
                        id: 'test-notification',
                    },
                    pressAction: null,
                },
            };

            // When: Event is triggered
            await backgroundEventHandler?.(event);

            // Then: Should handle gracefully
            expect(consoleLogSpy).not.toHaveBeenCalled();

            consoleLogSpy.mockRestore();
        });
    });

    describe('Event handling - other event types', () => {
        beforeEach(() => {
            registerBackgroundNotificationHandler();
        });

        it('should ignore DISMISSED events', async () => {
            // Given: DISMISSED event
            const consoleLogSpy = jest.spyOn(console, 'log').mockImplementation();
            const event = {
                type: EventType.DISMISSED,
                detail: {
                    notification: {
                        id: 'dismissed-notification',
                    },
                    pressAction: {
                        id: 'default',
                    },
                },
            };

            // When: Event is triggered
            await backgroundEventHandler?.(event);

            // Then: Should not process (only ACTION_PRESS handled)
            expect(consoleLogSpy).not.toHaveBeenCalled();

            consoleLogSpy.mockRestore();
        });

        it('should ignore DELIVERED events', async () => {
            // Given: DELIVERED event
            const consoleLogSpy = jest.spyOn(console, 'log').mockImplementation();
            const event = {
                type: EventType.DELIVERED,
                detail: {
                    notification: {
                        id: 'delivered-notification',
                    },
                },
            };

            // When: Event is triggered
            await backgroundEventHandler?.(event);

            // Then: Should not process
            expect(consoleLogSpy).not.toHaveBeenCalled();

            consoleLogSpy.mockRestore();
        });

        it('should handle unknown event types gracefully', async () => {
            // Given: Unknown event type
            const consoleLogSpy = jest.spyOn(console, 'log').mockImplementation();
            const event = {
                type: 'UNKNOWN_TYPE' as any,
                detail: {
                    notification: {
                        id: 'test-notification',
                    },
                    pressAction: {
                        id: 'default',
                    },
                },
            };

            // When: Event is triggered
            await backgroundEventHandler?.(event);

            // Then: Should not crash
            expect(consoleLogSpy).not.toHaveBeenCalled();

            consoleLogSpy.mockRestore();
        });
    });

    describe('Multiple event scenarios', () => {
        beforeEach(() => {
            registerBackgroundNotificationHandler();
        });

        it('should handle multiple ACTION_PRESS events sequentially', async () => {
            // Given: Multiple press events
            const consoleLogSpy = jest.spyOn(console, 'log').mockImplementation();
            const events = [
                {
                    type: EventType.ACTION_PRESS,
                    detail: {
                        notification: { id: 'notification-1' },
                        pressAction: { id: 'default' },
                    },
                },
                {
                    type: EventType.ACTION_PRESS,
                    detail: {
                        notification: { id: 'notification-2' },
                        pressAction: { id: 'default' },
                    },
                },
                {
                    type: EventType.ACTION_PRESS,
                    detail: {
                        notification: { id: 'notification-3' },
                        pressAction: { id: 'default' },
                    },
                },
            ];

            // When: Events are triggered sequentially
            for (const event of events) {
                await backgroundEventHandler?.(event);
            }

            // Then: All should be logged
            expect(consoleLogSpy).toHaveBeenCalledTimes(3);
            expect(consoleLogSpy).toHaveBeenNthCalledWith(
                1,
                '[BackgroundHandler] User pressed notification:',
                'notification-1'
            );
            expect(consoleLogSpy).toHaveBeenNthCalledWith(
                2,
                '[BackgroundHandler] User pressed notification:',
                'notification-2'
            );
            expect(consoleLogSpy).toHaveBeenNthCalledWith(
                3,
                '[BackgroundHandler] User pressed notification:',
                'notification-3'
            );

            consoleLogSpy.mockRestore();
        });

        it('should handle mixed event types', async () => {
            // Given: Mix of different event types
            const consoleLogSpy = jest.spyOn(console, 'log').mockImplementation();
            const events = [
                {
                    type: EventType.DELIVERED,
                    detail: { notification: { id: 'notif-1' } },
                },
                {
                    type: EventType.ACTION_PRESS,
                    detail: {
                        notification: { id: 'notif-2' },
                        pressAction: { id: 'default' },
                    },
                },
                {
                    type: EventType.DISMISSED,
                    detail: { notification: { id: 'notif-3' } },
                },
                {
                    type: EventType.ACTION_PRESS,
                    detail: {
                        notification: { id: 'notif-4' },
                        pressAction: { id: 'default' },
                    },
                },
            ];

            // When: Events are triggered
            for (const event of events) {
                await backgroundEventHandler?.(event);
            }

            // Then: Only ACTION_PRESS should be logged
            expect(consoleLogSpy).toHaveBeenCalledTimes(2);

            consoleLogSpy.mockRestore();
        });
    });

    describe('Edge cases', () => {
        beforeEach(() => {
            registerBackgroundNotificationHandler();
        });

        it('should handle event with empty detail', async () => {
            // Given: Event with empty detail
            const event = {
                type: EventType.ACTION_PRESS,
                detail: {} as any,
            };

            // When: Event is triggered
            const handlePromise = backgroundEventHandler?.(event);

            // Then: Should not crash
            await expect(handlePromise).resolves.toBeUndefined();
        });

        it('should handle event with null detail', async () => {
            // Given: Event with null detail
            const event = {
                type: EventType.ACTION_PRESS,
                detail: null as any,
            };

            // When: Event is triggered
            const handlePromise = backgroundEventHandler?.(event);

            // Then: Should not crash
            await expect(handlePromise).resolves.toBeUndefined();
        });

        it('should handle event with undefined type', async () => {
            // Given: Event with undefined type
            const event = {
                type: undefined as any,
                detail: {
                    notification: { id: 'test' },
                    pressAction: { id: 'default' },
                },
            };

            // When: Event is triggered
            const handlePromise = backgroundEventHandler?.(event);

            // Then: Should not crash
            await expect(handlePromise).resolves.toBeUndefined();
        });

        it('should handle notification with very long ID', async () => {
            // Given: Notification with extremely long ID
            const consoleLogSpy = jest.spyOn(console, 'log').mockImplementation();
            const longId = 'a'.repeat(10000);
            const event = {
                type: EventType.ACTION_PRESS,
                detail: {
                    notification: { id: longId },
                    pressAction: { id: 'default' },
                },
            };

            // When: Event is triggered
            await backgroundEventHandler?.(event);

            // Then: Should handle without errors
            expect(consoleLogSpy).toHaveBeenCalledWith(
                '[BackgroundHandler] User pressed notification:',
                longId
            );

            consoleLogSpy.mockRestore();
        });

        it('should handle notification with special characters in ID', async () => {
            // Given: Notification with special characters
            const consoleLogSpy = jest.spyOn(console, 'log').mockImplementation();
            const specialId = 'notification-!@#$%^&*()_+-=[]{}|;:\'",.<>?/`~';
            const event = {
                type: EventType.ACTION_PRESS,
                detail: {
                    notification: { id: specialId },
                    pressAction: { id: 'default' },
                },
            };

            // When: Event is triggered
            await backgroundEventHandler?.(event);

            // Then: Should preserve special characters
            expect(consoleLogSpy).toHaveBeenCalledWith(
                '[BackgroundHandler] User pressed notification:',
                specialId
            );

            consoleLogSpy.mockRestore();
        });

        it('should handle notification with unicode characters in ID', async () => {
            // Given: Notification with unicode
            const consoleLogSpy = jest.spyOn(console, 'log').mockImplementation();
            const unicodeId = '通知-🚨-警报';
            const event = {
                type: EventType.ACTION_PRESS,
                detail: {
                    notification: { id: unicodeId },
                    pressAction: { id: 'default' },
                },
            };

            // When: Event is triggered
            await backgroundEventHandler?.(event);

            // Then: Should handle unicode correctly
            expect(consoleLogSpy).toHaveBeenCalledWith(
                '[BackgroundHandler] User pressed notification:',
                unicodeId
            );

            consoleLogSpy.mockRestore();
        });
    });

    describe('Async behavior', () => {
        beforeEach(() => {
            registerBackgroundNotificationHandler();
        });

        it('should handle handler as async function', async () => {
            // Given: ACTION_PRESS event
            const consoleLogSpy = jest.spyOn(console, 'log').mockImplementation();
            const event = {
                type: EventType.ACTION_PRESS,
                detail: {
                    notification: { id: 'async-test' },
                    pressAction: { id: 'default' },
                },
            };

            // When: We await the handler
            await backgroundEventHandler?.(event);

            // Then: Should complete successfully
            expect(consoleLogSpy).toHaveBeenCalled();

            consoleLogSpy.mockRestore();
        });

        it('should allow concurrent event handling', async () => {
            // Given: Multiple simultaneous events
            const consoleLogSpy = jest.spyOn(console, 'log').mockImplementation();
            const events = Array.from({ length: 5 }, (_, i) => ({
                type: EventType.ACTION_PRESS,
                detail: {
                    notification: { id: `concurrent-${i}` },
                    pressAction: { id: 'default' },
                },
            }));

            // When: Events are handled concurrently
            await Promise.all(events.map(event => backgroundEventHandler?.(event)));

            // Then: All should be processed
            expect(consoleLogSpy).toHaveBeenCalledTimes(5);

            consoleLogSpy.mockRestore();
        });
    });

    describe('Real-world scenarios', () => {
        beforeEach(() => {
            registerBackgroundNotificationHandler();
        });

        it('should simulate user tapping critical alert notification', async () => {
            // Simulate real scenario: user receives critical alert and taps it
            const consoleLogSpy = jest.spyOn(console, 'log').mockImplementation();
            const criticalAlertData = {
                id: 'earthquake-alert-001',
                hazardLevel: 'CRITICAL',
                title: 'Earthquake Detected',
                location: { latitude: 35.6762, longitude: 139.6503 },
            };

            const event = {
                type: EventType.ACTION_PRESS,
                detail: {
                    notification: {
                        id: 'notif-earthquake-001',
                        title: 'Earthquake Detected',
                        body: 'Magnitude 6.5 earthquake. Take cover immediately.',
                        data: {
                            alert: JSON.stringify(criticalAlertData),
                            contextMode: 'HEADS_UP',
                        },
                    },
                    pressAction: {
                        id: 'default',
                    },
                },
            };

            // When: User taps notification
            await backgroundEventHandler?.(event);

            // Then: Should be logged for processing
            expect(consoleLogSpy).toHaveBeenCalledWith(
                '[BackgroundHandler] User pressed notification:',
                'notif-earthquake-001'
            );

            consoleLogSpy.mockRestore();
        });

        it('should simulate user dismissing notification without interaction', async () => {
            // Simulate user swiping away notification
            const consoleLogSpy = jest.spyOn(console, 'log').mockImplementation();
            const event = {
                type: EventType.DISMISSED,
                detail: {
                    notification: {
                        id: 'dismissed-alert',
                    },
                },
            };

            // When: Notification is dismissed
            await backgroundEventHandler?.(event);

            // Then: Should not trigger any action
            expect(consoleLogSpy).not.toHaveBeenCalled();

            consoleLogSpy.mockRestore();
        });
    });
});