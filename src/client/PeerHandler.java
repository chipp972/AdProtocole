/* Comment to add */
package client;

import java.io.IOException;
import java.lang.NullPointerException;
import java.lang.NumberFormatException;
import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Scanner;
import java.net.InetAddress;

import utils.Ad;
import utils.Transaction;
import utils.ClientInfo;
import static client.Commande.*;

public class PeerHandler {

	private DatagramSocket udpSock;
	private LinkedList<String> msgList;
	private LinkedList<Transaction> transacList;
	private LinkedList<ClientInfo> peerList;
	private DatagramPacket dp;
	private PeerListener pl;
	private AdClient main;

	public PeerHandler(AdClient m) {
		this.main = m;
		msgList = new LinkedList<String>();
		transacList = new LinkedList<Transaction>();
		peerList = new LinkedList<ClientInfo>();

		try {
			udpSock = new DatagramSocket();
		} catch (SocketException e) {
			e.printStackTrace();
			System.exit(0);
		}
		pl = new PeerListener(udpSock, this);
		pl.setDaemon(true);
		// on lance la thread qui écoute les clients
		pl.start();
	}

	public int getUdpPort() {
		return udpSock.getLocalPort();
	}


	/**
	 * Envoie les messages UDP aux clients/pairs
	 * @param  id l'id de l'annonce
	 * @param  messageType le type d'action à effectuer
	 * @param  message le message à envoyer si il y en a un
	 */
	public void sendClient(String id, String messageType, String message) throws NumberFormatException {
		byte [] buf;
		String msg = "";
		Ad a = null;
		InetAddress ip;
		int port;

		if (messageType.equals("RP")) {
			if ((a = main.getAd(getPeer(id).getAdId())) == null) {
				System.out.println("Probleme sur l'id du pair");
				System.out.println("peer"+getPeer(id)+" "+getPeer(id).getAdId());
				return;
			}
			ip = getPeer(id).getIp();
			port = getPeer(id).getPort();
	 	} else {
			if ((a = main.getAd(id)) == null) {
				System.out.println("Probleme sur l'id du pair");
				return;
			}
			ip = a.getClientInfo().getIp();
			port = a.getClientInfo().getPort();
		}

		try {
			switch (Commande.valueOf(messageType)) {
				case SM:
					msg = new String("AD\r\nID "+a.getAdId()+"\r\nMSG "+message+"\r\n");
				break;
				case RP:
					msg = new String("AD\r\nID "+a.getAdId()+"\r\nMSG "+message+"\r\n");
				break;
				case ST:
					msg = new String("AD\r\nQUERY\r\nID "+a.getAdId()+"\r\n");
				break;
				default:
					System.out.println("Commande inconnue");
					return;
			}
			buf = msg.getBytes();
			dp = new DatagramPacket(buf, msg.length(), ip, port);
			udpSock.send(dp);
		} catch(IOException | NullPointerException e) {
			System.out.println("Erreur lors de l'envoie au client ");
			e.printStackTrace();
		}
	}

	/**
	 * Version pour les transactions
	 * @param  id l'id de la transaction
	 * @param  messageType le type d'action à effectuer
	 * @param  message le message à envoyer si il y en a un
	 */
	public void sendClient(String id, String messageType) throws NumberFormatException {
		byte [] buf;
		String msg = "";
		Transaction t = null;
		Ad a = null;

		if ((t = getTransac(id)) == null) {
			System.out.println("Erreur sur l'id de transaction");
			return;
		}
		if ((a = main.getAd(t.getAdId())) == null) {
			System.out.println("Probleme sur l'annonce de la transaction");
			return;
		}
		try {
			switch (Commande.valueOf(messageType)) {
				case OK:
					msg = new String("AD\r\nACCEPT\r\nID "+a.getAdId()+"\r\n");
				break;
				case KO:
					msg = new String("AD\r\nREFUSE\r\nID "+a.getAdId()+"\r\n");
				break;
				default:
					System.out.println("Commande inconnue");
					return;
			}

			buf = msg.getBytes();
			dp = new DatagramPacket(buf, msg.length(), t.getClientIp(), t.getClientPort());
			udpSock.send(dp);
			if (messageType.equals("OK")) // on suppr l'annonce après ok
				main.sendServer("AD\r\nDEL\r\nID "+a.getAdId()+"\r\n");
		} catch(IOException | NullPointerException e) {
			System.out.println("Erreur lors de l'envoie au client ");
			e.printStackTrace();
		}
	}

	/* Imprimme la liste des transactions proposées par les clients */
	public void listTransac() {
		Iterator<Transaction> it = transacList.iterator();
		if (transacList.isEmpty()) {
			System.out.println("Aucune transaction proposée.");
		} else {
			while (it.hasNext())
				System.out.println(it.next());
		}
	}

	/* Imprimme la liste des messages des clients */
	public void listMsg() {
		Iterator<String> it = msgList.iterator();
		if (msgList.isEmpty()) {
			System.out.println("Aucun message de clients.");
		} else {
			while (it.hasNext()) {
				System.out.println(it.next());
			}
		}
	}

	public void listPeers() {
		Iterator<ClientInfo> it = peerList.iterator();
		if (peerList.isEmpty()) {
			System.out.println("Aucun client dans les pairs.");
		} else {
			while (it.hasNext()) {
				System.out.println(it.next());
			}
		}
	}

	public void addPeer(String id, String adId, InetAddress ip, int port) {
		ClientInfo c = new ClientInfo(id, adId, ip, port);
		if (!peerList.contains(c))
			peerList.add(c);
	}

	public ClientInfo getPeer(String id) {
		Iterator<ClientInfo> it = peerList.iterator();
		ClientInfo tmp;
		if (!peerList.isEmpty()) {
			while (it.hasNext()) {
				tmp = it.next();
				if (tmp.getId().equals(id))
					return tmp;
			}
		}
		return null;
	}

	public synchronized void addClientMessage(String s) {
		msgList.add(s);
	}

	public synchronized void addTransaction(Transaction t) {
		transacList.add(t);
	}

	public Transaction getTransac(String id) {
		Transaction t;
		Iterator<Transaction> it = transacList.iterator();
		while(it.hasNext()) {
			t = it.next();
			if (t.getId().equals(id))
				return t;
		}
		return null;
	}

	public void cleanPeers(String adId) {
		ClientInfo c;
		Iterator<ClientInfo> it = peerList.iterator();
		while(it.hasNext()) {
			c = it.next();
			if (c.getId().equals(adId))
				it.remove();
		}
	}

	// Supprime toutes les transactions qui contiennent l'annonce que l'on vient de céder
	public void cleanTransac(String id) {
		Transaction t;
		String adId = null;
		Iterator<Transaction> it = transacList.iterator();
		while(it.hasNext()) {
			t = it.next();
			if (t.getId().equals(id)) {
				adId = t.getAdId();
				break;
			}
		}
		if (!(adId == null)) {
			it = transacList.iterator();
			while(it.hasNext()) {
				t = it.next();
				if (t.getAdId().equals(adId))
					it.remove();
			}
		}
	}

	// Supprime la transaction identifiée par l'id
	public void rmTransac(String id) {
		Transaction t;
		Iterator<Transaction> it = transacList.iterator();
		while(it.hasNext()) {
			t = it.next();
			if (t.getId().equals(id))
				it.remove();
		}
	}
}
