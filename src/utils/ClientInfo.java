package utils;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class ClientInfo {
	private int id, port;
	private InetAddress ip;

	/* Constructeur utilisé par le serveur */
	public ClientInfo(int id, InetAddress ip, int port) {
		this.id = id;
		this.ip = ip;
		this.port = port;
	}

	/* Constructeur utilisé par le client */
	public ClientInfo(int i, String ip, String port) throws UnknownHostException {
		this.id = i;
		this.port = Integer.parseInt(port);
		this.ip = InetAddress.getByName(ip);
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
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
}
