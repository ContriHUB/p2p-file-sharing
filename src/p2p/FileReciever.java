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
public class FileReciever {
	// this class  has  all the methods for the file reciever
	public static void downloadFile(String fileHash , Node CentralRegistry) {
		
		
        
		CentralRegistryRequest req = new CentralRegistryRequest(fileHash);
		
		try {
			Socket socket = new Socket(CentralRegistry.getPeerIP() , CentralRegistry.getPeerPort());
			ObjectTransfer.sendObject(socket , req);
			Object obj = ObjectTransfer.receiveObject(socket);
			CentralRegistryResponse res = (CentralRegistryResponse)obj;
			FileData file = new FileData();
			file.setFileHash(fileHash);
			if(res.sucess) {
				for(Node potentialPeer : res.peers) { 
					
					//implement a peer selection logic here
					if(FileReciever.downloadFromPeer(potentialPeer , file)) {
						System.out.println("\n Downloaded from peer " + potentialPeer);
						
						break;
					}
					else {
						System.out.println("Failed Downloading from " + potentialPeer);
					}
				}
			}
			else {
				System.out.println("FAILED TO GET PEERS");
			}
		} catch (IOException | ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static boolean downloadFromPeer(Node peer , FileData f) {
			Node two = Handshake.getClient();
			FileRequest req = new FileRequest(f , two);
			
		
			
			
			
		   try (Socket socket = new Socket(peer.getPeerIP(), peer.getPeerPort())) {
		         
			   KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
				keyPairGenerator.initialize(2048); // Key size
		        KeyPair keys = keyPairGenerator.generateKeyPair();
		        req.pub =  keys.getPublic();
		        
		        
	           ObjectTransfer.sendObject(socket, req);
	           
	 		   Object obj = ObjectTransfer.receiveObject(socket);
	 		   
	 		   FileResponse res = (FileResponse)obj;
	 		   String fileName = res.file.getFileName();
	 		   TransferRequest treq = new TransferRequest(two , f , 7777);
	 		  
	 		    ObjectTransfer.sendObject(socket, treq);
	 		   
	 	
	 		   String filePath = "./downloads/" + fileName;
	 		   
	 		   ConnectionHandlerSequential.receiveFile(treq.Port , filePath , keys.getPrivate());
	 		   
	 		   Handshake.registerFile(f , filePath);
	 		   return true;

	        } catch (IOException | ClassNotFoundException | NoSuchAlgorithmException  e) {
	            e.printStackTrace();
	            return false;
	        }
	}
}
