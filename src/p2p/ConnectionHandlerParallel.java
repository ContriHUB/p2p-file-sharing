package p2p;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class ConnectionHandlerParallel {

	 public static void receiveFile(int port, String fileName) {
	        try (ServerSocket serverSocket = new ServerSocket(port)) {
	            System.out.println("Server is starting on port " + port);

	            // Accept the client connection
	            try (Socket clientSocket = serverSocket.accept();
	                 DataInputStream dataInputStream = new DataInputStream(clientSocket.getInputStream());
	                 FileOutputStream fileOutputStream = new FileOutputStream(fileName)) {

	                System.out.println("Client connected: " + clientSocket.getInetAddress());

	                // Read the file size from the client
	                long fileSize = dataInputStream.readLong();
	                System.out.println("Receiving file of size: " + fileSize + " bytes");

	                // Buffer to read chunks of data
	                byte[] buffer = new byte[4 * 1024]; // 4 KB buffer
	                int bytesRead;

	                // Read the file data in chunks and write to the output file
	                while (fileSize > 0 && (bytesRead = dataInputStream.read(buffer, 0, (int) Math.min(buffer.length, fileSize))) != -1) {
	                    // perform encryption check sum here
	                	
	                	
	                	
	                	
	                	///////
	                	
	                	fileOutputStream.write(buffer, 0, bytesRead);
	                    fileSize -= bytesRead; // Reduce remaining file size
	                }

	                System.out.println("File received and saved as: " + fileName);
	            } catch (IOException e) {
	                System.err.println("Error during file transfer: " + e.getMessage());
	                e.printStackTrace();
	            }
	        } catch (IOException e) {
	            System.err.println("Could not start server on port " + port + ": " + e.getMessage());
	            e.printStackTrace();
	        }
	    }
	 
	 public static void sendFile(String serverAddress, int port, String filePath) {
	        try (Socket socket = new Socket(serverAddress, port);
	             DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
	             FileInputStream fileInputStream = new FileInputStream(filePath)) {

	            System.out.println("Connected to server: " + serverAddress);

	            // Get the file to send
	            File file = new File(filePath);
	            long fileSize = file.length();
	            System.out.println("Sending file: " + file.getName() + " (" + fileSize + " bytes)");

	            // Send the file size to the server
	            dataOutputStream.writeLong(fileSize);

	            // Buffer to read and send chunks of data
	            byte[] buffer = new byte[4 * 1024]; // 4 KB buffer
	            int bytesRead;

	            // Read the file and send it in chunks
	            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
	            	//perform encryption check sum all here
	            	
	            	
	            	
	            	////////
	                dataOutputStream.write(buffer, 0, bytesRead);
	                dataOutputStream.flush(); // Ensure data is sent immediately
	            }

	            System.out.println("File sent successfully.");
	        } catch (IOException e) {
	            System.err.println("Error during file transfer: " + e.getMessage());
	            e.printStackTrace();
	        }
	    }

}