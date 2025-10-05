package p2p;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;

import utils.Config;

public class ObjectTransfer {

	public static int broadCastListeningPort = Config.getBeaconPort();
    private static final String BROADCAST_GROUP = Config.getBroadcastGroup();

    private static final int MAX_UDP_PAYLOAD = 65507;
    private static final int RECV_BUFFER = 65535;
    
    public static void sendObject(Socket socket, Object object) throws IOException {
        // Create an ObjectOutputStream to send the object
        ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
        // Write the object to the stream
        outputStream.writeObject(object);
        // Flush the stream to ensure the object is sent
        outputStream.flush();
    }

    
    public static Object receiveObject(Socket socket) throws IOException, ClassNotFoundException {
        // Create an ObjectInputStream to receive the object
        ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
        // Read the object from the stream
        return inputStream.readObject();
    }
    
    public static void sendObjectBroadcast(Object obj) {
            try {
            // Serialize the object
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(obj);
            objectOutputStream.flush();
            byte[] serializedObject = byteArrayOutputStream.toByteArray();
            objectOutputStream.close();
            byteArrayOutputStream.close();

            // 
            if (serializedObject.length > MAX_UDP_PAYLOAD) {
                System.err.println("[ObjectTransfer] Serialized object too large for a single UDP packet (" +
                                   serializedObject.length + " > " + MAX_UDP_PAYLOAD + ").");
                return;
            }

            // Create a UDP socket
            try(DatagramSocket socket = new DatagramSocket()){
            socket.setBroadcast(true); // Enable broadcasting

            // Define the broadcast address and port
            InetAddress broadcastAddress = InetAddress.getByName(BROADCAST_GROUP.trim());

            // Create a DatagramPacket with the serialized object
            DatagramPacket packet = new DatagramPacket(serializedObject, serializedObject.length, broadcastAddress, ObjectTransfer.broadCastListeningPort);

            // Send the packet
            socket.send(packet);
            }
            System.out.println("Object broadcasted successfully!");
        }catch (Exception e){
                e.printStackTrace();
            }
        }
    
    public static Object recieveObjectBroadcast() {
    	try {
            // Create a UDP socket
            DatagramSocket socket = new DatagramSocket(ObjectTransfer.broadCastListeningPort); // Listen on the same port

            // Buffer to store incoming data
            byte[] buffer = new byte[RECV_BUFFER];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

            // Receive the packet
            System.out.println("Waiting to receive object...");
            socket.receive(packet);
            System.out.println("Object received!");

            // Deserialize the object
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(packet.getData(), 0, packet.getLength());
            ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
            Object receivedObject = objectInputStream.readObject();
            objectInputStream.close();
            byteArrayInputStream.close();

            // Close the socket
            socket.close();

            // Return the received object
            return receivedObject;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    
}