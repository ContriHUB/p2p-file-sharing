package p2p;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import utils.CryptoUtils;
import utils.UserExperience;

public class ConnectionHandlerSequential {

    // Sends a file to a peer with AES-GCM encryption protected by passkey
    public static void sendFile(String serverAddress, int port, String filePath, Object pub, String passkey) {
        try (Socket socket = new Socket(serverAddress, port);
             DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
             FileInputStream fileInputStream = new FileInputStream(filePath)) {

            // Generate AES key for file encryption
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(256);
            SecretKey fileKey = keyGen.generateKey();

            // Encrypt AES key using passkey
            byte[] salt = CryptoUtils.randomBytes(16);
            SecretKey passKeyDerived = CryptoUtils.deriveKey(passkey.toCharArray(), salt);
            byte[] iv = CryptoUtils.randomBytes(12); // AES-GCM IV
            byte[] encFileKey = CryptoUtils.aesGcmEncrypt(fileKey.getEncoded(), passKeyDerived, iv, null);

            // Send salt, IV, and encrypted AES key
            dataOutputStream.writeInt(salt.length);
            dataOutputStream.write(salt);
            dataOutputStream.writeInt(iv.length);
            dataOutputStream.write(iv);
            dataOutputStream.writeInt(encFileKey.length);
            dataOutputStream.write(encFileKey);

            // Encrypt and send file
            Cipher aesCipher = Cipher.getInstance("AES");
            aesCipher.init(Cipher.ENCRYPT_MODE, fileKey);

            File file = new File(filePath);
            long fileSize = file.length();
            dataOutputStream.writeLong(fileSize);

            byte[] buffer = new byte[4096];
            int bytesRead;
            long totalBytes = 0;

            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                byte[] encryptedChunk = aesCipher.update(buffer, 0, bytesRead);
                if (encryptedChunk != null) dataOutputStream.write(encryptedChunk);
                totalBytes += bytesRead;
                UserExperience.printProgressBar(totalBytes, fileSize);
            }

            byte[] finalChunk = aesCipher.doFinal();
            if (finalChunk != null) dataOutputStream.write(finalChunk);

            UserExperience.printProgressBar(fileSize, fileSize);
            System.out.println("\nFile sent successfully with passkey protection!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Receives a file from a peer and decrypts it using AES-GCM with passkey
    public static void receiveFile(int port, String filePath, String passkey) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server started on port " + port);

            try (Socket clientSocket = serverSocket.accept();
                 DataInputStream dataInputStream = new DataInputStream(clientSocket.getInputStream())) {

                // Ensure downloads directory exists
                File outFile = new File(filePath);
                outFile.getParentFile().mkdirs();

                try (FileOutputStream fileOutputStream = new FileOutputStream(outFile)) {

                    // Read salt, IV, and encrypted AES key
                    int saltLen = dataInputStream.readInt();
                    byte[] salt = new byte[saltLen];
                    dataInputStream.readFully(salt);

                    int ivLen = dataInputStream.readInt();
                    byte[] iv = new byte[ivLen];
                    dataInputStream.readFully(iv);

                    int encKeyLen = dataInputStream.readInt();
                    byte[] encKey = new byte[encKeyLen];
                    dataInputStream.readFully(encKey);

                    // Derive AES key from passkey and decrypt file AES key
                    SecretKey derivedKey = CryptoUtils.deriveKey(passkey.toCharArray(), salt);
                    byte[] fileKeyBytes = CryptoUtils.aesGcmDecrypt(encKey, derivedKey, iv, null);
                    SecretKey fileKey = new SecretKeySpec(fileKeyBytes, "AES");

                    // Prepare AES cipher for file decryption
                    Cipher cipher = Cipher.getInstance("AES");
                    cipher.init(Cipher.DECRYPT_MODE, fileKey);

                    // Read file data
                    long fileSize = dataInputStream.readLong();
                    byte[] buffer = new byte[4096];
                    long totalRead = 0;

                    while (totalRead < fileSize) {
                        int read = dataInputStream.read(buffer);
                        if (read == -1) break;
                        byte[] decrypted = cipher.update(buffer, 0, read);
                        if (decrypted != null) fileOutputStream.write(decrypted);
                        totalRead += read;
                        UserExperience.printProgressBar(totalRead, fileSize);
                    }

                    byte[] finalBytes = cipher.doFinal();
                    if (finalBytes != null) fileOutputStream.write(finalBytes);
                    UserExperience.printProgressBar(fileSize, fileSize);

                    System.out.println("\nFile received and decrypted successfully!");
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
