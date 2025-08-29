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



