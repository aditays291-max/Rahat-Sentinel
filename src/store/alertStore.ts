import { create } from 'zustand';
import { Alert } from '../types/alerts';

interface AlertState {
    alerts: Alert[];
    addAlert: (alert: Alert) => void;
    removeAlert: (id: string) => void;
    clearExpiredAlerts: (currentTime: number) => void;
}

export const useAlertStore = create<AlertState>((set) => ({
    alerts: [],
    addAlert: (alert) =>
        set((state) => ({
            alerts: [...state.alerts, alert],
        })),
    removeAlert: (id) =>
        set((state) => ({
            alerts: state.alerts.filter((alert) => alert.id !== id),
        })),
    clearExpiredAlerts: (currentTime) =>
        set((state) => ({
            alerts: state.alerts.filter((alert) => alert.expiresAt > currentTime),
        })),
}));
