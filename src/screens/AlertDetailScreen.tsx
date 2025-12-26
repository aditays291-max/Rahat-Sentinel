import React from 'react';
import { View, Text, StyleSheet } from 'react-native';
import { useRoute } from '@react-navigation/native';
import { useAlertStore } from '../store/alertStore';
import { HAZARD_METADATA } from '../features/alerts/presentation/hazardMetadata';

export const AlertDetailScreen = () => {
    const route = useRoute();
    const { alertId } = route.params as { alertId: string };

    // Retrieve the specific alert from the store
    const alert = useAlertStore((state) =>
        state.alerts.find((a) => a.id === alertId)
    );

    // Safety: Handle missing or expired alerts
    if (!alert) {
        return (
            <View style={styles.container}>
                <Text style={styles.errorText}>Alert not found or expired</Text>
            </View>
        );
    }

    const meta = HAZARD_METADATA[alert.hazardLevel];

    return (
        <View style={styles.container}>
            <Text style={styles.title}>{alert.title}</Text>

            <Text style={[styles.hazardLevel, { color: meta.color }]}>
                {meta.label}
            </Text>

            <Text style={styles.label}>Description:</Text>
            <Text style={styles.description}>{alert.description}</Text>

            <Text style={styles.label}>Source:</Text>
            <Text>{alert.source}</Text>

            <Text style={styles.label}>Timestamp:</Text>
            <Text>{new Date(alert.timestamp).toLocaleString()}</Text>
        </View>
    );
};

const styles = StyleSheet.create({
    container: {
        flex: 1,
        padding: 20,
    },
    title: {
        fontSize: 24,
        fontWeight: 'bold',
        marginBottom: 10,
    },
    hazardLevel: {
        fontSize: 18,
        fontWeight: 'bold',
        marginBottom: 20,
    },
    label: {
        fontSize: 14,
        fontWeight: '600',
        marginTop: 15,
        marginBottom: 5,
    },
    description: {
        fontSize: 16,
        lineHeight: 24,
    },
    errorText: {
        fontSize: 16,
        color: '#999',
        textAlign: 'center',
    },
});
