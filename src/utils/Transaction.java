package utils;

import java.net.InetAddress;
import utils.ClientInfo;

public class Transaction {
	private int port;
	private String id, adId;
	private InetAddress ip;

	// On préfère ip, port à client id ici puisqu'un client n'ayant posté aucune
	// annonce peut envoyer une transaction
	public Transaction(String i, int p, InetAddress ad, String a) {
		this.id = i;
		this.ip = ad;
		this.port = p;
		this.adId = a;
	}

	public String getId() {
		return id;
	}

	public InetAddress getClientIp() {
		return ip;
	}

	public int getClientPort() {
		return port;
	}


	public String getAdId() {
		return adId;
	}

	public String toString() {
		return new String("Transaction : "+id+"|Client : "+ip+"/"+port+"|Annonce : "+adId);
	}

	public boolean equals(Transaction t) {
		if (this.id.equals(t.getId()))
			return true;
		return false;
	}
}
