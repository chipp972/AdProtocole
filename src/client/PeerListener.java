/*
 * Serveur udp client qui parse les paquets reçus et les renvoie au thread
 * client principal
 */

package client;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.DatagramPacket;

public class PeerListener extends Thread {
	private DatagramSocket s;
	private DatagramPacket dp;
	private byte [] buf;
	private boolean end;
	private final int TAILLE_MAX_BUFFER = 1000;

	public PeerListener(DatagramSocket s) {
		this.end = false;
		this.s = s;
		this.buf = new byte[TAILLE_MAX_BUFFER];
		this.dp = new DatagramPacket(buf, TAILLE_MAX_BUFFER);
	}

	public void close() {
		end = true;
	}

	public void run() {
		while (true) {
			try {
				s.receive(dp);
				// parsing et traitement dans un switch
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(0);
			}
		}
	}
}
