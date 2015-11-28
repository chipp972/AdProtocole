package utils;

import java.net.InetAddress;
import utils.ClientInfo;

public class Transaction {
	private int id, port;
	private String adId;
	private InetAddress ip;

	// On préfère ip, port à client id ici puisqu'un client n'ayant posté aucune
	// annonce peut envoyer une transaction
	public Transaction(int i, int p, InetAddress ad, String a) {
		this.id = i;
		this.ip = ad;
		this.port = p;
		this.adId = a;
	}

	public int getId() {
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
}
