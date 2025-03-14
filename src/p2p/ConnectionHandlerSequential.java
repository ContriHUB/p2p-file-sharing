package p2p;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ConnectionHandlerSequential {
		static int CHUNK_SIZE = 1024;
		private static void printProgressBar(long currentProgress, long totalSize) {
		    int barLength = 50; // Length of the progress bar
		    int progress = (int) ((double) currentProgress / totalSize * barLength);
		
		    // Build the progress bar string
		    StringBuilder bar = new StringBuilder("[");
		    for (int i = 0; i < barLength; i++) {
		        if (i < progress) {
		            bar.append("="); // Filled part
		        } else {
		            bar.append(" "); // Empty part
		        }
		    }
		    bar.append("] ");
		    bar.append((currentProgress * 100)/ totalSize).append("%");
		
		    // Print the progress bar
		    System.out.print("\r" + bar.toString()); // \r to overwrite the same line
		}
		public static void receiveFile(int port, String fileName) {
	        try (ServerSocket serverSocket = new ServerSocket(port)) {
	           

	            // Accept the client connection
	            try (Socket clientSocket = serverSocket.accept();
	                 DataInputStream dataInputStream = new DataInputStream(clientSocket.getInputStream());
	                 FileOutputStream fileOutputStream = new FileOutputStream(fileName)) {

	              
	                long fileSize = dataInputStream.readLong();
	              
	                byte[] buffer = new byte[CHUNK_SIZE]; // 4 KB buffer
	                int bytesRead;
	                long totalSize = fileSize;
	                // Read the file data in chunks and write to the output file
	                while (fileSize > 0 && (bytesRead = dataInputStream.read(buffer, 0, (int) Math.min(buffer.length, fileSize))) != -1) {
	                    // perform encryption check sum here
	                	
	                	
	                	
	                	
	                	///////
	                	
	                	fileOutputStream.write(buffer, 0, bytesRead);
	                    fileSize -= bytesRead; // Reduce remaining file size
	                    ConnectionHandlerSequential.printProgressBar(totalSize - fileSize, totalSize);
	                }

	                
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

	           
	            File file = new File(filePath);
	            long fileSize = file.length();
	            
	            // Send the file size to the server
	            dataOutputStream.writeLong(fileSize);

	            // Buffer to read and send chunks of data
	            byte[] buffer = new byte[CHUNK_SIZE]; // 4 KB buffer
	            int bytesRead;

	            // Read the file and send it in chunks
	            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
	            	//perform encryption check sum all here
	            	
	            	
	            	
	            	////////
	                dataOutputStream.write(buffer, 0, bytesRead);
	                dataOutputStream.flush(); // Ensure data is sent immediately
	            }

	            
	        } catch (IOException e) {
	            System.err.println("Error during file transfer: " + e.getMessage());
	            e.printStackTrace();
	        }
	    }

	}


