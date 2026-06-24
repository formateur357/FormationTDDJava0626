package com.formation.tdd;

public class Operation {
    private TypeOperation type; // DEPOT ou RETRAIT
    private double montant;
    private double soldeApres;
    
    public Operation(TypeOperation type, double montant, double soldeApres) {
    	this.type = type;
    	this.montant = montant;
    	this.soldeApres = soldeApres;
    }
    
    public TypeOperation getType() {
    	return type;
    }
    
    
    public double getMontant() {
    	return montant;
    }
    
    
    public double getSoldeApres() {
    	return soldeApres;
    }
}