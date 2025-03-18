package discovery;
import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Map;

import discovery.messages.CentralRegistryRequest;
import discovery.messages.CentralRegistryResponse;
import discovery.messages.FileRequest;
import discovery.messages.FileResponse;
import discovery.messages.TransferRequest;
import discovery.messages.TransferResponse;
import p2p.ConnectionHandlerSequential;
import p2p.ObjectTransfer;
public class Handshake {

	// this class has everything required for the uploader of the file
	
    private static Node client; // client peer registered 
    private static Node CentralRegistry;
    
    
    private static Map<String , FileData> HashtoFD = new HashMap<>();
    private static Map<String , String> HashtoPath = new HashMap<>();
    
    public static void start(int port , Node c) {
    	
//    	registerToCentralRegistry(port);
//    	System.out.println("handshake on");
//    	System.out.print(port);
    	
    	client = c;
    	while(true) {
    		try (ServerSocket serverSocket = new ServerSocket(client.getPeerPort())) {
                

                Socket socket = serverSocket.accept();
                Object obj =  ObjectTransfer.receiveObject(socket);
//                System.out.println("FileRequst Recieved");
                FileRequest req = (FileRequest)obj;
                FileResponse res;
              
                if(HashtoFD.containsKey(req.FileData.getFileHash())) {
                	res = new FileResponse(req.RequestingNode,client,true,Handshake.HashtoFD.get(req.FileData.getFileHash()));
//                	System.out.println("FileResponse Sentding");
                	ObjectTransfer.sendObject(socket, res);
//                	System.out.println("FileResponse Sent");
                	obj = ObjectTransfer.receiveObject(socket);
                	
                	
//                	System.out.println("TransferRequest Recieved");
                	TransferRequest treq = (TransferRequest)obj;
//                	System.out.println("TransferResponse Senting");
                	TransferResponse tres = new TransferResponse(treq.RequestingNode , client , treq.Port , true);
//                	System.out.println("TransferResponse Sent");
                	ObjectTransfer.sendObject(socket, tres);
              	
                	
                	socket.close();
                
//                	System.out.println("the public key of requesting node is " + req.pub );
                	ConnectionHandlerSequential.sendFile(tres.RequestingNode.getPeerIP() , tres.Port , Handshake.HashtoPath.get(treq.Fd.getFileHash()) , req.pub);
                	
                	
                }
               
                
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
                
            }
    	}
    	
    }
  
    
    
    
    public static void registerFile(FileData f , String path) {
    	// send fileData to server
    	Node central = Handshake.getCentralRegistry();
    	Node c = Handshake.getClient();
    	CentralRegistryRequest req = new CentralRegistryRequest(f , c);
    	
    	
    	try {
			Socket socket = new Socket(central.getPeerIP() , central.getPeerPort());
			ObjectTransfer.sendObject(socket, req);
			
			CentralRegistryResponse res = (CentralRegistryResponse)ObjectTransfer.receiveObject(socket);
			
			if(res.sucess) {
				HashtoFD.put(f.getFileHash() , f);
				HashtoPath.put(f.getFileHash(), path);
//				System.out.println("SuccessFully Uploaded the File");
			}
		} catch (IOException | ClassNotFoundException e) {
			
			// TODO Auto-generated catch block
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