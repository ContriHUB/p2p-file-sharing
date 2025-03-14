package discovery.messages;

import java.io.Serializable;

import discovery.FileData;
import discovery.Node;

public class TransferRequest implements Serializable{
	public Node RequestingNode;
	public int Port;
	public FileData Fd;
	public TransferRequest(Node Sender , FileData fd , int port){
		this.Fd = fd;
		this.RequestingNode = Sender;
		this.Port = port;
	}
}
