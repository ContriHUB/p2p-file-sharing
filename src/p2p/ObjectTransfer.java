package p2p;

import java.io.*;
import java.net.Socket;

public class ObjectTransfer {
    /**
     * Sends a serializable object over the provided socket's output stream.
     *
     * @param socket The socket to send the object through.
     * @param object The serializable object to send.
     * @throws IOException If an I/O error occurs.
     */
    public static void sendObject(Socket socket, Object object) throws IOException {
        // Create an ObjectOutputStream to send the object
        ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
        // Write the object to the stream
        outputStream.writeObject(object);
        // Flush the stream to ensure the object is sent
        outputStream.flush();
    }

    /**
     * Receives a serializable object from the provided socket's input stream.
     *
     * @param socket The socket to receive the object from.
     * @return The deserialized object.
     * @throws IOException            If an I/O error occurs.
     * @throws ClassNotFoundException If the class of the received object cannot be found.
     */
    public static Object receiveObject(Socket socket) throws IOException, ClassNotFoundException {
        // Create an ObjectInputStream to receive the object
        ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
        // Read the object from the stream
        return inputStream.readObject();
    }
}