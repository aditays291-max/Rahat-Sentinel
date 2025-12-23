# Alert Intelligence

This module provides pure, deterministic utility functions for managing alert intelligence, specifically focusing on deduplication, expiry, and severity suppression.

## Purpose

Disaster data often comes from multiple sensors or reports, leading to noise. To prevent panic and ensure clarity, we need to:
1.  **Deduplicate**: Combine identical or highly similar reports into a single alert.
2.  **Expire**: Remove outdated information to keep the map relevant.
3.  **Suppress**: Prioritize severe alerts over lower-priority ones in the same area to avoid clutter.

## Logic Overview

### Deduplication (`dedupRules.ts`)
-   **Equivalence**: Alerts are considered same if they share the source, are within 300m, and occur within 10 minutes of each other.
-   **Strategy**: Keeps the most recent alert from a set of duplicates.

### Expiry (`expiryRules.ts`)
-   Simple check against a provided `now` timestamp.
-   No internal timers; time must be passed in to ensure testability.

### Suppression (`suppressionRules.ts`)
-   **Hierarchy**: `CRITICAL` > `SEVERE` > `MODERATE` > `LOW`.
-   **Rule**: Lower severity alerts are suppressed if a higher severity, non-expired alert exists within 500m.
-   **Safety**: `CRITICAL` alerts are NEVER suppressed.

## Usage

These are pure functions. They do not modify state directly. Use them within a store or service layer.

```typescript
const cleanAlerts = deduplicateAlerts(rawAlerts);
const activeAlerts = removeExpiredAlerts(cleanAlerts, Date.now());
// ... apply suppression logic
```
