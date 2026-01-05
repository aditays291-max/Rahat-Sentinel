import React, { useEffect } from 'react';
import { NavigationContainer } from '@react-navigation/native';
import { SafeAreaProvider } from 'react-native-safe-area-context';
import RootNavigator from './navigation/RootNavigator';
import { useAlertStore } from '../store/alertStore';
import { useUserStore } from '../store/userStore';
import { DEMO_ALERTS } from '../dev/demoAlerts';
import { DEMO_USERS } from '../dev/demoUsers';
import { DemoControlPanel } from '../dev/DemoControlPanel';
import { LogBox } from 'react-native';

// Suppress known warnings
LogBox.ignoreLogs([
    'The result of getSnapshot should be cached',
    'Open debugger to view warnings',
]);

const App = () => {
    // DEV-only: Seed demo alerts and users on app start
    useEffect(() => {
        if (__DEV__) {
            const addAlert = useAlertStore.getState().addAlert;
            const addUser = useUserStore.getState().addUser;

            DEMO_ALERTS.forEach((alert) => addAlert(alert));
            console.log('[DEV] Seeded', DEMO_ALERTS.length, 'demo alerts');

            DEMO_USERS.forEach((user) => addUser(user));
            console.log('[DEV] Seeded', DEMO_USERS.length, 'demo users');
        }
    }, []); // Empty deps = runs once on mount

    return (
        <SafeAreaProvider>
            <NavigationContainer>
                <RootNavigator />
            </NavigationContainer>
            {/* DEV-only: Demo control panel for live alert injection */}
            {__DEV__ && <DemoControlPanel />}
        </SafeAreaProvider>
    );
};

export default App;
