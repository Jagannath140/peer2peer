# P2P Resource Sharing System - Architecture & Design

## 1. High-Level Architecture (P2P Hybrid)

```ascii
      +----------------+            +----------------+
      |    Peer A      |            |    Peer B      |
      |   (Student)    |            |   (Student)    |
      +-------+--------+            +-------+--------+
              |                             |
      +-------v--------+            +-------v--------+
      |      UI        |            |      UI        |
      |  (MainFrame)   |            |  (MainFrame)   |
      +-------+--------+            +-------+--------+
              |                             |
      +-------v---------+           +-------v---------+
      |  Networking     |<--------->|  Networking     |
      | (Server/Client) |   TCP     | (Server/Client) |
      +-------+---------+           +-------+---------+
              |                             |
      +-------v---------+           +-------v---------+
      | In-Memory Data  |           | In-Memory Data  |
      | (PeerMap, Files)|           | (PeerMap, Files)|
      +-------+---------+           +-------+---------+
              |                             |
      +-------v---------+           +-------v---------+
      |   Local Files   |           |   Local Files   |
      |     (Disk)      |           |     (Disk)      |
      +-----------------+           +-----------------+
```

## 2. Peer Components

### A. Networking Layer (Socket Programming)
- **PeerServer (`core.PeerServer`)**: A multi-threaded TCP server listening on a specific port (default 6000). It accepts incoming connections and delegates them to `ClientHandler`.
- **ClientHandler (`core.ClientHandler`)**: A runnable task that handles individual peer requests (`HELLO`, `LIST_FILES`, `GET_FILE`). It ensures non-blocking handling of multiple peers.
- **PeerClient (`core.PeerClient`)**: A utility to initiate outgoing connections to other peers to fetch lists or download files.

### B. Data Management (In-Memory)
- **PeerManager (`data.PeerManager`)**: Maintains a thread-safe list (`CopyOnWriteArrayList`) of known peers and their online status. No database is used; this list is reset on restart, mimicking a volatile distributed node.
- **FileManager (`data.FileManager`)**: Scans the local shared folder and maintains a map of available files. It also temporarily caches remote file lists for display.

### C. UI Layer (Java Swing)
- **MainFrame**: The container for the application.
- **PeerPanel**: Allows adding peers by IP:Port and checking their status.
- **FilePanel**: Displays local files and allows fetching/downloading files from remote peers.

## 3. Interaction Flow

### Handshake (HELLO)
1. Peer A adds Peer B (IP:Port).
2. Peer A sends `HELLO`.
3. Peer B responds `OK`.
4. Peer A marks Peer B as "Online".

### File Discovery (LIST_FILES)
1. Peer A requests file list from Peer B.
2. Peer B scans its local directory.
3. Peer B sends count and list of `filename:size`.
4. Peer A updates its UI with Peer B's files.

### File Transfer (GET_FILE) - Chunked Streaming
1. Peer A selects "notes.pdf" from Peer B's list.
2. Peer A sends `GET_FILE notes.pdf`.
3. Peer B checks existence, sends `OK <size>`.
4. Peer B opens file stream, reads 4KB chunks, and writes to socket.
5. Peer A reads chunks and writes to local disk until <size> is reached.
**(Note: The entire file is never loaded into RAM, adhering to memory constraints.)**

## 4. Key Implementation Details

### Why In-Memory Storage?
For a pure P2P system, especially in a lab/educational context, in-memory storage demonstrates the concept of **volatile state**. Peers are transient; if a peer leaves, its state should not persist centrally. This enforces the distributed nature where each peer is responsible for its own availability.

### Concurrency Handling
- **Server**: Uses `ExecutorService` (CachedThreadPool) to handle multiple incoming download/list requests simultaneously without blocking the UI or other connections.
- **UI**: Uses `SwingUtilities.invokeLater` to update the GUI from background network threads, ensuring responsiveness.
- **Data Structures**: Uses `ConcurrentHashMap` and `CopyOnWriteArrayList` to prevent `ConcurrentModificationException` when the UI reads data while the network layer updates it.

### Fault Tolerance
- **Timeouts**: Socket connections have `CONNECT_TIMEOUT` (3s) and `READ_TIMEOUT` (5s) to prevent hanging if a peer crashes or network fails.
- **Error Handling**: `try-catch` blocks ensure that a single failed transfer does not crash the application. The UI reports "Download Failed" or "Offline" instead of crashing.
