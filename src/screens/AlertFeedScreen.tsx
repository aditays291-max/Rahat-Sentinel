import React from 'react';
import { View, Text, StyleSheet, Pressable } from 'react-native';
import { useNavigation } from '@react-navigation/native';
import { useAlertStore } from '../store/alertStore';
import { sortAlertsForDisplay } from '../features/alerts/presentation/sortRules';
import { filterAlertsForDisplay } from '../features/alerts/presentation/filterRules';
import { HAZARD_METADATA } from '../features/alerts/presentation/hazardMetadata';
import { Alert } from '../types/alerts';

// Selector Logic (Composed from Presentation Rules)
// This keeps the component "dumb" and logic-free.
const getPresentedAlerts = (state: { alerts: Alert[] }) => {
    // 1. Sort (Critical First, Recent First)
    const sorted = sortAlertsForDisplay(state.alerts);
    // 2. Filter (Cap capacity, ensure safety)
    return filterAlertsForDisplay(sorted);
};

export const AlertFeedScreen = () => {
    const navigation = useNavigation();
    // Consumption: specific selector for presentation
    const { visible, hidden } = useAlertStore(getPresentedAlerts);

    return (
        <View style={styles.container}>
            {/* DEV-only: Diagnostics button */}
            {__DEV__ && (
                <Pressable
                    style={styles.devButton}
                    onPress={() => navigation.navigate('Diagnostics')}
                >
                    <Text style={styles.devButtonText}>🔧 Diagnostics</Text>
                </Pressable>
            )}

            {/* Empty State */}
            {visible.length === 0 && (
                <View style={styles.emptyState}>
                    <Text style={styles.emptyText}>No active alerts</Text>
                </View>
            )}

            {/* List of Visible Alerts */}
            {visible.map((alert) => {
                const meta = HAZARD_METADATA[alert.hazardLevel];
                return (
                    <Pressable
                        key={alert.id}
                        style={styles.alertItem}
                        onPress={() => navigation.navigate('AlertDetail', { alertId: alert.id })}
                    >
                        <Text style={styles.title}>{alert.title}</Text>
                        <Text style={{ color: meta.color, fontWeight: 'bold' }}>
                            {meta.label}
                        </Text>
                        <Text>{alert.source}</Text>
                        <Text>{new Date(alert.timestamp).toLocaleTimeString()}</Text>
                    </Pressable>
                );
            })}

            {/* Hidden Count Summary */}
            {hidden.length > 0 && (
                <Text style={styles.hiddenText}>{hidden.length} more alerts hidden</Text>
            )}
        </View>
    );
};

// No heavy styling as per instructions (just structural/basic)
const styles = StyleSheet.create({
    container: {
        flex: 1,
        padding: 20,
    },
    devButton: {
        backgroundColor: '#FFD700',
        padding: 10,
        borderRadius: 5,
        marginBottom: 15,
        alignSelf: 'flex-start',
    },
    devButtonText: {
        fontSize: 12,
        fontWeight: 'bold',
        color: '#000',
    },
    emptyState: {
        flex: 1,
        justifyContent: 'center',
        alignItems: 'center',
    },
    emptyText: {
        fontSize: 16,
        color: '#999',
    },
    alertItem: {
        marginBottom: 10,
        padding: 10,
        borderWidth: 1,
        borderColor: '#ccc',
    },
    title: {
        fontSize: 16,
        fontWeight: '600',
    },
    hiddenText: {
        marginTop: 10,
        fontStyle: 'italic',
    },
});
