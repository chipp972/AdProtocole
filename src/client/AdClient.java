package client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
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
import client.PeerListener;

public class AdClient extends Thread {
	/* Données serveur */
	public static final String LOCAL_SERVER = "localhost";
	public static final int PORT_SERVER = 1027;

	/* Communication avec le serveur */
	private LinkedList<Ad> adList;
	private Socket servSock;
	private DataInputStream in;
	private DataOutputStream out;
	private Scanner userIn;
	private ServerListener sl;
	private boolean end;

	/* Communication avec les clients */
	private LinkedList<String> msgList;
	private LinkedList<Transaction> transacList;
	private DatagramSocket udpSock;
	private DatagramPacket dp;
	private PeerListener pl;
	private final int TAILLE_COMMANDES = 2;
	private final String ADD_AD = "ad", DEL_AD = "rm", LS_AD = "ls",
	SEND_MSG = "ms", SHW_MSG = "sm", SHW_TRANSAC = "st", ASK_TRANSAC = "ts",
	OK_TRANSAC = "ok", KO_TRANSAC = "ko", HELP = "he", EXIT = "ex";

	/* Constructeur */
	public AdClient() {
		userIn = new Scanner(System.in);
		end = false;
		adList = new LinkedList<Ad>();
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
	}

	/* transacListhodes concernants le serveur */
	public void connexion(Socket s) throws IOException {
		this.servSock = s;
		in = new DataInputStream(servSock.getInputStream());
		out = new DataOutputStream(servSock.getOutputStream());
		sl = new ServerListener(in, this);

		System.out.println("Client connecte au serveur "+ servSock);
	}

	/* Envoie la String s au serveur */
	public void sendServer(String s) {
		try {
			out.writeUTF(s);
			out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/* Imprimme la liste des annonces récupérées sur la sortie standard */
	synchronized public void listAds() {
		Iterator<Ad> it = adList.iterator();
		if (adList.isEmpty()) {
			System.out.println("Aucune annonce sur le serveur.");
		} else {
			while(it.hasNext())
				System.out.println(it.next().toClientOutput());
		}
	}

	synchronized public void addAd(Ad a) {
		adList.add(a);
	}

	synchronized public void delAd(String id) {
		Iterator<Ad> it = adList.iterator();
		Ad a;
		while(it.hasNext()) {
			a = it.next();
			if(a.getAdId().equals(id)) {
				it.remove();
				break;
			}
		}
	}

	/* Ferme les buffers de lecture, d'écriture, le scanner et la socket */
	public void closeConnexion() {
		try {
			in.close();
			out.close();
			userIn.close();
			servSock.close();
		} catch (IOException e) {
			System.out.println("Erreur en fermant la communication.");
			e.printStackTrace();
		}
	}

	public void endCommunication() {
		end = true;
	}

	/* Méthodes concernants les clients */
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
			if((a = getAd(t.getAdId())) == null) {
				System.out.println("Probleme sur l'annonce de la transaction");
				return;
			}
		} else {
			if((a = getAd(id)) == null) {
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
				sendServer("AD\r\nDEL\r\nID "+a.getAdId()+"\r\n");
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


	/* Permet d'obtenir l'id d'un client à partir de son adresse et son port
	 * ou -1 si l'addresse et le port donnés ne correspondent à personne */
	public int resolveClientId(InetAddress add, int port) {
		ClientInfo c;
		Iterator<Ad> it = adList.iterator();
		while(it.hasNext()) {
			c = it.next().getClientInfo();
			if (c.getIp().equals(add) && c.getPort() == port)
				return c.getId();
		}
		return -1;
	}

	/* Permet d'obtenir l'annonce à partir de son id */
	public Ad getAd(String adId) {
		Ad a;
		Iterator<Ad> it = adList.iterator();
		while(it.hasNext()) {
			a = it.next();
			if (a.getAdId().equals(adId)) {
				return a;
			}
		}
		return null;
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

	// Vérifie que l'id du client correspond bien à l'auteur de l'annonce
	public boolean checkAuthor(int id, String adId) {
		Ad a;
		Iterator<Ad> it = adList.iterator();
		while(it.hasNext()) {
			a = it.next();
			if (a.getAdId().equals(adId)) {
				if (a.getClientInfo().getId() == id)
					return true;
				else
					break;
			}
		}
		return false;
	}

	/* Fonctions qui concernent la gestion des entrées clavier */
	private void printCommands() {
		System.out.println("Ajout d'une annonce : "+ADD_AD+" <message de l'annonce>");
		System.out.println("Supression d'une annonce : "+DEL_AD+" <id-annonce>");
		System.out.println("Lister toutes les annonces : "+LS_AD);

		System.out.println("Envoyer un message a un client : "+SEND_MSG+" <id-annonce> <message>");
		System.out.println("Afficher les messages des clients : "+SHW_MSG);
		System.out.println("Afficher les demandes de transaction : "+SHW_TRANSAC);
		System.out.println("Demander de conclure une transaction : "+ASK_TRANSAC+" <id-annonce>");
		System.out.println("Accepter une transaction : "+OK_TRANSAC+" <id-transaction>");
		System.out.println("Refuser une transaction : "+KO_TRANSAC+" <id-transaction>");
		System.out.println("Afficher cette aide : "+HELP);
		System.out.println("Deconnexion : "+EXIT);
	}

	public void run() {
		String choice = "";
		String[] strs;
		// Envoie du port UDP
		sendServer("USR\r\nPORT "+udpSock.getLocalPort()+"\r\n");
		printCommands();
		sl.start(); // on lance le thread qui écoute le serveur
		pl.start(); // on lance le thread qui écoute les clients
		while(userIn.hasNextLine() && !end) {
			choice = userIn.nextLine();
			try {
				if(choice.length() >= TAILLE_COMMANDES) {
					switch(choice.substring(0, TAILLE_COMMANDES)) {
					case ADD_AD:
						strs = choice.trim().split(" ", 2);
						if (strs.length > 1)
							sendServer("AD\r\nADD\r\nMSG "+strs[1].trim()+"\r\n");
						else
							System.out.println("Erreur dans la commande");
					break;
					case DEL_AD:
						strs = choice.trim().split(" ");
						if (strs.length > 1)
							sendServer("AD\r\nDEL\r\nID "+strs[1].trim()+"\r\n");
						else
							System.out.println("Erreur dans la commande");
					break;
					case LS_AD:
						listAds();
					break;
					case SEND_MSG:
						strs = choice.trim().split(" ", 3);
						if (strs.length > 2)
							sendClient(strs[1], strs[0], strs[2]);
						else
							System.out.println("Erreur dans la commande");
					break;
					case ASK_TRANSAC:
						strs = choice.trim().split(" ");
						if (strs.length > 1)
							sendClient(strs[1].trim(), strs[0].trim(), null);
						else
							System.out.println("Erreur dans la commande");
					break;
					case OK_TRANSAC:
					case KO_TRANSAC:
						strs = choice.trim().split(" ");
						if (strs.length > 1) {
							sendClient(strs[1].trim(), strs[0].trim(), null);
							cleanTransac(Integer.parseInt(strs[1].trim()));
						} else {
							System.out.println("Erreur dans la commande");
						}
					break;
					case SHW_MSG:
						listMsg();
					break;
					case SHW_TRANSAC:
						listTransac();
					break;
					case EXIT:
						end = true;
						sendServer("DISCONNECT\r\n");
					break;
					case HELP:
						printCommands();
					break;
					default:
						System.out.println("Choix invalide.");
						System.out.println("\"he\" pour un rappel des commandes");
					}
				}
			} catch (NumberFormatException e) {
				System.out.println("Erreur sur un id dans la commande");
			}
		}
		sl.close();
		closeConnexion();
	}

	public static void main(String args[]) {
		AdClient c = null;
		try {
			if(args.length == 0) { //Sur la même machine
				c = new AdClient();
				c.connexion(new Socket(LOCAL_SERVER, PORT_SERVER));

			} else if(args.length == 1) { //Sur le réseau local
				c = new AdClient();
				c.connexion(new Socket(InetAddress.getByName(args[0]), PORT_SERVER));

			} else {
				System.out.println("Usage 1 : java -jar client.jar");
				System.out.println("Usage 2 : java -jar client.jar Server-address");
				System.exit(0);
			}

			c.start();
			c.join();

		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("Le serveur n'est pas exécuté. Fermeture du client.");
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
