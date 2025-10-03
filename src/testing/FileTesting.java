package testing;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;

import discovery.CentralRegistry;
import discovery.FileData;
import discovery.Handshake;
import discovery.Node;
import p2p.FileReciever;

public class FileTesting {

    public static String CentralIP = "127.0.0.1";
    public static String SenderIP = "127.0.0.1";
    public static String RecieverIP = "127.0.0.1";
    public static int CentralPort = 3000;
    public static int SenderPort = 4000;
    public static int RecieverPort = 5000;

    public static void test(String[] files) {
        String[] hashes = new String[files.length];

        for (int i = 0; i < files.length; i++) {
            hashes[i] = generateHash(files[i]);
        }

        Thread recieverThread = new Thread(() -> recieverThread(hashes));
        Thread senderThread = new Thread(() -> senderThread(files, recieverThread));
        Thread centralThread = new Thread(FileTesting::centralThread);

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
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException | IOException e) {
            throw new RuntimeException("Failed to generate hash for file: " + filePath, e);
        }
    }

    public static void centralThread() {
        Node central = new Node();
        central.setPeerIP(CentralIP);
        central.setPeerPort(CentralPort);
        CentralRegistry.start(central);
    }

    public static void senderThread(String[] files, Thread recieverThread) {
        Node client = new Node();
        client.setPeerIP(SenderIP);
        client.setPeerPort(SenderPort);

        Node central = new Node();
        central.setPeerIP(CentralIP);
        central.setPeerPort(CentralPort);

        Handshake.setClient(client);
        Handshake.setCentralRegistry(central);

        Thread t = new Thread(() -> Handshake.start(client.getPeerPort(), client));
        t.start(); // start handshake server

        // Ask user for passkey
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter passkey for these file(s): ");
        String passkey = sc.nextLine();

        for (String filePath : files) {
            try {
                File fCheck = new File(filePath.trim());
                if (!fCheck.exists()) {
                    System.out.println("File not found: " + filePath);
                    continue;
                }

                FileData f = new FileData(filePath.trim());
                Handshake.registerFile(f, filePath.trim(), passkey);
                System.out.println("File registered: " + filePath);
            } catch (IOException e) {
                System.out.println("Failed to read file: " + filePath);
                e.printStackTrace();
            }
        }

        recieverThread.start();
    }

    public static void recieverThread(String[] hashes) {
        Node central = new Node();
        central.setPeerIP(CentralIP);
        central.setPeerPort(CentralPort);

        Scanner sc = new Scanner(System.in);

        for (String hash : hashes) {
            System.out.print("Enter passkey to download file with hash " + hash + ": ");
            String passkey = sc.nextLine();
            FileReciever.downloadFile(hash, central, passkey);
        }
    }
}
