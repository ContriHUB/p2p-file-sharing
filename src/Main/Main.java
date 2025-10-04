package Main;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

import discovery.FileData;
import discovery.Handshake;
import discovery.Node;
import discovery.CentralRegistry;
import p2p.FileReciever;

public class Main {
    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            System.out.println("Usage: java Main <peer|central> [args...]");
            return;
        }

        if (args[0].equals("central")) {
            Node central = new Node();
            central.setPeerIP(args[1]);
            central.setPeerPort(Integer.parseInt(args[2]));
            System.out.println("Central Registry started...");
            CentralRegistry.start(central);  // blocks and handles requests
        }

        else if (args[0].equals("peer")) {
            Node client = new Node();
            client.setPeerIP(args[1]);
            client.setPeerPort(Integer.parseInt(args[2]));

            Node central = new Node();
            central.setPeerIP(args[3]);
            central.setPeerPort(Integer.parseInt(args[4]));

            Handshake.setClient(client);
            Handshake.setCentralRegistry(central);

            Thread handshakeThread = new Thread(() -> {
                Handshake.start(client.getPeerPort(), client);
            });
            handshakeThread.start();

            System.out.println("Peer started. Commands: upload <FilePath(s)>, download <FileHash>, exit");

            Scanner sc = new Scanner(System.in);

            while (true) {
                System.out.print("> ");
                String command = sc.nextLine();

                if (command.startsWith("upload ")) {
                    String pathsStr = command.substring(7).trim();
                    String[] files = pathsStr.split(";");
                    System.out.print("Enter passkey for these file(s): ");
                    String passkey = sc.nextLine();

                    for (String filePath : files) {
                        File fCheck = new File(filePath.trim());
                        if (!fCheck.exists()) {
                            System.out.println("File not found: " + filePath);
                            continue;
                        }
                        try {
                            FileData f = new FileData(filePath.trim());
                            Handshake.registerFile(f, filePath.trim(), passkey);
                            System.out.println("File registered: " + filePath);
                            System.out.println("File hash: " + f.getFileHash());  // <-- HASH PRINTED HERE
                        } catch (IOException e) {
                            System.out.println("Error reading file: " + filePath);
                        }
                    }
                }

                else if (command.startsWith("download ")) {
                    String fileHash = command.substring(9).trim();
                    System.out.print("Enter passkey to download the file: ");
                    String passkey = sc.nextLine();
                    FileReciever.downloadFile(fileHash, central, passkey);
                }

                else if (command.equals("exit")) {
                    System.out.println("Exiting...");
                    break;
                }

                else {
                    System.out.println("Invalid command.");
                }
            }
        }
    }
}
