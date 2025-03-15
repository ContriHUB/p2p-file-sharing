package discovery.messages;

import java.io.Serializable;

import discovery.FileData;
import discovery.Node;
public class BroadcastBeacon implements Serializable{
	public FileData file;
	public Node sendingPeer;
	
	public BroadcastBeacon(FileData f , Node s){
		this.file = f;
		this.sendingPeer = s;
	}
}
