package discovery.messages;

import java.io.Serializable;
import java.security.PublicKey;

import discovery.FileData;
import discovery.Node;

public class FileRequest implements Serializable{
	public FileData FileData;
	public Node RequestingNode;
	public PublicKey pub;
	public FileRequest(FileData f , Node n){
		this.FileData = f;
		this.RequestingNode = n;
	}
}
