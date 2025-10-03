package p2p;

import java.io.IOException;
import java.net.Socket;

import discovery.FileData;
import discovery.Handshake;
import discovery.Node;
import discovery.messages.CentralRegistryRequest;
import discovery.messages.CentralRegistryResponse;
import discovery.messages.FileRequest;
import discovery.messages.FileResponse;
import discovery.messages.TransferRequest;

public class FileReciever {

    // Download a file by hash from Central Registry
    public static void downloadFile(String fileHash, Node centralRegistry, String passkey) {
        CentralRegistryRequest req = new CentralRegistryRequest(fileHash);

        try {
            Socket socket = new Socket(centralRegistry.getPeerIP(), centralRegistry.getPeerPort());
            ObjectTransfer.sendObject(socket, req);
            Object obj = ObjectTransfer.receiveObject(socket);
            CentralRegistryResponse res = (CentralRegistryResponse) obj;

            FileData file = new FileData();
            file.setFileHash(fileHash);

            if (res.sucess) {
                for (Node peerNode : res.peers) {
                    if (downloadFromPeer(peerNode, file, passkey)) {
                        System.out.println("\nDownloaded from peer: " + peerNode);
                        break;
                    } else {
                        System.out.println("Failed downloading from peer: " + peerNode);
                    }
                }
            } else {
                System.out.println("FAILED TO GET PEERS");
            }

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static boolean downloadFromPeer(Node peer, FileData file, String passkey) {
        Node client = Handshake.getClient();
        FileRequest req = new FileRequest(file, client);

        try (Socket socket = new Socket(peer.getPeerIP(), peer.getPeerPort())) {
            // Send request to peer
            ObjectTransfer.sendObject(socket, req);
            Object obj = ObjectTransfer.receiveObject(socket);
            FileResponse res = (FileResponse) obj;

            String fileName = res.file.getFileName();
            TransferRequest treq = new TransferRequest(client, file, 7777);
            ObjectTransfer.sendObject(socket, treq);

            String filePath = "./downloads/" + fileName;

            // Receive file using passkey
            ConnectionHandlerSequential.receiveFile(treq.Port, filePath, passkey);

            // Register file locally
            Handshake.registerFile(file, filePath, passkey);

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
