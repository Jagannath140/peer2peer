# Student P2P Resource Sharing System

A Java Swing-based application for sharing notes and PDFs directly between students without a central server.

## Features
- **Direct P2P File Transfer**: No central database or cloud storage.
- **Multi-threaded Server**: Handles multiple downloads/requests concurrently.
- **Real-time Status**: Check if a peer is Online/Offline.
- **Chunked File Streaming**: Efficient memory usage (4MB buffer) for large files.
- **In-Memory State**: strictly no database; peer lists reset on restart.

## How to Run

### Prerequisites
- Java JDK 8 or higher.
- A network (or localhost for testing).

### Running via Command Line

1. **Compile**:
   ```bash
   javac -d bin src/P2PApp.java src/core/*.java src/data/*.java src/model/*.java src/ui/*.java
   ```

2. **Run Instance 1 (Peer A)**:
   ```bash
   java -cp bin P2PApp
   ```
   - **Port**: 6000
   - **Folder**: shared_A (put some test files here)

3. **Run Instance 2 (Peer B)**:
   Open a new terminal and run:
   ```bash
   java -cp bin P2PApp
   ```
   - **Port**: 6001
   - **Folder**: shared_B

### Usage
1. In Peer A, go to **Peers** tab.
2. Add Peer B: Host `localhost`, Port `6001` -> Click **Add Peer**.
3. Click **Check Status** -> Should say **Online**.
4. Go to **Files** tab.
   - Click **Upload File** to add a PDF/Note from your computer to your shared folder.
   - Click **Refresh** to see your local files.
5. In "Remote Files", enter `localhost` and `6001`.
6. Click **Fetch List** -> See Peer B's files.
7. Select a file and click **Download Selected**.
