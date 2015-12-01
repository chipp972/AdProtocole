package utils;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class ClientInfo {
	private int port;
	private InetAddress ip;
	private String id, adId;

	/* Constructeur utilisé par le serveur */
	public ClientInfo(String id, InetAddress ip, int port) {
		this.id = id;
		this.ip = ip;
		this.port = port;
		this.adId = "0";
	}

	/* Constructeur utilisé par le client lorsqu'il reçoit les annonces du serveur */
	public ClientInfo(String id, String adId, String ip, String port) throws UnknownHostException {
		this.id = id; // peerId
		this.port = Integer.parseInt(port);
		this.ip = InetAddress.getByName(ip);
		this.adId = adId; // sujet de conversation
	}

	public String getId() {
		return id;
	}

	public String getAdId() {
		return adId;
	}

	public InetAddress getIp() {
		return ip;
	}

	public void setIp(InetAddress ip) {
		this.ip = ip;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String toString() {
		return new String("Pair "+id+" = "+ip+":"+port);
	}

	public boolean equals(ClientInfo c) {
		if (this.ip.equals(c.getId()) && this.port == c.getPort())
			return true;
		return false;
	}
}
