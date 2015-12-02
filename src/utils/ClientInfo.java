package utils;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class ClientInfo {
	private int port;
	private InetAddress ip;
	private String id, adId;

/* Constructeur utilisé par le serveur */
	/**
	 * Constructeur utilisé par le serveur et par le client
	 * @param   id   l'id du client (serveur) / pair (client)
	 * @param   adId l'id de l'annonce (client)
	 * @param   ip   l'ip du client
	 * @param   port le port du client
	 */
	public ClientInfo(String id, String adId, InetAddress ip, int port) {
		this.id = id; // clientId sur le server et peerId sur le client
		this.ip = ip;
		this.port = port;
		this.adId = adId;
	}

	/**
	 * Constructeur utilisé par le client lorsqu'il reçoit les annonces du serveur
	 * @param   ip    L'ip d'un client
	 * @param   port  Le port d'un client
	 */
	public ClientInfo(String ip, String port) throws UnknownHostException {
		this.port = Integer.parseInt(port);
		this.ip = InetAddress.getByName(ip);
		this.adId = null;
		this.id = null;
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
		return new String("Pair "+id+" = "+ip.getHostAddress()+":"+port);
	}

	public boolean equals(ClientInfo c) {
		if (this.ip.equals(c.getIp()) && this.port == c.getPort())
			return true;
		return false;
	}
}
