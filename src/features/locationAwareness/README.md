# Location Awareness Feature

This module provides controlled, in-memory tracking of the user's location to enable dynamic alert relevance filtering.

## Why Separate from GPS?
We separate the **State** (`currentLocation`) from the **Source** (Native GPS API) for several reasons:
1.  **Testability**: We can easily inject mock locations during testing without mocking native modules.
2.  **Performance**: High-frequency GPS updates (1Hz) should not trigger global store updates or React re-renders unless necessary. We store the raw location here and only "commit" relevant changes to the UI layer when needed.
3.  **Permissions**: This module operates purely on data provided to it, making it resilient to permission failures (it simply stays `null`).

## Integration with Alert Intelligence
This module feeds into the `relevanceEngine`, which uses the core `locationService` logic.
-   **Input**: Global Alert List + Current Location
-   **Output**: Filtered Relevant List

## Stale Alert Prevention
By re-calculating relevance whenever the user moves significantly, we ensure that:
-   **Entering a Hazard Zone**: Alerts that were previously "irrelevant" (too far) suddenly appear.
-   **Leaving a Hazard Zone**: Alerts disappear from the active view, reducing cognitive load.
