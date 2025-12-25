# Alert Delivery Policy

This module defines the **Rules of Engagement** for interrupting the user with notifications.

## Philosophy: "Respect the User's Attention"
In a disaster zone, anxiety is high. If the app buzzes for every minor update (e.g., "Light wind"), the user will disable notifications—potentially missing the "Dam Failure" warning later.

We implement likely **Escalation Policy**:

### 1. Silent (Low Severity)
*   Updates that are informative but not urgent.
*   Example: "Weather Advisory", "Traffic Update".
*   Action: UI update only. No buzz.

### 2. Standard (Moderate Severity)
*   Updates the user should know about soon.
*   Example: "Road Closed nearby".
*   Action: Standard "Bing" sound.

### 3. Urgent (Severe Severity)
*   Threats to property or health.
*   Example: "Flash Flood Watch".
*   Action: High Priority Notification, Short Vibration.

### 4. Critical (Critical Severity)
*   Immediate threat to life.
*   Example: "Earthquake Detected", "Dam Failure".
*   Action: **ALARM** sound, **LONG** vibration, bypassing Do Not Disturb (future capability).

## Context-Aware Delivery (`deliveryContext.ts`)
We further refine behavior based on **App Visibility**:

| Hazard Type | Foreground (App Open) | Background (Phone Locked/Other App) |
| :--- | :--- | :--- |
| **CRITICAL** | **Heads-Up** (Interrupt) | **Heads-Up** (Interrupt) |
| **SEVERE** | **Silent** (Toast) | **Heads-Up** (High Priority) |
| **MODERATE** | ❌ None (List Update) | **Silent** (Tray Notification) |
| **LOW** | ❌ None | ❌ None |

### Rationale
*   **Foreground**: If the user is staring at the map, we don't need to buzz the phone for a "Road Closed" (Moderate) alert. They will see the pin appear. We *DO* need to interrupt them for a Dam Failure (Critical).
*   **Background**: If the phone is in their pocket, we must buzz for "Road Closed", or they will miss it.

## Implementation
The logic is encapsulated in `decideAlertDelivery(alert)`. This function returns a `DeliveryDecision` object (pure data), which the Platform Layer (Android/iOS) effectively "renders" into native API calls.
