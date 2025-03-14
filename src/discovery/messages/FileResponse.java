package discovery.messages;

import java.io.Serializable;

import discovery.FileData;
import discovery.Node;

public class FileResponse implements Serializable{
	public Node RequestingNode;
	public Node RespondingNode;
	public FileData file;
	public Boolean response;
	public 	FileResponse(Node RequestingNode , Node RespondingNode , Boolean res , FileData f){
		this.RequestingNode = RequestingNode;
		this.RespondingNode = RespondingNode;
		this.response = res;
		this.file = f;
	}
}
