package discovery.messages;

import discovery.Node;

import java.io.Serializable;

import discovery.FileData;

public class CentralRegistryResponse implements Serializable{
	public Boolean sucess;
	public Node[] peers;
	public FileData file;
	
	
	public CentralRegistryResponse(Boolean res , Node owner , FileData f) {
		
		// file reistration response
		this.file = f;
		this.peers = new Node[1];
		this.peers[0] = owner;
		this.sucess = res;
		
		System.out.println(this.sucess);
		System.out.println(this.peers[0]);
		System.out.println(this.file);
	}
	
	public CentralRegistryResponse(Boolean res , Node owner ) {
		// peer registration Response
		this.peers = new Node[1];
		this.peers[0] = owner;
		this.sucess = res;
		System.out.println(this.sucess);
		System.out.println(this.peers[0]);
	
	}
	
	public CentralRegistryResponse(Boolean res , Node[] peers) {
		// sample peer response
		this.peers = peers;
		this.sucess = res;
		System.out.println(this.sucess);
		System.out.println(this.peers[0]);
	}
}
