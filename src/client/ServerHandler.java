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

public class ServerHandler {

	/* Données serveur */
	public final int PORT_SERVER = 1027;

	private LinkedList<Ad> adList;
	private Socket servSock;
	private DataInputStream in;
	private DataOutputStream out;
	private ServerListener sl;
	private AdClient main;

	public ServerHandler(AdClient m, InetAddress a, PeerHandler ph) {
		main = m;
		adList = new LinkedList<Ad>();
		try {
			connexion(new Socket(a, PORT_SERVER));
		} catch (IOException e) {
			System.out.println("Le serveur n'est pas exécuté. Fermeture du client.");
			System.exit(0);
		}

		// Envoie du port UDP
		sendServer("USR\r\nPORT "+ph.getUdpPort()+"\r\n");
		// on lance la thread qui écoute le serveur
		sl.start();
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

	/* méthodes concernants le serveur */
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

	public void cleanTransac(String adId) {
		main.cleanTransac(adId);
	}

	public void cleanPeers(String adId) {
		main.cleanPeers(adId);
	}

	public void close() {
		// main.close();
		sl.close();
		try {
			in.close();
			out.close();
			servSock.close();
		} catch (IOException e) {
			System.out.println("Erreur en fermant la communication.");
			e.printStackTrace();
		}
	}

}
