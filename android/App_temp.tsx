import React, { useEffect } from 'react';
import { NavigationContainer } from '@react-navigation/native';
import { SafeAreaProvider } from 'react-native-safe-area-context';
import RootNavigator from './navigation/RootNavigator';
import { useAlertStore } from '../store/alertStore';
import { useUserStore } from '../store/userStore';
import { DEMO_ALERTS } from '../dev/demoAlerts';
import { DEMO_USERS } from '../dev/demoUsers';
import { DemoControlPanel } from '../dev/DemoControlPanel';

const App = () => {
    // DEV-only: Seed demo alerts on app start
    useEffect(() => {
        if (__DEV__) {
            const addAlert = useAlertStore.getState().addAlert;
            DEMO_ALERTS.forEach((alert) => addAlert(alert));
            console.log('[DEV] Seeded', DEMO_ALERTS.length, 'demo alerts');

            const addUser = useUserStore.getState().addUser;
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
