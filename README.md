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



    <div class="section">
        <h2>How To Use</h2>
        <h3>Basic Program Compilation</h3>
        <ol>
            <li>
                Navigate to the <code>src</code> folder and run the following command:
                <pre><code>javac -cp . ./Main/Main.java ./discovery/.java ./discovery/messages/.java ./p2p/.java ./testing/.java</code></pre>
            </li>
            <li>
                Start the central server:
                <pre><code>java -cp . Main.Main central &lt;!CentralServerIP!&gt; &lt;!CentralServerPort!&gt;</code></pre>
            </li>
            <li>
                Start a peer:
                <pre><code>java -cp . Main.Main peer &lt;!PeerIP!&gt; &lt;!PeerPort!&gt; &lt;!CentralServerIP!&gt; &lt;!CentralServerPort!&gt;</code></pre>
            </li>
        </ol>
    </div>

    <div class="section">
        <h2>How To Upload a File</h2>
        <ol>
            <li>Run the peer command as shown above.</li>
            <li>Use the following command to upload a file:
                <pre><code>upload &lt;!FILEPATHINOS!&gt;</code></pre>
            </li>
        </ol>
    </div>

    <div class="section">
        <h2>How To Download a File</h2>
        <ol>
            <li>Run the peer command as shown above.</li>
            <li>Use the following command to download a file:
                <pre><code>download</code></pre>
            </li>
        </ol>
    </div>

    <div class="section">
        <h2>How to Test</h2>
        <ol>
            <li>In the <code>test</code> directory, add a file containing the paths of all the files to be tested.</li>
            <li>Run the following command to start testing:
                <pre><code>java -cp . Main.Main testing &lt;!test Name!&gt;</code></pre>
                <p>Currently, the test will use a three-node setup to perform the testing.</p>
            </li>
        </ol>
    </div>
</body>
</html>
