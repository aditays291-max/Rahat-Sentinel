# Bluetooth Mesh Feature

This feature module is responsible for handling **offline Peer-to-Peer (P2P) alert propagation** using Bluetooth Low Energy (BLE) Mesh networking. It allows devices to communicate and share critical alerts even when cellular networks and Internet connectivity are unavailable.

## Architecture

Data flows through this module as follows:

1.  **Raw Data In**: Encoded data packets are received via the Bluetooth hardware.
2.  **`MeshPayload`**: The raw data is deserialized into a strict `MeshPayload` structure.
3.  **Processing & Filtering**: The service checks:
    *   **De-duplication**: Have we seen this message ID before?
    *   **TTL (Time To Live)**: Should this message keep hopping?
4.  **`NormalizedMeshAlert`**: Valid payloads are converted into a standardized format (`NormalizedMeshAlert`) that matches the global application's `Alert` interface.
5.  **Global Store**: Finally, the normalized alert is dispatched to the global application state (outside this feature).

## Key Concepts

### MeshPayload
The network-level data contract. It includes metadata specifically for the mesh transport layer, such as `id` (transaction ID), `originDeviceId` (sender), and `ttl`.

### TTL (Time To Live)
An integer counter initialized by the sender.
*   **Purpose**: Prevents infinite loops in the network (e.g., Device A -> Device B -> Device A -> ...).
*   **Mechanism**: Each device decrements the TTL by 1 before relaying. If `ttl <= 0`, the message is dropped and not relayed further.

### De-duplication
A mechanism to ignore messages that have already been processed. Since mesh networks flood messages, a device may receive the same alert from multiple neighbors. We track message IDs to process each unique alert only once.

## Future Integration
Logic within `bluetoothService.ts` will eventually connect to a specific React Native Bluetooth library (like `react-native-ble-plx`) to send and receive bytes.

## Integration with Global Alert System

### Data Flow
1.  **Mesh Layer**: `receiveMeshPayload` ingests raw packets.
2.  **Normalization**: `normalizeMeshPayload` converts safe/defensive checks on the raw data (which is untrusted) into a strict `Alert` object.
    *   **Source**: Always set to `BLUETOOTH`.
    *   **Verification**: Always set to `false`. Since P2P data can be spoofed or unverified, the app treats it with caution until confirmed by other means (e.g., Satellite/Internet).
3.  **Store Dispatch**: Valid alerts are directly injected into the `useAlertStore`. This makes them immediately visible on the UI (Map/List) without the UI components needing to know they came from Bluetooth.

### Safety
The normalization process acts as a **corruption firewall**. It ensures that malformed or malicious data from the mesh network cannot crash the UI or corrupt the global store state.

## Edge Cases & Safety

### Duplicate Prevention
The service checks the global `alertStore` for existing IDs before dispatching. If a mesh node receives the same alert multiple times (common in flooding networks), it effectively ignores duplicates.

### Malformed Data
If a payload contains non-finite numbers for location or missing fields, `normalizeMeshPayload` returns `null`. This "silent failure" strategy is intentional; we prefer dropping a bad packet over crashing the background service during a disaster.


