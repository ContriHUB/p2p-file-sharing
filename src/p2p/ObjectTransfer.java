package p2p;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;

public class ObjectTransfer {
	public static int broadCastListeningPort = 12345;
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

            // Create a UDP socket
            DatagramSocket socket = new DatagramSocket();
            socket.setBroadcast(true); // Enable broadcasting

            // Define the broadcast address and port
            InetAddress broadcastAddress = InetAddress.getByName("255.255.255.255");
            

            // Create a DatagramPacket with the serialized object
            DatagramPacket packet = new DatagramPacket(serializedObject, serializedObject.length, broadcastAddress, ObjectTransfer.broadCastListeningPort);

            // Send the packet
            socket.send(packet);
            System.out.println("Object broadcasted successfully!");

            // Close the socket
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static Object recieveObjectBroadcast() {
    	try {
            // Create a UDP socket
            DatagramSocket socket = new DatagramSocket(ObjectTransfer.broadCastListeningPort); // Listen on the same port

            // Buffer to store incoming data
            byte[] buffer = new byte[1024];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

            // Receive the packet
            System.out.println("Waiting to receive object...");
            socket.receive(packet);
            System.out.println("Object received!");

            // Deserialize the object
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(packet.getData());
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