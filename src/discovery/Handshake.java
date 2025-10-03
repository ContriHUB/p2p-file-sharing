package discovery;
import discovery.messages.CentralRegistryRequest;
import discovery.messages.CentralRegistryResponse;
import discovery.messages.FileRequest;
import discovery.messages.FileResponse;
import discovery.messages.TransferRequest;
import discovery.messages.TransferResponse;
import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Map;
import p2p.ConnectionHandlerSequential;
import p2p.ObjectTransfer;
import utils.UserExperience;
import utils.UserExperience.TransferStage;

public class Handshake {

    private static Node client;
    private static Node CentralRegistry;
    
    private static Map<String, FileData> HashtoFD = new HashMap<>();
    private static Map<String, String> HashtoPath = new HashMap<>();
    
    public static void start(int port, Node c) {
        System.out.println("Peer listening on port: " + port);
        
        client = c;
        while (true) {
            try (ServerSocket serverSocket = new ServerSocket(client.getPeerPort())) {
                
                Socket socket = serverSocket.accept();
                Object obj = ObjectTransfer.receiveObject(socket);
                
                UserExperience.printStatus("Incoming file request received");
                
                FileRequest req = (FileRequest)obj;
                FileResponse res;
              
                if (HashtoFD.containsKey(req.FileData.getFileHash())) {
                    res = new FileResponse(req.RequestingNode, client, true, Handshake.HashtoFD.get(req.FileData.getFileHash()));
                    
                    UserExperience.printStatus("File found, sending response...");
                    ObjectTransfer.sendObject(socket, res);
                    
                    obj = ObjectTransfer.receiveObject(socket);
                    TransferRequest treq = (TransferRequest)obj;
                    
                    TransferResponse tres = new TransferResponse(treq.RequestingNode, client, treq.Port, true);
                    ObjectTransfer.sendObject(socket, tres);
                    
                    socket.close();
                    
                    UserExperience.printSeparator();
                    UserExperience.printStatus("Starting file transfer to " + req.RequestingNode.getPeerIP());
                    
                    ConnectionHandlerSequential.sendFile(
                        tres.RequestingNode.getPeerIP(), 
                        tres.Port, 
                        Handshake.HashtoPath.get(treq.Fd.getFileHash()), 
                        req.pub
                    );
                }
               
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
    
    public static void registerFile(FileData f, String path) {
        Node central = Handshake.getCentralRegistry();
        Node c = Handshake.getClient();
        CentralRegistryRequest req = new CentralRegistryRequest(f, c);
        
        UserExperience.printSeparator();
        UserExperience.printStatus("Registering file with central registry...");
        UserExperience.printStatus("File: " + f.getFileName());
        UserExperience.printStatus("Hash: " + f.getFileHash());
        UserExperience.printStatus("Size: " + UserExperience.formatBytes(f.getFileSize()));
        
        try {
            Socket socket = new Socket(central.getPeerIP(), central.getPeerPort());
            ObjectTransfer.sendObject(socket, req);
            
            CentralRegistryResponse res = (CentralRegistryResponse)ObjectTransfer.receiveObject(socket);
            
            if (res.sucess) {
                HashtoFD.put(f.getFileHash(), f);
                HashtoPath.put(f.getFileHash(), path);
                UserExperience.printSuccess("File registered successfully!");
                UserExperience.printStatus("Your file is now available for download");
            } else {
                UserExperience.printWarning("Failed to register file");
            }
            UserExperience.printSeparator();
            
            socket.close();
        } catch (IOException | ClassNotFoundException e) {
            UserExperience.printWarning("Error contacting central registry: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public static Node getClient() {
        return client;
    }
    
    public static void setClient(Node c) {
        Handshake.client = c;
    }
    
    public static void setCentralRegistry(Node c) {
        Handshake.CentralRegistry = c; 
    }
    
    public static Node getCentralRegistry() {
        return Handshake.CentralRegistry;
    }
}