import React from 'react';
import { createNativeStackNavigator } from '@react-navigation/native-stack';
import { AlertStack } from './AlertStack';
import { SettingsStack } from './SettingsStack';

const Stack = createNativeStackNavigator();

export const RootNavigator = () => {
    return (
        <Stack.Navigator screenOptions={{ headerShown: false }}>
            <Stack.Screen name="Alerts" component={AlertStack} />
            <Stack.Screen name="Settings" component={SettingsStack} />
        </Stack.Navigator>
    );
};
