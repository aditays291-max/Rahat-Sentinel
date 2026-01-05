import React, { useEffect, useState, useReducer } from 'react';
import { View, Text, ScrollView, StyleSheet, SafeAreaView, TouchableOpacity } from 'react-native';
import { useAlertStore } from '../store/alertStore';
import { getCurrentUserLocation } from '../features/locationAwareness/locationContext';
import { shouldSuppressAlert } from '../features/alerts/intelligence/suppressionRules';
import { calculateDistanceMeters } from '../features/location/geoRules';
import { isAlertExpired } from '../features/alerts/intelligence/expiryRules';
import { Alert } from '../types/alerts';

/**
 * Developer-only screen for inspecting the internal state of the Alert System.
 * 
 * WHY: "Logic" issues (why isn't my alert showing?) are hard to debug with logs alone.
 * This screen visualizes the decision matrix (Expiry, Suppression, Distance) in real-time.
 *
 * This screen provides insights into:
 * 1. Raw Store State (All alerts, including suppressed/expired)
 * 2. Active View (What the user actually sees)
 * 3. Logic Diagnostics (Why an alert is suppressed, TTL, Distance)
 */
export const DiagnosticsScreen = () => {
    // 1. Dev Guard
    if (!__DEV__) {
        return null;
    }

    // 2. Data Subscriptions
    const allAlerts = useAlertStore((state) => state.alerts);
    const getActiveAlerts = useAlertStore((state) => state.getActiveAlerts);
    const activeAlerts = getActiveAlerts();

    const userLocation = getCurrentUserLocation();

    // 3. Force re-render for timers (TTL)
    const [, forceUpdate] = useReducer((x) => x + 1, 0);

    useEffect(() => {
        const interval = setInterval(forceUpdate, 1000);
        return () => clearInterval(interval);
    }, []);

    // Helper to render a single alert card
    const renderAlertCard = (alert: Alert, isActiveView: boolean) => {
        const now = Date.now();
        const expired = isAlertExpired(alert, now);

        // Check suppression against others in the FULL list (if we are looking at raw view)
        // For active view, they are by definition not suppressed.
        const suppressed = !isActiveView && shouldSuppressAlert(alert, allAlerts);

        let statusColor = '#2ecc71'; // Green (Active)
        let statusText = 'ACTIVE';

        if (expired) {
            statusColor = '#95a5a6'; // Gray
            statusText = 'EXPIRED';
        } else if (suppressed) {
            statusColor = '#e67e22'; // Orange
            statusText = 'SUPPRESSED';
        }

        const ttlSeconds = Math.max(0, Math.floor((alert.expiresAt - now) / 1000));

        let distanceText = 'Unknown Loc';
        if (userLocation && alert.location) {
            const dist = calculateDistanceMeters(alert.location, userLocation);
            distanceText = isFinite(dist) ? `${Math.round(dist)}m` : 'Invalid Dist';
        }

        return (
            <View key={alert.id} style={[styles.card, { borderLeftColor: statusColor }]}>
                <View style={styles.cardHeader}>
                    <Text style={styles.cardId}>ID: {alert.id.slice(0, 8)}...</Text>
                    <View style={[styles.badge, { backgroundColor: statusColor }]}>
                        <Text style={styles.badgeText}>{statusText}</Text>
                    </View>
                </View>

                <Text style={styles.detailText}>Source: {alert.source}</Text>
                <Text style={styles.detailText}>Hazard: {alert.hazardLevel}</Text>
                <Text style={styles.detailText}>Dist: {distanceText}</Text>
                <Text style={[styles.detailText, styles.mono]}>TTL: {ttlSeconds}s</Text>
            </View>
        );
    };

    return (
        <SafeAreaView style={styles.container}>
            <ScrollView contentContainerStyle={styles.scrollContent}>
                <Text style={styles.header}>Diagnostics (DEV ONLY)</Text>

                {/* User Location Section */}
                <View style={styles.section}>
                    <Text style={styles.sectionTitle}>User Location (In-Memory)</Text>
                    {userLocation ? (
                        <Text style={styles.infoText}>
                            Lat: {userLocation.latitude.toFixed(6)}, Lon: {userLocation.longitude.toFixed(6)}
                        </Text>
                    ) : (
                        <Text style={[styles.infoText, { color: 'red' }]}>NULL (Unknown)</Text>
                    )}
                </View>

                {/* Active Alerts Section */}
                <View style={styles.section}>
                    <Text style={styles.sectionTitle}>
                        Active / Visible ({activeAlerts.length})
                    </Text>
                    <Text style={styles.subTitle}>Result of getActiveAlerts()</Text>
                    {activeAlerts.length === 0 ? (
                        <Text style={styles.emptyText}>No visible alerts</Text>
                    ) : (
                        activeAlerts.map(a => renderAlertCard(a, true))
                    )}
                </View>

                {/* Raw Store Section */}
                <View style={styles.section}>
                    <Text style={styles.sectionTitle}>
                        Raw Store State ({allAlerts.length})
                    </Text>
                    <Text style={styles.subTitle}>All persisted alerts</Text>
                    {allAlerts.length === 0 ? (
                        <Text style={styles.emptyText}>Store is empty</Text>
                    ) : (
                        allAlerts.map(a => renderAlertCard(a, false))
                    )}
                </View>
            </ScrollView>
        </SafeAreaView>
    );
};

const styles = StyleSheet.create({
    container: {
        flex: 1,
        backgroundColor: '#f5f6fa',
    },
    scrollContent: {
        padding: 16,
    },
    header: {
        fontSize: 24,
        fontWeight: 'bold',
        marginBottom: 16,
        color: '#2c3e50',
    },
    section: {
        marginBottom: 24,
        backgroundColor: '#fff',
        borderRadius: 8,
        padding: 12,
        shadowColor: '#000',
        shadowOpacity: 0.05,
        shadowRadius: 5,
        elevation: 2,
    },
    sectionTitle: {
        fontSize: 18,
        fontWeight: '600',
        marginBottom: 4,
        color: '#34495e',
    },
    subTitle: {
        fontSize: 12,
        color: '#7f8c8d',
        marginBottom: 12,
    },
    infoText: {
        fontSize: 14,
        fontFamily: 'monospace',
        color: '#2c3e50',
    },
    emptyText: {
        fontStyle: 'italic',
        color: '#95a5a6',
    },
    card: {
        backgroundColor: '#fafafa',
        padding: 12,
        borderRadius: 6,
        borderLeftWidth: 4,
        marginBottom: 8,
        borderWidth: 1,
        borderColor: '#eee',
    },
    cardHeader: {
        flexDirection: 'row',
        justifyContent: 'space-between',
        alignItems: 'center',
        marginBottom: 8,
    },
    cardId: {
        fontSize: 12,
        fontWeight: 'bold',
        color: '#7f8c8d',
    },
    badge: {
        paddingHorizontal: 8,
        paddingVertical: 4,
        borderRadius: 4,
    },
    badgeText: {
        color: '#fff',
        fontSize: 10,
        fontWeight: 'bold',
    },
    detailText: {
        fontSize: 12,
        marginBottom: 2,
        color: '#34495e',
    },
    mono: {
        fontFamily: 'monospace',
        color: '#e74c3c',
        marginTop: 4,
    },
});

export default DiagnosticsScreen;
