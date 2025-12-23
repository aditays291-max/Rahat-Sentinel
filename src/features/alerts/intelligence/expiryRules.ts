import { Alert } from '../../../types/alerts';

/**
 * Checks if an alert is expired based on the current time.
 *
 * @param alert - The alert to check
 * @param now - The current timestamp in milliseconds
 * @returns boolean - True if the alert is expired
 */
export const isAlertExpired = (alert: Alert, now: number): boolean => {
    return alert.expiresAt <= now;
};

/**
 * Filters out expired alerts from a list.
 *
 * @param alerts - List of alerts to filter
 * @param now - The current timestamp in milliseconds
 * @returns Alert[] - List of non-expired alerts
 */
export const removeExpiredAlerts = (alerts: Alert[], now: number): Alert[] => {
    return alerts.filter((alert) => !isAlertExpired(alert, now));
};
