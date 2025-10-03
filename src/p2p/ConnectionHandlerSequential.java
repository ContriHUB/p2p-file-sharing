package p2p;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.PrivateKey;
import java.security.PublicKey;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import utils.UserExperience;
import utils.UserExperience.TransferStage;

public class ConnectionHandlerSequential {
    static int CHUNK_SIZE = 20;

    public static void sendFile(String serverAddress, int port, String filePath, PublicKey pub) {
        try {
            UserExperience.printStage(TransferStage.CONNECTING);
            Socket socket = new Socket(serverAddress, port);
            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
            FileInputStream fileInputStream = new FileInputStream(filePath);
            
            UserExperience.printStage(TransferStage.ENCRYPTING);
            UserExperience.printStatus("Generating AES encryption key...");
            
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(256);
            SecretKey aesKey = keyGenerator.generateKey();

            Cipher rsaCipher = Cipher.getInstance("RSA");
            rsaCipher.init(Cipher.ENCRYPT_MODE, pub);
            byte[] encryptedAesKey = rsaCipher.doFinal(aesKey.getEncoded());

            dataOutputStream.writeInt(encryptedAesKey.length);
            dataOutputStream.write(encryptedAesKey);
            
            UserExperience.printStatus("Encryption key exchanged successfully");

            Cipher aesCipher = Cipher.getInstance("AES");
            aesCipher.init(Cipher.ENCRYPT_MODE, aesKey);

            File file = new File(filePath);
            long fileSize = file.length();
            dataOutputStream.writeLong(fileSize);
            
            UserExperience.printStage(TransferStage.TRANSFERRING);
            UserExperience.printStatus("Sending file: " + file.getName() + " (" + UserExperience.formatBytes(fileSize) + ")");
            
            // Encrypt and send file in chunks
            byte[] buffer = new byte[4096];
            int bytesRead;
            int totalBytes = 0;
            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                byte[] encryptedChunk = aesCipher.update(buffer, 0, bytesRead);
                if (encryptedChunk != null) {
                    dataOutputStream.write(encryptedChunk);
                }
                totalBytes += bytesRead;
                UserExperience.printProgressBar(totalBytes, fileSize);
            }

            // Finalize encryption
            UserExperience.printStage(TransferStage.FINALIZING);
            byte[] finalEncryptedChunk = aesCipher.doFinal();
            if (finalEncryptedChunk != null) {
                dataOutputStream.write(finalEncryptedChunk);
            }
            UserExperience.printProgressBar(fileSize, fileSize);
            
            UserExperience.printStage(TransferStage.COMPLETE);
            UserExperience.printSuccess("File sent successfully!");
            
            fileInputStream.close();
            socket.close();

        } catch (Exception e) {
            System.err.println("Error during file transfer: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void receiveFile(int port, String fileName, PrivateKey prv) {
        try {
            UserExperience.printStage(TransferStage.CONNECTING);
            UserExperience.printStatus("Waiting for sender on port " + port + "...");
            
            ServerSocket serverSocket = new ServerSocket(port);
            Socket clientSocket = serverSocket.accept();
            
            UserExperience.printSuccess("Connected to sender");
            UserExperience.printStage(TransferStage.DECRYPTING);
            
            DataInputStream dataInputStream = new DataInputStream(clientSocket.getInputStream());
            FileOutputStream fileOutputStream = new FileOutputStream(fileName);

            UserExperience.printStatus("Receiving encryption key...");
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
            
            UserExperience.printSuccess("Decryption key received");
            UserExperience.printStage(TransferStage.TRANSFERRING);
            UserExperience.printStatus("Receiving file (" + UserExperience.formatBytes(fileSize) + ")...");
            
            // Decrypt and write file in chunks
            byte[] buffer = new byte[4096];
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
            UserExperience.printStage(TransferStage.FINALIZING);
            byte[] finalDecryptedChunk = aesCipher.doFinal();
            if (finalDecryptedChunk != null) {
                fileOutputStream.write(finalDecryptedChunk);
            }
            UserExperience.printProgressBar(fileSize, fileSize);
            
            UserExperience.printStage(TransferStage.COMPLETE);
            UserExperience.printSuccess("File received and saved: " + fileName);
            
            fileOutputStream.close();
            clientSocket.close();
            serverSocket.close();

        } catch (Exception e) {
            System.err.println("Error during file transfer: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
