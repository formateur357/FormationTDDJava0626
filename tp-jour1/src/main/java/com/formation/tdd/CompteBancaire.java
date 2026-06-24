package com.formation.tdd;

import java.util.ArrayList;
import java.util.List;

public class CompteBancaire {

    private double solde;
    private final List<Operation> historique = new ArrayList<>();

    public CompteBancaire() {
        this(0.0);
    }

    public CompteBancaire(double soldeInitial) {
        if (soldeInitial < 0) {
            throw new IllegalArgumentException(
                    "Le solde initial ne peut pas être négatif : " + soldeInitial
            );
        }

        this.solde = soldeInitial;
    }

    public double getSolde() {
        return solde;
    }

    public void deposer(double montant) {
        verifierMontantStrictementPositif(montant);

        solde += montant;

        historique.add(new Operation(
                TypeOperation.DEPOT,
                montant,
                solde
        ));
    }

    public void retirer(double montant) {
        verifierMontantStrictementPositif(montant);

        if (montant > solde) {
            throw new SoldeInsuffisantException(solde, montant);
        }

        solde -= montant;

        historique.add(new Operation(
                TypeOperation.RETRAIT,
                montant,
                solde
        ));
    }

    public List<Operation> getHistorique() {
        return List.copyOf(historique);
    }

    private void verifierMontantStrictementPositif(double montant) {
        if (montant <= 0) {
            throw new IllegalArgumentException(
                    "Le montant doit être strictement positif : " + montant
            );
        }
    }
}