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
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import utils.UserExperience;

public class ConnectionHandlerSequential {
		static int CHUNK_SIZE = 20;
	
	
		 
		 
		public static void sendFile(String serverAddress, int port, String filePath, PublicKey pub) {
	        try (Socket socket = new Socket(serverAddress, port);
	             DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
	             FileInputStream fileInputStream = new FileInputStream(filePath)) {

	            
	            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
	            keyGenerator.init(256); // 256-bit key
	            SecretKey aesKey = keyGenerator.generateKey();

	            
	            Cipher rsaCipher = Cipher.getInstance("RSA");
	            rsaCipher.init(Cipher.ENCRYPT_MODE, pub);
	            byte[] encryptedAesKey = rsaCipher.doFinal(aesKey.getEncoded());

	            dataOutputStream.writeInt(encryptedAesKey.length); // Send key length
	            dataOutputStream.write(encryptedAesKey); // Send encrypted key

	            Cipher aesCipher = Cipher.getInstance("AES");
	            aesCipher.init(Cipher.ENCRYPT_MODE, aesKey);

	            File file = new File(filePath);
	            long fileSize = file.length();
	            dataOutputStream.writeLong(fileSize);
	            System.out.println("Recieving file ");
	            // Encrypt and send file in chunks
	            byte[] buffer = new byte[4096]; // File read buffer
	            int bytesRead;
	            int totalBytes = 0;
	            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
	                byte[] encryptedChunk = aesCipher.update(buffer, 0, bytesRead);
	                if (encryptedChunk != null) {
	                    dataOutputStream.write(encryptedChunk);
	                }
	                totalBytes += bytesRead;
	                UserExperience.printProgressBar(totalBytes , fileSize);
	            }

	            // Finalize encryption
	            byte[] finalEncryptedChunk = aesCipher.doFinal();
	            if (finalEncryptedChunk != null) {
	                dataOutputStream.write(finalEncryptedChunk);
	            }
	            UserExperience.printProgressBar(fileSize , fileSize);
	            System.out.println("\n File sent successfully.");

	        } catch (Exception e) {
	            System.err.println("Error during file transfer: " + e.getMessage());
	            e.printStackTrace();
	        }
	    }
		
		
	

		 public static void receiveFile(int port, String fileName, PrivateKey prv) {
		        try (ServerSocket serverSocket = new ServerSocket(port)) {
		            System.out.println("Server started on port " + port);

		            // Accept the client connection
		            try (Socket clientSocket = serverSocket.accept();
		                 DataInputStream dataInputStream = new DataInputStream(clientSocket.getInputStream());
		                 FileOutputStream fileOutputStream = new FileOutputStream(fileName)) {

		                int encryptedKeyLength = dataInputStream.readInt();
		                byte[] encryptedAesKey = new byte[encryptedKeyLength];
		                dataInputStream.readFully(encryptedAesKey);

		         
		                Cipher rsaCipher = Cipher.getInstance("RSA");
		                rsaCipher.init(Cipher.DECRYPT_MODE, prv);
		                byte[] aesKeyBytes = rsaCipher.doFinal(encryptedAesKey);
		                SecretKey aesKey = new SecretKeySpec(aesKeyBytes, "AES");

		              
		                Cipher aesCipher = Cipher.getInstance("AES");
		                aesCipher.init(Cipher.DECRYPT_MODE, aesKey);

		            
		                long fileSize = dataInputStream.readLong();
		                
		                
		                System.out.println("Initiating File Transfer");
		                // Decrypt and write file in chunks
		                byte[] buffer = new byte[4096]; // File write buffer
		                int bytesRead;
		                long totalBytesRead = 0;
		                while (totalBytesRead < fileSize) {
		                    bytesRead = dataInputStream.read(buffer);
		                    if (bytesRead == -1) break;
		                    byte[] decryptedChunk = aesCipher.update(buffer, 0, bytesRead);
		                    if (decryptedChunk != null) {
		                        fileOutputStream.write(decryptedChunk);
		                    }
		                    totalBytesRead += bytesRead;
		                    UserExperience.printProgressBar(totalBytesRead, fileSize);
		                }

		                // Finalize decryption
		                byte[] finalDecryptedChunk = aesCipher.doFinal();
		                if (finalDecryptedChunk != null) {
		                    fileOutputStream.write(finalDecryptedChunk);
		                }
		                UserExperience.printProgressBar(fileSize , fileSize);
		                System.out.println("\n File received and decrypted successfully.");

		            } catch (Exception e) {
		                System.err.println("Error during file transfer: " + e.getMessage());
		                e.printStackTrace();
		            }
		        } catch (IOException e) {
		            System.err.println("Could not start server on port " + port + ": " + e.getMessage());
		            e.printStackTrace();
		        }
		    }
	}


