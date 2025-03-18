package discovery;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
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

public class CentralRegistry {
	public static Map<String, Node[]> peerMap = new HashMap<>();
	public static Node CR; 
	public static void start(Node central){
		// make the node listen on a particular port
		
		// call the three method below as per the reqest
//		System.out.println("central registry on");
//    	System.out.print(central.getPeerPort());
    	
    	CR = central;
    	while(true) {
    		try (ServerSocket serverSocket = new ServerSocket(central.getPeerPort())) {
                

                Socket socket = serverSocket.accept();
                Object obj =  ObjectTransfer.receiveObject(socket);
               
                CentralRegistryRequest req = (CentralRegistryRequest)obj;
                
                if(req.type.equals("registerfile")) {
                	CentralRegistry.RegisterFile(socket , req);
                }
                
                if(req.type.equals("getpotentialpeers")) {
                	CentralRegistry.SendPotentialSeeds(socket , req);
                }
                
                if(req.type.equals("registerpeer")) {
                	CentralRegistry.RegisterPeer(socket , req);
                }
                
               
                
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
                
            }
    	}
    	
		
	}
	public static void RegisterPeer(Socket socket , CentralRegistryRequest req) {
		//  register a peer 
		// i.e give it a name and a id
		
		// this is not needed as of know
		// will think
	}
	public static void RegisterFile(Socket socket, CentralRegistryRequest req) {
	    // Get the file hash and the node from the request
	    String fileHash = req.file.getFileHash();
	    Node node = req.owner;

	    // Check if the file hash exists in the map
	    if (peerMap.containsKey(fileHash)) {
	        // File hash exists, get the existing array of nodes
	        Node[] nodes = peerMap.get(fileHash);

	        // Check if the node already exists in the array
	        boolean nodeExists = false;
	        for (Node existingNode : nodes) {
	            if (existingNode.equals(node)) {
	                nodeExists = true;
	                break;
	            }
	        }

	        // If the node doesn't exist, resize the array and add the node
	        if (!nodeExists) {
	            // Create a new array with increased size
	            Node[] newNodes = new Node[nodes.length + 1];

	            // Copy existing nodes to the new array
	            System.arraycopy(nodes, 0, newNodes, 0, nodes.length);

	            // Add the new node to the end of the array
	            newNodes[newNodes.length - 1] = node;

	            // Update the map with the new array
	            peerMap.put(fileHash, newNodes);
//	            System.out.println("Node added to existing file hash: " + fileHash);
	        } else {
//	            System.out.println("Node already exists for file hash: " + fileHash);
	        }
	    } else {
	        // File hash does not exist, create a new entry in the map
	        Node[] newNodes = new Node[]{node};
	        peerMap.put(fileHash, newNodes);
//	        System.out.println("New file hash registered: " + fileHash);
	    }

	    
	   
	    // Close the socket
	    try {
	    	
	    	CentralRegistryResponse res = new CentralRegistryResponse(true , req.owner , req.file);
		    ObjectTransfer.sendObject(socket, res);
	        socket.close();
//	        System.out.println("Socket closed.");
	    } catch (IOException e) {
	        System.err.println("Error closing socket: " + e.getMessage());
	    }
	}
	
	public static void SendPotentialSeeds(Socket socket , CentralRegistryRequest req) {
		// read tbe map and send the entries
		if(CentralRegistry.peerMap.containsKey(req.file.getFileHash())) {
			CentralRegistryResponse res = new CentralRegistryResponse(true , CentralRegistry.peerMap.get(req.file.getFileHash()));
			try {
				ObjectTransfer.sendObject(socket, res);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
			
	}
	
}
