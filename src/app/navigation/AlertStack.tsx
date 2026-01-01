import React from 'react';
import { createNativeStackNavigator } from '@react-navigation/native-stack';

import AlertFeedScreen from '../../screens/AlertFeedScreen';
import AlertDetailScreen from '../../screens/AlertDetailScreen';
import AlertMapScreen from '../../screens/AlertMapScreen';
import DiagnosticsScreen from '../../screens/DiagnosticsScreen';

export type AlertStackParamList = {
  AlertFeed: undefined;
  AlertDetail: { alertId: string };
  AlertMap: undefined;
  Diagnostics: undefined;
};

const Stack = createNativeStackNavigator<AlertStackParamList>();

export default function AlertStack() {
  return (
    <Stack.Navigator
      initialRouteName="AlertFeed"
      screenOptions={{ headerShown: false }}
    >
      <Stack.Screen name="AlertFeed" component={AlertFeedScreen} />
      <Stack.Screen name="AlertDetail" component={AlertDetailScreen} />
      <Stack.Screen name="AlertMap" component={AlertMapScreen} />
      {__DEV__ && (
        <Stack.Screen name="Diagnostics" component={DiagnosticsScreen} />
      )}
    </Stack.Navigator>
  );
}
