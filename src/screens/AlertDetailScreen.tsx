import React from 'react';
import { View, Text, StyleSheet } from 'react-native';

export const AlertDetailScreen = () => {
    return (
        <View style={styles.container}>
            <Text>Alert Detail Screen</Text>
        </View>
    );
};

const styles = StyleSheet.create({
    container: {
        flex: 1,
        justifyContent: 'center',
        alignItems: 'center',
    },
});
