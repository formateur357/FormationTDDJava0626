package com.formation.tdd;

public class Validateur {

    public void validerAge(int age) {
        if (age < 0 || age > 150) {
            throw new IllegalArgumentException("Age invalide : " + age);
        }
    }

    public void validerEmail(String email) {
        if (email == null) {
            throw new IllegalArgumentException("Email invalide : null");
        }

        if (email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email invalide : email vide");
        }

        if (!email.contains("@")) {
            throw new IllegalArgumentException("Email invalide : " + email);
        }
    }

    public void validerMontant(double montant) {
        if (montant < 0) {
            throw new IllegalArgumentException("Montant invalide : " + montant);
        }
    }
}