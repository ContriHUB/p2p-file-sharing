package p2p;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import discovery.FileData;
import discovery.Node;
import discovery.messages.BroadcastBeacon;
import utils.UserExperience;
import utils.Config;

public class BroadCastTransfer {
	
	public static int broadcastListeningPort = 12345;
	private static int BUFFER_SIZE = 1024;
	public static int broadCastCountDefault = 1; //anybody that misses the beacon would miss the file transfer
	public static int congestionControlSleepTime = 30;
	public static int congestionControlSleepPacketCount = 400;
	public static void BroadcastFile(FileData f, Node client , String filePath) {
		try {
			
			
			
			BroadcastBeacon beacon = new BroadcastBeacon(f , client);
			for(int i = 0 ; i < BroadCastTransfer.broadCastCountDefault ; i++) {
				ObjectTransfer.sendObjectBroadcast(beacon);
				
				Thread.sleep(20);
			}
			
			
			DatagramSocket socket = new DatagramSocket();
            socket.setBroadcast(true); // Enable broadcasting
            
            // Define the broadcast address
            InetAddress broadcastAddress = InetAddress.getByName("255.255.255.255");

            // Open the file to read
            File file = new File(filePath);
            FileInputStream fileInputStream = new FileInputStream(file);
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;

            // Send the file in chunks
            int sequenceNumber = 0;
            long totalSize = f.getFileSize();
            long sentSize = 0;
            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                // Create a packet with the chunk and sequence number
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);
                dataOutputStream.writeInt(sequenceNumber); // Add sequence number
                dataOutputStream.write(buffer, 0, bytesRead); // Add the chunk
                byte[] packetData = byteArrayOutputStream.toByteArray();

                // Send the packet
                DatagramPacket packet = new DatagramPacket(packetData, packetData.length, broadcastAddress, BroadCastTransfer.broadcastListeningPort);
                socket.send(packet);
                sentSize += bytesRead;
                sequenceNumber++;
                
                UserExperience.printProgressBar(sentSize, totalSize);
                
                if(sequenceNumber%BroadCastTransfer.congestionControlSleepPacketCount == 0) {
                	Thread.sleep(BroadCastTransfer.congestionControlSleepTime);
                }
            }

            // Send an end-of-file packet
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);
            dataOutputStream.writeInt(-1); // Special sequence number to indicate EOF
            byte[] eofPacketData = byteArrayOutputStream.toByteArray();
            DatagramPacket eofPacket = new DatagramPacket(eofPacketData, eofPacketData.length, broadcastAddress, BroadCastTransfer.broadcastListeningPort);
            socket.send(eofPacket);

            System.out.println("File broadcast complete.");

            // Clean up
            fileInputStream.close();
            socket.close();
		} catch (IOException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public static void RecieveFile(String fileHash) {
		BroadcastBeacon beacon = (BroadcastBeacon)ObjectTransfer.recieveObjectBroadcast();
		
		long totalSize = beacon.file.getFileSize();
		
		if(beacon.file.getFileHash().equals(fileHash)) {
			try {
	            // Create a UDP socket
	            DatagramSocket socket = new DatagramSocket(BroadCastTransfer.broadcastListeningPort);

	            // Open the file to write
	            String outputFilePath = new java.io.File(Config.getDownloadsDir(), beacon.file.getFileName()).getPath();
	            FileOutputStream fileOutputStream = new FileOutputStream(outputFilePath);
	            byte[] buffer = new byte[BUFFER_SIZE];
	            long recvSize = 0;
	            // Receive packets and write to file
	            while (true) {
	                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
	                socket.receive(packet);

	                // Extract sequence number and data
	                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(packet.getData());
	                DataInputStream dataInputStream = new DataInputStream(byteArrayInputStream);
	                int sequenceNumber = dataInputStream.readInt();

	                if (sequenceNumber == -1) {
	                    // End-of-file packet received
	                    System.out.println("End of file received.");
	                    break;
	                }

	                // Write the chunk to the file
	                int bytesRead = packet.getLength() - 4; // Subtract 4 bytes for the sequence number
	                byte[] chunk = new byte[bytesRead];
	                dataInputStream.readFully(chunk);
	                fileOutputStream.write(chunk);
	                recvSize += bytesRead;
	                
	                UserExperience.printProgressBar(recvSize, totalSize);
	                
	            }

	            // Clean up
	            fileOutputStream.close();
	            socket.close();
	        } catch (Exception e) {
	            e.printStackTrace();
	        }

		}
	}
}
