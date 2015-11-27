package utils;

import utils.ClientInfo;

public class Transaction {
	private int id, clientId;
	private String adId;

	public Transaction(int i, int c, String a) {
		this.id = i;
		this.clientId = c;
		this.adId = a;
	}

	public int getId() {
		return id;
	}

	public int getClientId() {
		return clientId;
	}

	public String getAdId() {
		return adId;
	}

	public String toString() {
		return new String(id+"|C"+clientId+"|A"+adId);
	}
}
