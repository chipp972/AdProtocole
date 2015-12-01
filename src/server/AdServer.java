package server;

import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;
import java.util.LinkedList;

import utils.Ad;

public class AdServer extends Thread {
	/* Infos partagées par les threads server */
	public static final int port = 1027;
	private static LinkedList<Ad> adList = new LinkedList<Ad>();
	private static LinkedList<Ad> removedAd = new LinkedList<Ad>();
	private static int numClient = 0, numAd = 0;
	private static ServerSocket srvs;

	/* Paramètres de chaque thread service */
	private Socket threadSocket;
	private DataOutputStream out;
	private ClientListener cl;
	private int currAdNb, currRmNb;
	private static PrintWriter logWriter;

	public AdServer(Socket s) throws IOException {
		this.threadSocket = s;
		this.out = new DataOutputStream(threadSocket.getOutputStream());
		this.cl = new ClientListener(s, this, ""+numClient++);
	}

	synchronized public void sendAds(int start) {
		int t = adList.size();
		try {
			for(int i = start; i < t; i++) {
					out.writeUTF(adList.get(i).toMessage());
					out.flush();
			}
		} catch (IOException e) {
			printLog("Erreur Client : Deconnexion inattendue.");
		}
		currAdNb = adList.size();
	}

	synchronized public void sendRmAds(int start) {
		int t = removedAd.size();
		try {
			for(int i = start; i < t; i++) {
					out.writeUTF(removedAd.get(i).toDelMessage());
					out.flush();
			}
		} catch (IOException e) {
			printLog("Erreur Client : Deconnexion inattendue.");
		}
		currAdNb = adList.size();
		currRmNb = removedAd.size();
	}

	synchronized public void addAd(Ad a) {
		a.setAdId(Integer.toString(numAd));
		numAd++;
		adList.add(a);
		printLog("Client "+a.getClientInfo().getId()+" added "+a.toClientOutput());
	}

	synchronized public void delAd(String id, String clientId) {
		printLog("Client "+clientId+" removed ad "+id);
		for(int i = 0; i < adList.size(); i++) {
			if(adList.get(i).getAdId().equals(id)) {
				if (!clientId.equals(adList.get(i).getClientInfo().getId()))
					sendErr(2, clientId);
				else
					removedAd.add(adList.remove(i));
				return;
			}
		}
		sendErr(1, clientId);
	}

	synchronized public void delClient(String id) {
		Iterator<Ad> it = adList.iterator();
		Ad a;
		printLog("Client "+id+" just disconnected");
		while(it.hasNext()) {
			a = it.next();
			if(a.getClientInfo().getId().equals(id)) {
				removedAd.add(a);
				it.remove();
			}
		}
	}

	public void sendErr(int id, String clientId) {
		try {
			out.writeUTF("SYS\r\nERROR 00"+id+"\r\n");
			out.flush();
		} catch (IOException e) {
			printLog("Erreur Client "+clientId+" : 00"+id);
		}
	}

	public static void printLog(String s) {
		logWriter.println(s);
		logWriter.flush();
	}

	/* Thread qui gère un client */
	public void run() {
		try {
			cl.start(); // Thread qui écoute le client et interprète ses messages
			sendAds(0);
			currRmNb = removedAd.size();
			do {

				if(currAdNb < adList.size())
					sendAds(currAdNb);
				else if(currAdNb > adList.size())
					sendRmAds(currRmNb);

			} while(cl.isAlive()); // Tant que le client n'a pas indiqué une deconnexion
			out.close();
			threadSocket.close();
		} catch (IOException e) {
			System.out.println("Exception inopinee...");
		}
	}


	public static void main(String[] args) {
		try {
			logWriter = new PrintWriter(new BufferedWriter(new FileWriter(
				"log"+File.separator+System.currentTimeMillis()+"_log.txt")));
		} catch (IOException e) {
			(new File("log")).mkdir();
			try {
				logWriter = new PrintWriter(new BufferedWriter(new FileWriter(
				"log"+File.separator+System.currentTimeMillis()+"_log.txt")));
			} catch (IOException e2) {
				System.out.println("Fatal Error at log folder creation");
				System.exit(0);
			}
		}
		try {
			srvs = new ServerSocket(port);
			printLog("Serveur en attente de client sur "+srvs.toString());
			while(true) {
				AdServer cs;
				try {
					Socket s = srvs.accept();
					cs = new AdServer(s);
					cs.start();
					printLog("Serveur connecte au client "+
										s.getInetAddress()+ "(" + s.getPort() + ")");
				} catch (IOException e) {
					System.out.println("Erreur durant une connexion avec un client.");
				}
			}

		} catch (IOException e) {
			System.out.println("Un serveur est deja execute sur cette adresse et ce port.");
			System.exit(0);
		}
		logWriter.close();
	}

}
