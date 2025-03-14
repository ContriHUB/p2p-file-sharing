package Main;

import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

import discovery.CentralRegistry;
import discovery.FileData;
import discovery.Handshake;
import discovery.Node;
import discovery.messages.FileRequest;
import discovery.messages.FileResponse;
import discovery.messages.TransferRequest;
import discovery.messages.TransferResponse;
import p2p.ConnectionHandlerSequential;
import p2p.FileTransfer;
import p2p.ObjectTransfer;

public class Main {
	public static void main(String[] args) throws IOException {
        // Initialize nodes
        
        // args are as follows 
        // args[0] = central / peer -> decides the role
        // args[1] = IP of self ( for listening for connections)
		// args[2] = port of self
        // args[3] = only if it is peer // (central IP)
        // args[4] = only if  it is a peer (central port)
        
		
		if(args[0].equals("central")) {
        	Node central = new Node();
        	central.setPeerIP(args[1]);
        	central.setPeerPort(Integer.parseInt(args[2]));
        	CentralRegistry.start(central);
        }
        
        
        
        if(args[0].equals("peer")) {
        	// one does this
        	Node client = new Node();
        	client.setPeerIP(args[1]);
        	client.setPeerPort(Integer.parseInt(args[2]));
        	
        	Node central = new Node();
        	central.setPeerIP(args[3]);
        	central.setPeerPort(Integer.parseInt(args[4]));
            try {
            	Handshake.setClient(client);
            	Handshake.setCentralRegistry(central);
            	Thread t = new Thread(() -> Handshake.start(client.getPeerPort() , client));
                t.start(); //this thread line will be started for each of the peer

                
                Scanner scanner = new Scanner(System.in);
                System.out.print("Enter command (upload <FilePath> or download <FileHash>): ");
                
                while (true) {
                    
                    String userInput = scanner.nextLine().trim();

                    // Exit condition
                    if (userInput.equalsIgnoreCase("exit")) {
                        System.out.println("Exiting program...");
                        break;
                    }

                    // Split the input into command and argument
                    String[] parts = userInput.split(" ");
                    if (parts.length < 2) {
                        System.out.println("Invalid command. Usage: upload <FilePath> or download <FileHash>");
                        continue;
                    }

                    String command = parts[0];
                    String argument = parts[1];

                    // Process the command
                    switch (command.toLowerCase()) {
                        case "upload":
                            // Start a new thread for upload
                        	FileData f = new FileData(argument);
                            Handshake.registerFile(f , argument);
                            break;
                        case "download":
                            // Start a new thread for download
                            FileTransfer.downloadFile(argument, central);
                            break;
                        default:
                            System.out.println("Invalid command. Usage: upload <FilePath> or download <FileHash>");
                            break;
                    }
                }

                scanner.close();
               

                
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        

        
        
      
    }
}