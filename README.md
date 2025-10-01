# P2P File Sharing Guide

## How To Use

### Basic Program Compilation
1. Navigate to the `src` folder and run the following command:
   ```bash
   javac -cp . ./Main/Main.java ./discovery/.java ./discovery/messages/.java ./p2p/.java ./testing/.java
   ```
2. Start the central server:
   ```bash
   java -cp . Main.Main central <!CentralServerIP!> <!CentralServerPort!>
   ```
3. Start a peer:
   ```bash
   java -cp . Main.Main peer <!PeerIP!> <!PeerPort!> <!CentralServerIP!> <!CentralServerPort!>
   ```

---

## How To Upload a File
1. Run the peer command as shown above.
2. Use the following command to upload a file:
   ```bash
   upload <!FILEPATHINOS!>
   ```

---

## How To Download a File
1. Run the peer command as shown above.
2. Use the following command to download a file:
   ```bash
   download
   ```

---

## How to Test
1. In the `test` directory, add a file containing the paths of all the files to be tested.
2. Run the following command to start testing:
   ```bash
   java -cp . Main.Main testing <!test Name!>
   ```
   > Currently, the test will use a three-node setup to perform the testing.




## Project Structure

- `src/`
  - `Main/`
    - `Main.java`: Entry point. Parses CLI, adapts peer/central roles, and user commands.
  - `discovery/`
    - `CentralRegistry.java`: Central server logic for file and peer discovery.
    - `Handshake.java`: Session setup and request/response coordination between peers and central.
    - `FileData.java`: Metadata for files (name, size, hash) used across discovery and transfer.
    - `Node.java`: Represents a peer (IP, port, identity).
    - `messages/`: Serializable message types exchanged over the network
      - `CentralRegistryRequest.java`, `CentralRegistryResponse.java`
      - `FileRequest.java`, `FileResponse.java`
      - `TransferRequest.java`, `TransferResponse.java`
      - `BroadcastBeacon.java`: Beacon used for UDP broadcast transfers.
  - `p2p/`
    - `ConnectionHandlerSequential.java`: Encrypted sequential TCP file send/receive.
    - `ConnectionHandlerParallel.java`: Simpler TCP send/receive variant.
    - `FileReciever.java`: High-level download flow (queries registry, selects peer, saves to downloads dir).
    - `ObjectTransfer.java`: Object serialization over TCP and UDP broadcast helpers.
    - `BroadCastTransfer.java`: File broadcasting and reception over UDP using beacons.
  - `testing/`
    - `FileTesting.java`: Test harness that spins up central, sender, receiver from a list of file paths.
  - `utils/`
    - `UserExperience.java`: Console progress bar utilities for transfers.
    - `Config.java`: Centralized config loader; provides `getTestDir()` and `getDownloadsDir()`.
  - `test/`
    - Test input lists (e.g., `unit.txt`, `big.txt`, `encryption.txt`) consumed in testing mode.
  - `downloads/`
    - Default downloads target directory (configurable via `config.properties`).

- `bin/`
  - Compiled `.class` files organized mirroring `src/` packages.

- `config.properties`
  - Repository-level configuration for directories:
    - `test.dir`: Directory containing test list files (default `./src/test`).
    - `downloads.dir`: Directory where downloads are saved (default `./src/downloads`).
