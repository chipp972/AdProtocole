/*
 * Serveur udp client qui parse les paquets reçus et les renvoie au thread
 * client principal
 */

package client;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.InetAddress;

import client.AdClient;
import utils.Transaction;

public class PeerListener extends Thread {
	private DatagramSocket s;
	private DatagramPacket dp;
	private byte [] buf;
	private boolean end;
	private AdClient main;
	private int transacId;
	private final int TAILLE_MAX_BUFFER = 1000;

	public PeerListener(DatagramSocket s, AdClient m) {
		this.end = false;
		this.s = s;
		this.buf = new byte[TAILLE_MAX_BUFFER];
		this.dp = new DatagramPacket(buf, TAILLE_MAX_BUFFER);
		this.main = m;
		this.transacId = 0;
	}

	public void run() {
		String message, adId;
		String[] strs;
		int port, id;
		InetAddress ip;
		while (true) {
			try {
				s.receive(dp);
				message = new String(dp.getData());
				port = dp.getPort();
				ip = dp.getAddress();
				id = main.resolveClientId(ip, port);

				// Parsing et traitement du message
				System.out.println("test : "+message);

				strs = message.split("\r\n");
				if (strs[0].trim().equals("AD")) {
					adId = strs[2].trim().substring(3).trim();

					switch (strs[1].trim()) {
						case "QUERY":
							// on rajoute une transaction à la liste
							main.addTransaction(new Transaction((transacId++), id, adId));
						break;
						case "ACCEPT":
							// On indique qu'une transaction a été accepté
							if (main.checkAuthor(id, adId))
								main.addClientMessage("Ad"+adId+" = Transaction acceptée");
						break;
						case "REFUSE":
							// On indique qu'on a eu un refus
							if (main.checkAuthor(id, adId))
								main.addClientMessage("Ad"+adId+" = Refus de transaction");
						break;
						default:
							if(strs[1].trim().substring(0, 2).equals("ID")) {
								// il faut ajouter un message client à la liste
								main.addClientMessage("Client "+id+" on Ad "+strs[1].trim().substring(3).trim()+" = "+strs[2].trim());
							}
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(0);
			}
		}
	}
}
