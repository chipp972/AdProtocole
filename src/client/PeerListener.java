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
import utils.ClientInfo;
import utils.Transaction;

public class PeerListener extends Thread {
	private DatagramSocket s;
	private DatagramPacket dp;
	private byte [] buf;
	private boolean end;
	private PeerHandler main;
	private int transacId;
	private int cId;
	private final int TAILLE_MAX_BUFFER = 1000;

	public PeerListener(DatagramSocket s, PeerHandler m) {
		this.end = false;
		this.s = s;
		this.buf = new byte[TAILLE_MAX_BUFFER];
		this.dp = new DatagramPacket(buf, TAILLE_MAX_BUFFER);
		this.main = m;
		this.transacId = 0;
		this.cId = 0;
	}

	public void run() {
		String message, adId;
		String[] strs;
		while (true) {
			try {
				s.receive(dp);
				message = new String(dp.getData());
				// Parsing et traitement du message
				strs = message.split("\r\n");
				if (strs[0].trim().equals("AD")) {
					adId = strs[2].trim().substring(3).trim();

					switch (strs[1].trim()) {
						case "QUERY":
							// on rajoute une transaction à la liste
							main.addTransaction(new Transaction((transacId++)+"", dp.getPort(), dp.getAddress(), adId));
						break;
						case "ACCEPT":
							// On indique qu'une transaction a été accepté
								main.addClientMessage("Annonce "+adId+" = Transaction acceptee");
						break;
						case "REFUSE":
							// On indique qu'on a eu un refus
								main.addClientMessage("Annonce "+adId+" = Refus de transaction");
						break;
						default:
							if(strs[1].trim().substring(0, 2).equals("ID")) {
								// il faut ajouter un message client à la liste
								main.addClientMessage("Client "+dp.getAddress()+"/"+dp.getPort()+" on Ad "+strs[1].trim().substring(3).trim()+" = "+strs[2].trim().substring(3).trim());
								// On ajoute le client à la liste des pairs
								main.addPeer(""+cId++, strs[1].trim().substring(3).trim(), dp.getAddress(), dp.getPort());
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
