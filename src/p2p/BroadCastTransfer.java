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
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Random;
import java.nio.charset.StandardCharsets;


import discovery.FileData;
import discovery.Node;
import discovery.messages.BroadcastBeacon;
import utils.UserExperience;
import utils.Config;

public class BroadCastTransfer {
	
	public static int broadcastListeningPort = Config.getBroadcastPort();
	private static int BUFFER_SIZE = 1024; 
	public static int broadCastCountDefault = 3; //anybody that misses the beacon would miss the file transfer
	public static int congestionControlSleepTime = 30;
	public static int congestionControlSleepPacketCount = 400;

	static final byte FLAG_DATA = 0x01;
	static final byte FLAG_EOB  = 0x02; // End Of Broadcast (marker after first pass)
	
	// Make a packet with header and data
	static ByteBuffer makePkt(int sessionId, int seq, int total, byte flag, byte[] data) 
	{
    	int body = (data == null ? 0 : data.length);
    	ByteBuffer bb = ByteBuffer.allocate(1 + 4 + 4 + 4 + body);
    	bb.put(flag);            // 1
    	bb.putInt(sessionId);    // 4
    	bb.putInt(seq);          // 4
    	bb.putInt(total);        // 4
    	if (body > 0) bb.put(data);
    	bb.flip();
    	return bb;
	}

	// Parse a received packet
	static class Parsed 
	{
    	byte flag; int sid; int seq; int total; byte[] data;
	}
	static Parsed parsePkt(byte[] buf, int len) 
	{
    	ByteBuffer bb = ByteBuffer.wrap(buf, 0, len);
    	Parsed p = new Parsed();
    	p.flag  = bb.get();
    	p.sid   = bb.getInt();
    	p.seq   = bb.getInt();
    	p.total = bb.getInt();
    	int remain = bb.remaining();
    	p.data = new byte[remain];
    	bb.get(p.data);
    	return p;
	}


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
            InetAddress broadcastAddress = InetAddress.getByName(Config.getBroadcastGroup());

			//NACK Listener 
			int nackPort = Config.getBroadcastNackPort();
            int waitNacksMs = Config.getBroadcastWaitNacksMs();
            int maxRounds = Config.getBroadcastMaxRounds();

			DatagramSocket nackSocket = new DatagramSocket(nackPort);
            nackSocket.setSoTimeout(waitNacksMs);

            // Open the file to read
            File file = new File(filePath);
            FileInputStream fileInputStream = new FileInputStream(file);

            byte[] buf = new byte[BUFFER_SIZE];
            List<byte[]> slices = new ArrayList<>();
            int bytesRead;
            while ((bytesRead = fileInputStream.read(buf)) != -1) {
                byte[] chunk = new byte[bytesRead];
                System.arraycopy(buf, 0, chunk, 0, bytesRead);
                slices.add(chunk);
            }
            fileInputStream.close();

			int totalPackets = slices.size(); // Total number of packets to send
            int sessionId = (int)(System.nanoTime() & 0x7fffffff); // Random session ID

            // Send the file in chunks
            //int sequenceNumber = 0;
            long totalSize = f.getFileSize();
            long sentSize = 0;

			for (int sequenceNumber =0; sequenceNumber < totalPackets; sequenceNumber++) 
			{
                byte[] chunk = slices.get(sequenceNumber);
                // Create a packet with the chunk and sequence number
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);
                dataOutputStream.writeInt(sequenceNumber); // Add sequence number
                dataOutputStream.write(chunk); // Add the chunk
                byte[] packetData = byteArrayOutputStream.toByteArray();

				ByteBuffer pkt = makePkt(sessionId, sequenceNumber, totalPackets, FLAG_DATA, packetData); // Create packet with header

                // Send the packet
                DatagramPacket packet = new DatagramPacket(pkt.array(), pkt.remaining(), broadcastAddress, BroadCastTransfer.broadcastListeningPort);
                socket.send(packet);
                sentSize += chunk.length;
                
                UserExperience.printProgressBar(sentSize, totalSize);
                
                if(sequenceNumber%BroadCastTransfer.congestionControlSleepPacketCount == 0 && sequenceNumber != 0) { // Congestion control
                	Thread.sleep(BroadCastTransfer.congestionControlSleepTime);
                }
            }

            // Send an end-of-file packet
           {
                ByteBuffer eob = makePkt(sessionId, -1, totalPackets, FLAG_EOB, null); // EOB packet
                DatagramPacket eofPacket = new DatagramPacket(eob.array(), eob.remaining(), broadcastAddress, BroadCastTransfer.broadcastListeningPort); 
                socket.send(eofPacket);// Send EOB packet
            }

            System.out.println("File broadcast complete (pass 1). Listening for NACKs...");

			// Listen for NACKs and resend missing packets

			for (int round = 0; round < maxRounds; round++) 
			{
                long deadline = System.currentTimeMillis() + waitNacksMs;
                Set<Integer> toRetransmit = new TreeSet<>();
				while (System.currentTimeMillis() < deadline) 
				{
                    try 
					{
                        byte[] nbuf = new byte[2048];
                        DatagramPacket ndp = new DatagramPacket(nbuf, nbuf.length);
                        nackSocket.receive(ndp);

                        String msg = new String(ndp.getData(), 0, ndp.getLength(), StandardCharsets.UTF_8);
                        // Expected: "NACK;<sessionId>;<seq1,seq2,...>"
                        if (!msg.startsWith("NACK;")) continue;
                        String[] parts = msg.split(";", 3);
                        if (parts.length < 2) continue;
                        int sid = Integer.parseInt(parts[1].trim());
                        if (sid != sessionId) continue;

                        if (parts.length == 3 && !parts[2].isEmpty()) {
                            String[] seqs = parts[2].split(",");
                            for (String s : seqs) {
                                s = s.trim();
                                if (!s.isEmpty()) toRetransmit.add(Integer.parseInt(s));
                            }
                        }
                    } catch (IOException e) {
                        // timeout or transient; break the inner wait loop
                        break;
                    }
                }

                if (toRetransmit.isEmpty()) {
                    System.out.println("No NACKs in round " + (round + 1) + ". Done.");
                    break;
                }
				
				System.out.println("Round " + (round + 1) + " retransmitting: " + toRetransmit);

                // Re-broadcast missing chunks
                for (int seq : toRetransmit) {
                    if (seq < 0 || seq >= totalPackets) continue;
                    byte[] chunk = slices.get(seq);

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    DataOutputStream dos = new DataOutputStream(baos);
                    dos.writeInt(seq);
                    dos.write(chunk);
                    byte[] legacyPayload = baos.toByteArray();

                    ByteBuffer pkt = makePkt(sessionId, seq, totalPackets, FLAG_DATA, legacyPayload);
                    DatagramPacket dp = new DatagramPacket(pkt.array(), pkt.remaining(), broadcastAddress, BroadCastTransfer.broadcastListeningPort);
                    socket.send(dp);
                }

                // Re-send EOB to trigger final hole checks at receivers
                ByteBuffer eob = makePkt(sessionId, -1, totalPackets, FLAG_EOB, null);
                socket.send(new DatagramPacket(eob.array(), eob.remaining(), broadcastAddress, BroadCastTransfer.broadcastListeningPort));
            }

            System.out.println("Broadcast (with repairs) complete.");

            // Clean up
            socket.close();
            nackSocket.close();

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }


	
	public static void RecieveFile(String fileHash) {
		BroadcastBeacon beacon = (BroadcastBeacon)ObjectTransfer.recieveObjectBroadcast();
		if (beacon == null) //
        {
            System.err.println("Failed to receive beacon (null). Is the sender running and on the same LAN?");
            return;
        }

		long totalSize = beacon.file.getFileSize();
		
		if(beacon.file.getFileHash().equals(fileHash)) {
			try {
	            // Create a UDP socket
	            DatagramSocket socket = new DatagramSocket(BroadCastTransfer.broadcastListeningPort);

	            // Open the file to write
	            String outputFilePath = new java.io.File(Config.getDownloadsDir(), beacon.file.getFileName()).getPath();
	            //FileOutputStream fileOutputStream = new FileOutputStream(outputFilePath);
	            byte[] bigBuf = new byte[65535]; // Max UDP packet size
	            long recvSize = 0;

				 // Buffer chunks by sequence number (so out-of-order doesn’t corrupt the file)
                Map<Integer, byte[]> bufferBySeq = new HashMap<>();
                int expectedTotal = -1;
                int sessionId = -1;

				// For NACKs
                int nackPort = Config.getBroadcastNackPort();
                int nackDelayMs = Config.getBroadcastNackDelayMs();
                Random jitter = new Random();
	            // Receive packets and write to file
	            while (true) {
	                DatagramPacket packet = new DatagramPacket(bigBuf, bigBuf.length);
	                socket.receive(packet);

					// Parse reliability header
                    Parsed p = parsePkt(packet.getData(), packet.getLength());
                    if (sessionId == -1) sessionId = p.sid;

	                // Extract sequence number and data
	                	if (p.flag == FLAG_DATA) { 
                        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(p.data); // FIXED: p.data
                        DataInputStream dataInputStream = new DataInputStream(byteArrayInputStream);
                        int sequenceNumber = dataInputStream.readInt();
                        int payloadLen = p.data.length - 4;
                        byte[] chunk = new byte[payloadLen];
                        dataInputStream.readFully(chunk);

                        if (!bufferBySeq.containsKey(sequenceNumber)) {
                            bufferBySeq.put(sequenceNumber, chunk);
                            recvSize += chunk.length;
                            UserExperience.printProgressBar(recvSize, totalSize);
                        }

                        if (expectedTotal < 0) expectedTotal = p.total;

                    } else if (p.flag == FLAG_EOB) {
                        if (expectedTotal < 0) expectedTotal = p.total;

                        List<Integer> missing = new ArrayList<>();
                        for (int i = 0; i < expectedTotal; i++) {
                            if (!bufferBySeq.containsKey(i)) missing.add(i);
                        }

                        if (missing.isEmpty()) {
                            try (FileOutputStream fos = new FileOutputStream(outputFilePath)) {
                                for (int i = 0; i < expectedTotal; i++) {
                                    fos.write(bufferBySeq.get(i));
                                }
                            }
                            System.out.println("End of file received (complete).");
                            socket.close();
                            return;
                        } else {
                            int delay = jitter.nextInt(Math.max(1, nackDelayMs));
                            try { Thread.sleep(delay); } catch (InterruptedException ignored) {}

                            missing.removeIf(bufferBySeq::containsKey);

                            if (!missing.isEmpty()) {
                                String body = String.join(",", missing.stream().map(String::valueOf).toArray(String[]::new));
                                String msg = "NACK;" + sessionId + ";" + body;
                                byte[] m = msg.getBytes(StandardCharsets.UTF_8);
                                DatagramPacket ndp = new DatagramPacket(m, m.length, InetAddress.getByName(Config.getBroadcastGroup()), nackPort);
                                DatagramSocket ns = new DatagramSocket();
                                ns.setBroadcast(true);
                                ns.send(ndp);
                                ns.close();
                            }
                        }
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
}
