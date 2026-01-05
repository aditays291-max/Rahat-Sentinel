import React from 'react';
import { createNativeStackNavigator } from '@react-navigation/native-stack';
import { AlertFeedScreen } from '../../screens/AlertFeedScreen';

const Stack = createNativeStackNavigator();

export default function AlertStack() {
    return (
        <Stack.Navigator>
            <Stack.Screen
                name="AlertFeed"
                component={AlertFeedScreen}
                options={{ title: 'Alerts' }}
            />
        </Stack.Navigator>
    );
}
