/* Objet instancié dans le serveur pour écouter les messages du clients
 * et savoir quand celui-ci veut mettre fin à la connexion */

package server;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;

import utils.Ad;
import utils.ClientInfo;


public class ClientListener extends Thread {
	private DataInputStream in;
	private Socket sock;
	private AdServer mainServ;
	private ClientInfo ci;

	public ClientListener(Socket sock, AdServer as, String id) {
		try {
			this.sock = sock;
			this.mainServ = as;
			this.in = new DataInputStream(sock.getInputStream());
			this.ci = new ClientInfo(id, sock.getInetAddress(), 0);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void run() {
		boolean end = false;
		while(!end) {
			try {
				String[] strs = in.readUTF().split("\r\n");
				for(int i = 0; i < strs.length && end == false; i++) {
					switch (strs[i].trim()) {
					case "USR" :
						// Il n'y a qu'un message possible : USR \r\n PORT port \r\n
						i++;
						ci.setPort(Integer.parseInt(strs[i].trim().substring(5)));
						break;
					case "AD" :
						if(ci.getPort() == 0) {
							System.out.println("Le client n'a pas donné de port UDP.");
							mainServ.sendErr(1, ci.getId());
						}
						i++;
						switch (strs[i].trim()) {
						case "ADD" :
							// Ajout d'une annonce à la liste
							i++;
							mainServ.addAd(new Ad(strs[i].trim().substring(4), ci));
							break;
						case "DEL" :
							// Suppression d'une annonce de la liste
							i++;
							mainServ.delAd(strs[i].trim().substring(3), ci.getId());
							break;
						default :
							// message ignoré
						}
						break;
					case "DISCONNECT":
						System.out.println("Deconnexion du client "+
								sock.getInetAddress()+":"+sock.getPort());
						// suppression des annonces
						mainServ.delClient(ci.getId());
						end = true;
						break;
					default :
						// message ignoré
					}
				}
			} catch (IOException e) {
				System.out.println("Deconnexion inatendue du client. Suppression des annonces "+
						sock.getInetAddress()+":"+sock.getPort());
				mainServ.delClient(ci.getId());
				end = true;
				break;
			} catch (NullPointerException e) {
				System.out.println("Deconnexion inatendue du client. Suppression des annonces "+
						sock.getInetAddress()+":"+sock.getPort());
				mainServ.delClient(ci.getId());
				end = true;
				break;
			}
		}
		try {
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
