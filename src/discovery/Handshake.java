package discovery;

import discovery.messages.*;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import p2p.ConnectionHandlerSequential;
import p2p.ObjectTransfer;

public class Handshake {

    private static Node client;
    private static Node CentralRegistry;

    // Maps file hash -> FileData, file hash -> path, file hash -> passkey
    private static Map<String, FileData> HashtoFD = new HashMap<>();
    private static Map<String, String> HashtoPath = new HashMap<>();
    private static Map<String, String> HashtoPasskey = new HashMap<>();

    // Start the peer server to listen for file requests
    public static void start(int port, Node c) {
        client = c;
        System.out.println("handshake on " + port);

        while (true) {
            try (ServerSocket serverSocket = new ServerSocket(client.getPeerPort())) {
                Socket socket = serverSocket.accept();
                Object obj = ObjectTransfer.receiveObject(socket);

                if (obj instanceof FileRequest) {
                    FileRequest req = (FileRequest) obj;

                    if (HashtoFD.containsKey(req.FileData.getFileHash())) {

                        // Send FileResponse
                        FileResponse res = new FileResponse(
                                req.RequestingNode,
                                client,
                                true,
                                HashtoFD.get(req.FileData.getFileHash())
                        );
                        ObjectTransfer.sendObject(socket, res);

                        obj = ObjectTransfer.receiveObject(socket);
                        if (obj instanceof TransferRequest) {
                            TransferRequest treq = (TransferRequest) obj;
                            TransferResponse tres = new TransferResponse(
                                    treq.RequestingNode,
                                    client,
                                    treq.Port,
                                    true
                            );
                            ObjectTransfer.sendObject(socket, tres);

                            // Automatic file transfer with passkey
                            String passkeyToUse = HashtoPasskey.getOrDefault(
                                    treq.Fd.getFileHash(),
                                    "default123"
                            );

                            ConnectionHandlerSequential.sendFile(
                                    tres.RequestingNode.getPeerIP(),
                                    tres.Port,
                                    HashtoPath.get(treq.Fd.getFileHash()),
                                    req.pub,
                                    passkeyToUse
                            );
                        }
                    }
                }

                socket.close();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    // Register file with Central Registry
    public static void registerFile(FileData f, String path, String passkey) {
    Node central = getCentralRegistry();
    CentralRegistryRequest req = new CentralRegistryRequest(f, getClient());

    try {
        Socket socket = new Socket(central.getPeerIP(), central.getPeerPort());
        ObjectTransfer.sendObject(socket, req);

        CentralRegistryResponse res = (CentralRegistryResponse) ObjectTransfer.receiveObject(socket);

        if (res.sucess) {
            HashtoFD.put(f.getFileHash(), f);
            HashtoPath.put(f.getFileHash(), path);
            HashtoPasskey.put(f.getFileHash(), passkey);

            System.out.println("Successfully uploaded file: " + path);
        }
    } catch (IOException | ClassNotFoundException e) {
        e.printStackTrace();
    }
}


    public static Node getClient() { return client; }
    public static void setClient(Node c) { client = c; }
    public static Node getCentralRegistry() { return CentralRegistry; }
    public static void setCentralRegistry(Node c) { CentralRegistry = c; }
}
