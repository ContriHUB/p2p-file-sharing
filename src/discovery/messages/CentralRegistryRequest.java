package discovery.messages;

import java.io.Serializable;

import discovery.FileData;
import discovery.Node;

public class CentralRegistryRequest implements Serializable{
	public FileData file;
	public Node owner;
	public String type;
	
	public CentralRegistryRequest(FileData fd , Node owner) {
		//for registering file
		this.file = fd;
		this.owner = owner;
		this.type = "registerfile";
//		System.out.println(this.owner);
//		System.out.println(this.file);
	}
	
	public CentralRegistryRequest(Node owner) {
		// for reistering a peer
		this.owner = owner;
		this.type = "registerpeer";
//		System.out.println(this.owner);
	
	}
	
	public CentralRegistryRequest(String fileHash) {
		// get potential peers
		this.file = new FileData();
		this.file.setFileHash(fileHash);
	
//		System.out.println(this.file);
		this.type = "getpotentialpeers";
		
	}
	
	
	
}
