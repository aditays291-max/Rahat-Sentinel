import React from 'react';
import { View, Text, StyleSheet, ScrollView } from 'react-native';
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
                <View style={styles.errorContainer}>
                    <Text style={styles.errorText}>Alert not found or expired</Text>
                </View>
            </View>
        );
    }

    const meta = HAZARD_METADATA[alert.hazardLevel];

    return (
        <ScrollView style={styles.container} contentContainerStyle={styles.content}>
            {/* Title Section */}
            <Text style={styles.title}>{alert.title}</Text>

            {/* Hazard Severity Badge */}
            <View style={[styles.severityBadge, { backgroundColor: meta.color }]}>
                <Text style={styles.severityText}>{meta.label}</Text>
            </View>

            {/* Description Section */}
            <View style={styles.section}>
                <Text style={styles.sectionTitle}>Details</Text>
                <Text style={styles.description}>{alert.description}</Text>
            </View>

            {/* Metadata Section */}
            <View style={styles.section}>
                <Text style={styles.sectionTitle}>Information</Text>

                <View style={styles.metadataRow}>
                    <Text style={styles.metadataLabel}>Source:</Text>
                    <Text style={styles.metadataValue}>{alert.source}</Text>
                </View>

                <View style={styles.metadataRow}>
                    <Text style={styles.metadataLabel}>Time:</Text>
                    <Text style={styles.metadataValue}>
                        {new Date(alert.timestamp).toLocaleString()}
                    </Text>
                </View>
            </View>
        </ScrollView>
    );
};

const styles = StyleSheet.create({
    container: {
        flex: 1,
        backgroundColor: '#fff',
    },
    content: {
        padding: 20,
    },
    errorContainer: {
        flex: 1,
        justifyContent: 'center',
        alignItems: 'center',
    },
    errorText: {
        fontSize: 16,
        color: '#999',
        textAlign: 'center',
    },
    title: {
        fontSize: 28,
        fontWeight: 'bold',
        color: '#000',
        marginBottom: 16,
        lineHeight: 34,
    },
    severityBadge: {
        alignSelf: 'flex-start',
        paddingHorizontal: 16,
        paddingVertical: 8,
        borderRadius: 6,
        marginBottom: 24,
    },
    severityText: {
        color: '#fff',
        fontSize: 14,
        fontWeight: 'bold',
        letterSpacing: 0.5,
    },
    section: {
        marginBottom: 24,
    },
    sectionTitle: {
        fontSize: 12,
        fontWeight: '600',
        color: '#666',
        textTransform: 'uppercase',
        letterSpacing: 0.5,
        marginBottom: 12,
    },
    description: {
        fontSize: 16,
        lineHeight: 24,
        color: '#333',
    },
    metadataRow: {
        flexDirection: 'row',
        marginBottom: 8,
    },
    metadataLabel: {
        fontSize: 14,
        fontWeight: '600',
        color: '#666',
        width: 80,
    },
    metadataValue: {
        fontSize: 14,
        color: '#333',
        flex: 1,
    },
});

export default AlertDetailScreen;
