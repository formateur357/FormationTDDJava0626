package com.formation.tdd;

public class SoldeInsuffisantException extends RuntimeException {
	
	public SoldeInsuffisantException(double solde, double montantDemande) {
		super("Solde Insuffisant: " + solde + " disponible, " + montantDemande + " demandé");
	}
}
