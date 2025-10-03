package p2p;

import java.io.IOException;
import java.net.Socket;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;

import discovery.FileData;
import discovery.Handshake;
import discovery.Node;
import discovery.messages.*;
import java.io.File;
import utils.Config;
import utils.UserExperience;
import utils.UserExperience.TransferStage;

public class FileReciever {
    
    public static void downloadFile(String fileHash, Node CentralRegistry) {
        
        UserExperience.printSeparator();
        UserExperience.printStage(TransferStage.CONNECTING);
        UserExperience.printStatus("Contacting central registry for file: " + fileHash);
        
        CentralRegistryRequest req = new CentralRegistryRequest(fileHash);
        
        try {
            Socket socket = new Socket(CentralRegistry.getPeerIP(), CentralRegistry.getPeerPort());
            ObjectTransfer.sendObject(socket, req);
            
            UserExperience.printStage(TransferStage.REQUESTING);
            UserExperience.printStatus("Requesting peer list from registry...");
            
            Object obj = ObjectTransfer.receiveObject(socket);
            CentralRegistryResponse res = (CentralRegistryResponse)obj;
            FileData file = new FileData();
            file.setFileHash(fileHash);
            
            if (res.sucess) {
                UserExperience.printSuccess("Found " + res.peers.length + " peer(s) with the file");
                
                boolean downloaded = false;
                for (int i = 0; i < res.peers.length; i++) {
                    Node potentialPeer = res.peers[i];
                    
                    UserExperience.printSeparator();
                    UserExperience.printStatus("Attempting download from peer " + (i+1) + "/" + res.peers.length);
                    UserExperience.printStatus("Peer: " + potentialPeer.getPeerIP() + ":" + potentialPeer.getPeerPort());
                    
                    if (FileReciever.downloadFromPeer(potentialPeer, file)) {
                        UserExperience.printSuccess("Download completed successfully!");
                        downloaded = true;
                        break;
                    } else {
                        UserExperience.printWarning("Failed to download from peer " + (i+1));
                    }
                }
                
                if (!downloaded) {
                    UserExperience.printWarning("Could not download from any peer");
                }
                UserExperience.printSeparator();
            } else {
                UserExperience.printWarning("No peers found with this file");
                UserExperience.printSeparator();
            }
            
            socket.close();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error contacting registry: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public static boolean downloadFromPeer(Node peer, FileData f) {
        Node two = Handshake.getClient();
        FileRequest req = new FileRequest(f, two);
        
        try {
            UserExperience.printStage(TransferStage.NEGOTIATING);
            UserExperience.printStatus("Establishing secure connection...");
            
            Socket socket = new Socket(peer.getPeerIP(), peer.getPeerPort());
            
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            KeyPair keys = keyPairGenerator.generateKeyPair();
            req.pub = keys.getPublic();
            
            UserExperience.printStatus("Sending file request...");
            ObjectTransfer.sendObject(socket, req);
            
            Object obj = ObjectTransfer.receiveObject(socket);
            FileResponse res = (FileResponse)obj;
            String fileName = res.file.getFileName();
            
            UserExperience.printSuccess("Peer accepted request for: " + fileName);
            
            TransferRequest treq = new TransferRequest(two, f, 7777);
            ObjectTransfer.sendObject(socket, treq);
            
            String filePath = new File(Config.getDownloadsDir(), fileName).getPath();
            
            ConnectionHandlerSequential.receiveFile(treq.Port, filePath, keys.getPrivate());
            
            Handshake.registerFile(f, filePath);
            return true;

        } catch (IOException | ClassNotFoundException | NoSuchAlgorithmException e) {
            UserExperience.printWarning("Connection failed: " + e.getMessage());
            return false;
        }
    }
}