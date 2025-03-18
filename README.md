<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>P2P File Sharing Guide</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            line-height: 1.6;
            margin: 20px;
        }
        h1, h2, h3 {
            color: #333;
        }
        code {
            background-color: #f4f4f4;
            padding: 2px 5px;
            border-radius: 3px;
            font-family: monospace;
        }
        pre {
            background-color: #f4f4f4;
            padding: 10px;
            border-radius: 5px;
            overflow-x: auto;
        }
        .section {
            margin-bottom: 30px;
        }
    </style>
</head>
<body>
    <h1>P2P File Sharing Guide</h1>

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
