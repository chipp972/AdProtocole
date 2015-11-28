package client;

import java.io.DataInputStream;
import java.io.IOException;

import utils.Ad;
import utils.ClientInfo;

public class ServerListener extends Thread {
	private DataInputStream in;
	private boolean end;
	private AdClient main;
	private int cId;

	public ServerListener(DataInputStream in, AdClient main) {
		this.in = in;
		this.end = false;
		this.main = main;
		this.cId = 0;
	}

	public void close() {
		end = true;
	}

	public void run() {
		while(!end) {
			try {
				String s = in.readUTF();
				String[] strs = s.split("\r\n");
				for(int i = 0; i < strs.length && end == false; i++) {
					switch(strs[i].trim()) {
					case "AD":
						i++;
						switch(strs[i].trim()) {
						case "SEND":
							i++;
							main.addAd(new Ad(strs[i].trim().substring(3), strs[i+3].trim().substring(4),
									new ClientInfo(cId++, strs[i+1].trim().substring(6),
											strs[i+2].trim().substring(5))));
							i += 3;
							break;
						case "DEL":
							i++;
							main.delAd(strs[i].trim().substring(3));
							main.cleanTransac(strs[i].trim().substring(3)); // On supprime les annonces également
							break;
						}
						break;
					case "SYS":
						i++;
						switch(strs[i].trim()) {
						case "ERROR 001":
							System.out.println("Requete incorrect");
							break;
						case "ERROR 002":
							System.out.println("Requete refusee");
							break;
						}
						break;
					default:
						System.out.println("Message inconnu : "+strs[i].trim());
					}
				}
			} catch (IOException e) {
				// Le serveur a crash
				System.out.println("Fin de communication avec le serveur.");
				System.out.println("Appuyez sur entrée pour terminer...");
				end = true;
				main.endCommunication();
			} catch (NullPointerException e) {
				break;
			}
		}
	}
}
