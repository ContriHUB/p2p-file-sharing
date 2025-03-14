package testing;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import discovery.CentralRegistry;
import discovery.FileData;
import discovery.Handshake;
import discovery.Node;
import p2p.FileTransfer;

public class FileTesting {
	public static String CentralIP = "127.0.0.1";
	public static String SenderIP = "127.0.0.1";
	public static String RecieverIP = "127.0.0.1";
	public static int CentralPort = 3000;
	public static int SenderPort = 4000;
	public static int RecieverPort = 5000;
	public static void test(String []files) {
		// this gives an array of file paths
		
		String[] hashes = new String[files.length];

        for (int i = 0; i < files.length; i++) {
            hashes[i] = generateHash(files[i]);
        }
        
        Thread recieverThread = new Thread(() ->{
        	recieverThread(hashes);
        });
        Thread senderThread = new Thread(() -> {
        	senderThread(files,recieverThread);
        	
        });
        Thread centralThread = new Thread(() -> {
        	centralThread();
        });
        centralThread.start();
        senderThread.start();

	}
	public static String generateHash(String filePath) {
		File file = new File(filePath);
		try (FileInputStream fis = new FileInputStream(file)) {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                md.update(buffer, 0, bytesRead);
            }
            byte[] hashBytes = md.digest();

            // Convert the byte array to a hexadecimal string
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException | IOException e) {
            throw new RuntimeException("MD5 algorithm not found.", e);
        }
		
	}
	public static void centralThread() {
		Node central = new Node();
    	central.setPeerIP(CentralIP);
    	central.setPeerPort(CentralPort);
    	CentralRegistry.start(central);
	}
	public static void senderThread(String []files , Thread recieverThread) {
		Node client = new Node();
    	client.setPeerIP(SenderIP);
    	client.setPeerPort(SenderPort);
    	
    	Node central = new Node();
    	central.setPeerIP(CentralIP);
    	central.setPeerPort(CentralPort);
        try {
        	Handshake.setClient(client);
        	Handshake.setCentralRegistry(central);
        	Thread t = new Thread(() -> Handshake.start(client.getPeerPort() , client));
            t.start(); //this thread line will be started for each of the peer
            for(String filePath : files) {
            	FileData f = new FileData(filePath);
                Handshake.registerFile(f , filePath);
            }
            recieverThread.start();
        }
        catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void recieverThread(String []hashes) {
		Node client = new Node();
    	client.setPeerIP(SenderIP);
    	client.setPeerPort(SenderPort);
    	
    	Node central = new Node();
    	central.setPeerIP(CentralIP);
    	central.setPeerPort(CentralPort);
    	
    	for(String hash : hashes) {
    		FileTransfer.downloadFile(hash, central);
    	}
    	
	}
}
