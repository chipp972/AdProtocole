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

public class PeerHandler {

	private DatagramSocket udpSock;
	private LinkedList<String> msgList;
	private LinkedList<Transaction> transacList;
	private DatagramPacket dp;
	private PeerListener pl;
	private final int TAILLE_COMMANDES = 2;
	private final String ADD_AD = "ad", DEL_AD = "rm", LS_AD = "ls",
	SEND_MSG = "ms", SHW_MSG = "sm", SHW_TRANSAC = "st", ASK_TRANSAC = "ts",
	OK_TRANSAC = "ok", KO_TRANSAC = "ko", HELP = "he", EXIT = "ex";
	private AdClient main;

	public PeerHandler(AdClient m) {
		this.main = m;
		msgList = new LinkedList<String>();
		transacList = new LinkedList<Transaction>();

		try {
			udpSock = new DatagramSocket(0, InetAddress.getByName("localhost"));
		} catch (SocketException | UnknownHostException e) {
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

	public void sendClient(String id, String messageType, String message) throws NumberFormatException {
		byte [] buf;
		String msg = "";
		Ad a = null;
		Transaction t = null;
		if (messageType.equals(OK_TRANSAC) || messageType.equals(KO_TRANSAC)) {
			if ((t = getTransac(Integer.parseInt(id))) == null) {
				System.out.println("Erreur sur l'id de transaction");
				return;
			}
			if((a = main.getAd(t.getAdId())) == null) {
				System.out.println("Probleme sur l'annonce de la transaction");
				return;
			}
		} else {
			if((a = main.getAd(id)) == null) {
				System.out.println("Probleme sur l'annonce de la transaction");
				return;
			}
			// Si on selectionne une de nos annonces on annule l'envoie
			if (a.getClientInfo().getIp().equals(udpSock.getLocalAddress()) &&
				a.getClientInfo().getPort() == udpSock.getLocalPort()) {
					System.out.println("Erreur : vous etes le proprietaire de cette annonce");
					return;
			}
		}
		try {
			switch (messageType) {
				case SEND_MSG:
					msg = new String("AD\r\nID "+a.getAdId()+"\r\nMSG "+message+"\r\n");
				break;
				case ASK_TRANSAC:
					msg = new String("AD\r\nQUERY\r\nID "+a.getAdId()+"\r\n");
				break;
				case OK_TRANSAC:
					msg = new String("AD\r\nACCEPT\r\nID "+a.getAdId()+"\r\n");
				break;
				case KO_TRANSAC:
					msg = new String("AD\r\nREFUSE\r\nID "+a.getAdId()+"\r\n");
				break;
				default:
					System.out.println("Commande inconnue");
					return;
			}
			buf = msg.getBytes();
			if (messageType.equals(OK_TRANSAC) || messageType.equals(KO_TRANSAC)) {
				dp = new DatagramPacket(buf, msg.length(), t.getClientIp(), t.getClientPort());
			} else {
				dp = new DatagramPacket(buf, msg.length(), a.getClientInfo().getIp(), a.getClientInfo().getPort());
			}
			udpSock.send(dp);
			if (messageType.equals(OK_TRANSAC)) // on suppr l'annonce après ok
				main.sendServer("AD\r\nDEL\r\nID "+a.getAdId()+"\r\n");
		} catch(IOException | NullPointerException e) {
			System.out.println("Erreur lors de l'envoie au client ");
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


	public synchronized void addClientMessage(String s) {
		msgList.add(s);
	}

	public synchronized void addTransaction(Transaction t) {
		transacList.add(t);
	}

	public Transaction getTransac(int id) {
		Transaction t;
		Iterator<Transaction> it = transacList.iterator();
		while(it.hasNext()) {
			t = it.next();
			if (t.getId() == id)
				return t;
		}
		return null;
	}

	// Supprime toutes les transactions en rapport avec l'id d'annonce donné
	public void cleanTransac(String adId) {
		Transaction t;
		Iterator<Transaction> it = transacList.iterator();
		while(it.hasNext()) {
			t = it.next();
			if (t.getAdId() == adId)
				it.remove();
		}
	}
	// Supprime toutes les transactions qui contiennent l'annonce que l'on vient de céder
	public void cleanTransac(int id) {
		Transaction t;
		String adId = null;
		Iterator<Transaction> it = transacList.iterator();
		while(it.hasNext()) {
			t = it.next();
			if (t.getId() == id) {
				adId = t.getAdId();
				break;
			}
		}
		if (!(adId == null))
			cleanTransac(adId);
	}

	// Supprime la transaction identifiée par l'id
	public void rmTransac(int id) {
		Transaction t;
		Iterator<Transaction> it = transacList.iterator();
		while(it.hasNext()) {
			t = it.next();
			if (t.getId() == id)
				it.remove();
		}
	}
}
