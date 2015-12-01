package utils;

public class Ad {
	private String msg, adId;
	private ClientInfo clientInfo;

	/* Utilisé par le serveur */
	public Ad(String msg, ClientInfo c) {
		this.msg = msg;
		this.clientInfo = c;
	}

	/* Utilisé par le client */
	public Ad(String id, String msg, ClientInfo c) {
		this.adId = id;
		this.msg = msg;
		this.clientInfo = c;
	}

	public String toMessage() {
		return new String("AD\r\nSEND\r\n"+
						"ID "+getAdId()+"\r\n"+
						"IPv4 "+getClientInfo().getIp()+"\r\n"+
						"PORT "+getClientInfo().getPort()+"\r\n"+
						"MSG "+getMsg()+"\r\n");
	}

	public String toDelMessage() {
		return new String("AD\r\nDEL\r\nID "+getAdId()+"\r\n");
	}

	public String toClientOutput() {
		return new String("Annonce : |"+getAdId()+"|"+getClientInfo().getIp()+
						":"+getClientInfo().getPort()+"| = "+getMsg());
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public String getAdId() {
		return adId;
	}

	public void setAdId(String adId) {
		this.adId = adId;
	}

	public ClientInfo getClientInfo() {
		return clientInfo;
	}

	public void setClientInfo(ClientInfo clientInfo) {
		this.clientInfo = clientInfo;
	}

	public boolean equals(Ad a) {
		if (this.adId.equals(a.getAdId()))
			return true;
		return false;
	}
}
