package discovery;

import java.io.Serializable;

public class Node implements Serializable{
	// Peer identification fields
    private String peerId; // Unique identifier for the peer
    private String peerName; // Human-readable name for the peer
    private String peerIP; // IP address of the peer
    private int peerPort; // handshake registry port

    // Constructor
    public Node(String peerId, String peerName, String peerIP, int peerPort) {
        this.peerId = peerId;
        this.peerName = peerName;
        this.peerIP = peerIP;
        this.peerPort = peerPort;
    }
    public Node() {
    	// default used for testing
    	this.peerIP = "127.0.0.1";
    	this.peerPort = 3000;
    }
    // Getters and Setters
    public String getPeerId() {
        return peerId;
    }

    public void setPeerId(String peerId) {
        this.peerId = peerId;
    }

    public String getPeerName() {
        return peerName;
    }

    public void setPeerName(String peerName) {
        this.peerName = peerName;
    }

    public String getPeerIP() {
        return peerIP;
    }

    public void setPeerIP(String peerIP) {
        this.peerIP = peerIP;
    }

    public int getPeerPort() {
        return peerPort;
    }

    public void setPeerPort(int peerPort) {
        this.peerPort = peerPort;
    }

    // toString method for easy debugging
    @Override
    public String toString() {
        return "peer{" +
                "peerId='" + peerId + '\'' +
                ", peerName='" + peerName + '\'' +
                ", peerIP='" + peerIP + '\'' +
                ", peerPort=" + peerPort +
                '}';
    }
}
