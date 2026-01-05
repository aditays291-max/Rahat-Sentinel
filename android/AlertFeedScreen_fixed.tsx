import React from 'react';
import {
    View,
    Text,
    FlatList,
    StyleSheet,
    TouchableOpacity,
    SafeAreaView,
} from 'react-native';
import { useAlertStore } from '../store/alertStore';

export const AlertFeedScreen = () => {
    // Fix: Use selector function directly without wrapper
    const alerts = useAlertStore((state) => state.alerts);

    const renderAlert = ({ item }: any) => (
        <TouchableOpacity
            style={styles.alertCard}
            onPress={() => console.log('[DEBUG] Alert pressed:', item.id)}
        >
            <Text style={styles.alertTitle}>{item.title}</Text>
            <Text style={styles.alertSeverity}>{item.severity}</Text>
            <Text style={styles.alertSource}>{item.source}</Text>
            <Text style={styles.alertTime}>{item.timestamp}</Text>
        </TouchableOpacity>
    );

    return (
        <SafeAreaView style={styles.container}>
            <View style={styles.header}>
                <Text style={styles.headerTitle}>Alert Feed</Text>
            </View>
            <FlatList
                data={alerts}
                renderItem={renderAlert}
                keyExtractor={(item) => item.id}
                contentContainerStyle={styles.listContainer}
            />
        </SafeAreaView>
    );
};

const styles = StyleSheet.create({
    container: {
        flex: 1,
        backgroundColor: '#f5f5f5',
    },
    header: {
        padding: 16,
        backgroundColor: '#fff',
        borderBottomWidth: 1,
        borderBottomColor: '#e0e0e0',
    },
    headerTitle: {
        fontSize: 24,
        fontWeight: 'bold',
    },
    listContainer: {
        padding: 16,
    },
    alertCard: {
        backgroundColor: '#fff',
        padding: 16,
        marginBottom: 12,
        borderRadius: 8,
        borderLeftWidth: 4,
        borderLeftColor: '#ff6b6b',
    },
    alertTitle: {
        fontSize: 16,
        fontWeight: 'bold',
        marginBottom: 4,
    },
    alertSeverity: {
        fontSize: 14,
        color: '#ff6b6b',
        marginBottom: 4,
    },
    alertSource: {
        fontSize: 12,
        color: '#666',
        marginBottom: 4,
    },
    alertTime: {
        fontSize: 12,
        color: '#999',
        fontStyle: 'italic',
    },
});
