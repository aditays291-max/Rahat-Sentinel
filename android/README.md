# RAHAT: Emergency Mesh & Disaster Response

RAHAT is a decentralized emergency communication platform designed to provide critical assistance during disasters when cellular networks fail. This repository contains the Android implementation focusing on a resilient Bluetooth Low Energy (BLE) mesh network and location-aware safety features.

## 🚀 Key Features & Stability Improvements

### 1. Resilient BLE Mesh (Legacy Hardened)
- **Universal Discovery**: Enforced **Legacy LE_1M** advertising to ensure 100% compatibility across all Android hardware (older and specialized devices).
- **Auto-Recovery**: Integrated Bluetooth state monitoring. The mesh network automatically restarts and recovers discovery within seconds of a Bluetooth toggle.
- **Finite Scanning Windows**: Optimized discovery loop (15s on / 10s off) to maximize battery life while maintaining high-frequency peer updates.
- **Peer Lifecycle Management**: Implemented `PeerManager` with median RSSI filtering and a 60-second TTL to ensure only active, nearby devices are displayed.

### 2. Hardened Security & Identity
- **128-bit Ephemeral IDs**: Replaced basic IDs with HMAC-SHA256 derived 128-bit EphIDs, ensuring user privacy and mesh security.
- **Dynamic Rotation**: EphIDs rotate every **10 minutes** linked to the device's Keystore-backed master secret.
- **SOS Payload**: Hardened advertising payload includes explicit status flags for instant SOS recognition without requiring a full connection.

### 3. Location & Map Stability
- **Instant Map Centering**: Resolved the "Africa Default" bug. The map now recovers the user's last known location immediately on launch.
- **Centering Gate**: Implemented a one-time centering logic to prevent jitter and UI instability during movement.
- **Approximate Distance Rings**: For peers without GPS visibility, the map renders relative distance rings based on signal strength (RSSI) trends.

### 4. Safety & UX
- **Permission Gate**: A "Loud Failure" UI enforces mandatory Bluetooth and Location permissions, preventing app usage in an unsafe state.
- **Prioritized Alert Feed**: Real-time filtering and sorting of nearby devices based on SOS status and signal proximity.

## 🛠 Architecture

- **`MeshRepository`**: Single Source of Truth (SSOT) for all discovered peers, consumed by the Map, Alert Feed, and Nearby Help screens.
- **`PeerManager`**: The "Brain" that processes raw RSSI data, performs trend analysis (Approaching vs. Receding), and manages peer expiry.
- **`BleScanner` & `BleAdvertiser`**: Low-level communication layer optimized for maximum penetration and discovery speed.
- **`IdentityManager`**: Manages secure hardware-backed secrets and ephemeral ID generation.

## 📦 Building and Running

1. **Prerequisites**: Android Studio Ladybug+ and an Android device (API 26+ recommended for `AdvertisingSet` support).
2. **Build**:
   ```bash
   ./gradlew assembleDebug
   ```
3. **Install**:
   ```bash
   ./gradlew installDebug
   ```

## 📜 Recent Verification
- [x] Verified build success on multiple devices.
- [x] Confirmed 128-bit EphID discovery in local mesh logs.
- [x] Validated auto-recovery of mesh service on Bluetooth state transitions.
