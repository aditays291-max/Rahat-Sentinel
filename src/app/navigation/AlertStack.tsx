import React from 'react';
import { createNativeStackNavigator } from '@react-navigation/native-stack';
import { AlertFeedScreen } from '../../screens/AlertFeedScreen';
import { AlertDetailScreen } from '../../screens/AlertDetailScreen';
import { AlertMapScreen } from '../../screens/AlertMapScreen';

const Stack = createNativeStackNavigator();

export const AlertStack = () => {
    return (
        <Stack.Navigator>
            <Stack.Screen name="AlertFeed" component={AlertFeedScreen} />
            <Stack.Screen name="AlertDetail" component={AlertDetailScreen} />
            <Stack.Screen name="AlertMap" component={AlertMapScreen} />
        </Stack.Navigator>
    );
};
