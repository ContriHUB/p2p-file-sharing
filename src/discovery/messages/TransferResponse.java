package discovery.messages;

import java.io.Serializable;

import discovery.Node;

public class TransferResponse implements Serializable{
	public Node RequestingNode;
	public Node RespondingNode;
	public int Port;
	public Boolean Response;
	public TransferResponse(Node requesting,Node responding , int port, Boolean Response){
		this.Port = port;
		this.RequestingNode = requesting;
		this.RespondingNode = responding;
		this.Response = Response;
	}
	
}
