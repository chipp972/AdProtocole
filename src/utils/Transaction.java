package utils;

import utils.ClientInfo;

public class Transaction {
	private int id, clientId, adId;

	public Transaction(int i, int c, int a) {
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

	public int getAdId() {
		return adId;
	}

	public String toString() {
		return new String(id+"|C"+clientId+"|A"+adId);
	}
}
