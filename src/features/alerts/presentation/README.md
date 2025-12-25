# Alert Presentation Logic

This module defines the **Presentation Semantics** and **Sorting Logic** for alerts. It is purely functional and contains no UI components.

## Why Separate Logic from UI?
1.  **Consistency**: The same sorting rules (`sortRules.ts`) apply to the Map List, Notification Tray, and Watch App (future).
2.  **Safety**: We enforce that **Critical** alerts always appear at the top via unit-testable logic, rather than relying on `flex-direction` or arbitrary render order.
3.  **Themability**: Colors and labels (`hazardMetadata.ts`) are centralized, making it easy to support Dark Mode or Colorblind Modes in the future by swapping one file.

## Design Philosophy: "Emergency First"
Our sorting algorithm prioritizes **Severity over Recency**. 
-   Usually, social apps show "Newest First". 
-   We show "Most Dangerous First". 
-   *Reasoning*: If a user wakes up to 50 alerts, the one saying "Evacuate Now" (even if 1 hour old) must be seen before "Light Rain" (1 minute old).

## Capping Strategy (`filterRules.ts`)
In a disaster, users can be flooded with 100+ alerts. Showing all of them causes **Decision Paralysis**.
We implement a "Capping" strategy to reduce panic:

1.  **Critical Override**: `CRITICAL` alerts ignore all limits. If there are 10 Dam Failures, the user sees all 10.
2.  **Visible Cap**: For non-critical items, we show max 5.
3.  **Noise Cap**: Within those 5, only 2 can be "Low Priority". This reserves 3 slots for "Severe" items.

This hierarchy ensures that *Alert Clutter* never hides *Life-Safety Information*.

