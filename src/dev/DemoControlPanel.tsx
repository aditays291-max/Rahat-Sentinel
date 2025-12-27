import React from 'react';
import { View, Text, TouchableOpacity, StyleSheet } from 'react-native';
import { useAlertStore } from '../store/alertStore';
import { Alert, AlertSource, HazardLevel } from '../types/alerts';

/**
 * DEV-only control panel for live alert injection during demos.
 * Enables presenters to trigger alerts on-demand without waiting for real sources.
 */
export const DemoControlPanel = () => {
    const addAlert = useAlertStore((state) => state.addAlert);
    const clearAlerts = () => {
        // Access store directly to clear all alerts
        useAlertStore.setState({ alerts: [] });
    };

    const triggerCriticalAlert = () => {
        const now = Date.now();
        const alert: Alert = {
            id: `demo-critical-${now}`,
            source: AlertSource.CELL,
            hazardLevel: HazardLevel.CRITICAL,
            title: 'LIVE DEMO: Earthquake Detected',
            description: 'Magnitude 6.5 earthquake detected. This is a demonstration alert for testing purposes.',
            location: {
                latitude: 27.7172,
                longitude: 85.3240,
                radius: 30000,
            },
            timestamp: now,
            expiresAt: now + 2 * 60 * 60 * 1000, // 2 hours
            verified: true,
        };
        addAlert(alert);
    };

    const triggerSevereAlert = () => {
        const now = Date.now();
        const alert: Alert = {
            id: `demo-severe-${now}`,
            source: AlertSource.CELL,
            hazardLevel: HazardLevel.SEVERE,
            title: 'LIVE DEMO: Flash Flood Warning',
            description: 'Rapid water level rise detected in nearby river. This is a demonstration alert for testing purposes.',
            location: {
                latitude: 27.6915,
                longitude: 85.3158,
                radius: 20000,
            },
            timestamp: now,
            expiresAt: now + 3 * 60 * 60 * 1000, // 3 hours
            verified: true,
        };
        addAlert(alert);
    };

    return (
        <View style={styles.container}>
            <Text style={styles.label}>DEV Controls</Text>
            <View style={styles.buttonRow}>
                <TouchableOpacity style={[styles.button, styles.criticalButton]} onPress={triggerCriticalAlert}>
                    <Text style={styles.buttonText}>🚨 CRITICAL</Text>
                </TouchableOpacity>
                <TouchableOpacity style={[styles.button, styles.severeButton]} onPress={triggerSevereAlert}>
                    <Text style={styles.buttonText}>⚠️ SEVERE</Text>
                </TouchableOpacity>
                <TouchableOpacity style={[styles.button, styles.clearButton]} onPress={clearAlerts}>
                    <Text style={styles.buttonText}>🗑️ CLEAR</Text>
                </TouchableOpacity>
            </View>
        </View>
    );
};

const styles = StyleSheet.create({
    container: {
        position: 'absolute',
        bottom: 0,
        left: 0,
        right: 0,
        backgroundColor: 'rgba(0, 0, 0, 0.8)',
        padding: 10,
        borderTopWidth: 2,
        borderTopColor: '#FFD700',
    },
    label: {
        color: '#FFD700',
        fontSize: 10,
        fontWeight: 'bold',
        marginBottom: 5,
        textAlign: 'center',
    },
    buttonRow: {
        flexDirection: 'row',
        justifyContent: 'space-around',
    },
    button: {
        paddingVertical: 8,
        paddingHorizontal: 12,
        borderRadius: 5,
        minWidth: 100,
    },
    criticalButton: {
        backgroundColor: '#e74c3c',
    },
    severeButton: {
        backgroundColor: '#e67e22',
    },
    clearButton: {
        backgroundColor: '#95a5a6',
    },
    buttonText: {
        color: '#fff',
        fontSize: 12,
        fontWeight: 'bold',
        textAlign: 'center',
    },
});
