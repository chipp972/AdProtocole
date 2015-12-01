package client;

import java.lang.Enum;

public enum Commande {

	AD ("Ajout d'une annonce : AD <msg>"),
	RM ("Supression d'une annonce : RM <id-ad>"),
	LS ("Lister toutes les annonces : LS"),

	SM ("Envoyer un message a un client : SM <id-ad> <msg>"),
	RP ("Repondre a un pair : RP <id-pair> <msg>"),
	LP ("Lister les paires : LP"),
	LM ("Lister les messages : LM"),

	ST ("Envoyer une demande de transaction : ST <id-ad>"),
	LT ("Lister les transactions : LT"),
	OK ("Envoyer une reponse positive a une transaction : OK <id-tr>"),
	KO ("Envoyer une reponse negative a une transaction : KO <id-tr>"),

	HE ("Afficher l'aide : HE"),
	EX ("Deconnexion : EX");

	private String cm = "";
	private static final int size = 2;

	Commande(String cm) {
		this.cm = cm;
	}

	public static int getSize() {
		return size;
	}

	public final String getValue() {
		return cm;
	}

	public final String toString() {
		return cm;
	}
}
