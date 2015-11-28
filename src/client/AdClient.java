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
import client.ServerHandler;
import client.PeerHandler;

public class AdClient extends Thread {

	public static final String LOCAL_SERVER = "localhost";

	/* Communication avec le serveur */
	private ServerHandler sh;

	/* Communication avec l'utilisateur */
	private boolean end;
	private Scanner userIn;

	/* Communication avec les clients */
	private PeerHandler ph;

	private final int TAILLE_COMMANDES = 2;
	private final String ADD_AD = "ad", DEL_AD = "rm", LS_AD = "ls",
	SEND_MSG = "ms", SHW_MSG = "sm", SHW_TRANSAC = "st", ASK_TRANSAC = "ts",
	OK_TRANSAC = "ok", KO_TRANSAC = "ko", HELP = "he", EXIT = "ex";

	/* Constructeur */
	public AdClient(InetAddress a) {
		userIn = new Scanner(System.in);
		end = false;

		ph = new PeerHandler(this);
		sh = new ServerHandler(this, a, ph);
	}

	public void endCommunication() {
		end = true;
	}

	public void sendServer(String s) {
		sh.sendServer(s);
	}

	public Ad getAd(String adId) {
		return sh.getAd(adId);
	}

	public void cleanTransac(String adId) {
		ph.cleanTransac(adId);
	}

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

		printCommands();

		while (userIn.hasNextLine() && !end) {
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
						sh.listAds();
					break;
					case SEND_MSG:
						strs = choice.trim().split(" ", 3);
						if (strs.length > 2)
							ph.sendClient(strs[1], strs[0], strs[2]);
						else
							System.out.println("Erreur dans la commande");
					break;
					case ASK_TRANSAC:
						strs = choice.trim().split(" ");
						if (strs.length > 1)
							ph.sendClient(strs[1].trim(), strs[0].trim(), null);
						else
							System.out.println("Erreur dans la commande");
					break;
					case OK_TRANSAC:
					case KO_TRANSAC:
						strs = choice.trim().split(" ");
						if (strs.length > 1) {
							ph.sendClient(strs[1].trim(), strs[0].trim(), null);
							ph.cleanTransac(Integer.parseInt(strs[1].trim()));
						} else {
							System.out.println("Erreur dans la commande");
						}
					break;
					case SHW_MSG:
						ph.listMsg();
					break;
					case SHW_TRANSAC:
						ph.listTransac();
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
		sh.close();
		userIn.close();
	}

	public static void main(String args[]) {
		AdClient c = null;
		try {
			if(args.length == 0) { //Sur la même machine
				c = new AdClient(InetAddress.getByName(LOCAL_SERVER));
			} else if(args.length == 1) { //Sur le réseau local
				c = new AdClient(InetAddress.getByName(args[0]));
			} else {
				System.out.println("Usage 1 : java -jar client.jar");
				System.out.println("Usage 2 : java -jar client.jar Server-address");
				System.exit(0);
			}
			c.start();
			c.join();

		} catch (UnknownHostException e) {
			System.out.println("Adresse serveur inconnue : "+args[0]);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
