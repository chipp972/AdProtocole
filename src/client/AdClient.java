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
import static client.Commande.*;

public class AdClient extends Thread {

	public static final String LOCAL_SERVER = "localhost";

	/* Communication avec le serveur */
	private ServerHandler sh;

	/* Communication avec l'utilisateur */
	private boolean end;
	private Scanner userIn;

	/* Communication avec les clients */
	private PeerHandler ph;

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

	public void cleanPeers(String adId) {
		ph.cleanPeers(adId);
	}

	private void printCommands() {
		for (Commande c : Commande.values()) {
			System.out.println(c);
		}
	}

	public void run() {
		String choice = "";
		String[] strs;

		printCommands();

		while (userIn.hasNextLine() && !end) {
			choice = userIn.nextLine();
			try {
				if(choice.length() >= Commande.getSize()) {
					switch (Commande.valueOf(choice.substring(0, Commande.getSize()))) {
					case AD:
						strs = choice.trim().split(" ", 2);
						if (strs.length > 1)
							sendServer("AD\r\nADD\r\nMSG "+strs[1].trim()+"\r\n");
						else
							System.out.println("Erreur dans la commande");
					break;
					case RM:
						strs = choice.trim().split(" ");
						if (strs.length > 1)
							sendServer("AD\r\nDEL\r\nID "+strs[1].trim()+"\r\n");
						else
							System.out.println("Erreur dans la commande");
					break;
					case LS:
						sh.listAds();
					break;
					case SM:
						strs = choice.trim().split(" ", 3);
						if (strs.length > 2)
							ph.sendClient(strs[1], strs[0], strs[2]);
						else
							System.out.println("Erreur dans la commande");
					break;
					case RP:
						strs = choice.trim().split(" ", 3);
						if (strs.length > 2)
							ph.sendClient(strs[1], strs[0], strs[2]);
						else
							System.out.println("Erreur dans la commande");
					break;
					case LP:
						ph.listPeers();
					break;
					case ST:
						strs = choice.trim().split(" ");
						if (strs.length > 1)
							ph.sendClient(strs[1].trim(), strs[0].trim(), null);
						else
							System.out.println("Erreur dans la commande");
					break;
					case OK:
					case KO:
						strs = choice.trim().split(" ");
						if (strs.length > 1) {
							ph.sendClient(strs[1].trim(), strs[0].trim());
							ph.cleanTransac(strs[1].trim());
						} else {
							System.out.println("Erreur dans la commande");
						}
					break;
					case LM:
						ph.listMsg();
					break;
					case LT:
						ph.listTransac();
					break;
					case EX:
						end = true;
						sendServer("DISCONNECT\r\n");
					break;
					case HE:
						printCommands();
					break;
					default:
						System.out.println("Choix invalide.");
						System.out.println("\"he\" pour un rappel des commandes");
					}
				}
			} catch (NumberFormatException e) {
				System.out.println("Erreur sur un id dans la commande");
			} catch (IllegalArgumentException e) {
				System.out.println("Erreur dans la commande");
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
