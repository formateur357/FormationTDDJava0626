package com.formation.tdd;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CompteBancaireTest {

    private static final double DELTA = 0.001;

    @Test
    void creerCompte_SoldeInitialZero_SoldeEgalZero() {
        // Arrange + Act
        CompteBancaire compte = new CompteBancaire(0.0);

        // Assert
        assertEquals(0.0, compte.getSolde(), DELTA);
    }

    @Test
    void creerCompte_SoldeInitialPositif_SoldeCorrect() {
        // Arrange + Act
        CompteBancaire compte = new CompteBancaire(150.0);

        // Assert
        assertEquals(150.0, compte.getSolde(), DELTA);
    }

    @Test
    void creerCompte_SoldeInitialNegatif_LeveException() {
        // Act + Assert
        assertThrows(
                IllegalArgumentException.class,
                () -> new CompteBancaire(-10.0)
        );
    }

    @Test
    void deposer_MontantPositif_AugmenteSolde() {
        // Arrange
        CompteBancaire compte = new CompteBancaire(100.0);

        // Act
        compte.deposer(50.0);

        // Assert
        assertEquals(150.0, compte.getSolde(), DELTA);
    }

    @Test
    void deposer_MontantNul_LeveException() {
        // Arrange
        CompteBancaire compte = new CompteBancaire(100.0);

        // Act + Assert
        assertThrows(
                IllegalArgumentException.class,
                () -> compte.deposer(0.0)
        );
    }

    @Test
    void deposer_MontantNegatif_LeveException() {
        // Arrange
        CompteBancaire compte = new CompteBancaire(100.0);

        // Act + Assert
        assertThrows(
                IllegalArgumentException.class,
                () -> compte.deposer(-50.0)
        );
    }

    @Test
    void retirer_MontantValide_DiminueSolde() {
        // Arrange
        CompteBancaire compte = new CompteBancaire(100.0);

        // Act
        compte.retirer(40.0);

        // Assert
        assertEquals(60.0, compte.getSolde(), DELTA);
    }

    @Test
    void retirer_MontantEgalSolde_SoldeZero() {
        // Arrange
        CompteBancaire compte = new CompteBancaire(100.0);

        // Act
        compte.retirer(100.0);

        // Assert
        assertEquals(0.0, compte.getSolde(), DELTA);
    }

    @Test
    void retirer_MontantSuperieurSolde_LeveSoldeInsuffisantException() {
        // Arrange
        CompteBancaire compte = new CompteBancaire(100.0);

        // Act + Assert
        assertThrows(
                SoldeInsuffisantException.class,
                () -> compte.retirer(150.0)
        );
    }

    @Test
    void retirer_MontantNul_LeveException() {
        // Arrange
        CompteBancaire compte = new CompteBancaire(100.0);

        // Act + Assert
        assertThrows(
                IllegalArgumentException.class,
                () -> compte.retirer(0.0)
        );
    }

    @Test
    void historique_ApresDepot_ContientOperation() {
        // Arrange
        CompteBancaire compte = new CompteBancaire(100.0);

        // Act
        compte.deposer(50.0);

        // Assert
        List<Operation> historique = compte.getHistorique();

        assertEquals(1, historique.size());

        Operation operation = historique.get(0);

        assertAll(
                () -> assertEquals(TypeOperation.DEPOT, operation.getType()),
                () -> assertEquals(50.0, operation.getMontant(), DELTA),
                () -> assertEquals(150.0, operation.getSoldeApres(), DELTA)
        );
    }

    @Test
    void historique_ApresRetrait_ContientOperation() {
        // Arrange
        CompteBancaire compte = new CompteBancaire(100.0);

        // Act
        compte.retirer(40.0);

        // Assert
        List<Operation> historique = compte.getHistorique();

        assertEquals(1, historique.size());

        Operation operation = historique.get(0);

        assertAll(
                () -> assertEquals(TypeOperation.RETRAIT, operation.getType()),
                () -> assertEquals(40.0, operation.getMontant(), DELTA),
                () -> assertEquals(60.0, operation.getSoldeApres(), DELTA)
        );
    }

    @Test
    void historique_PlusieursOperations_DansOrdreChronologique() {
        // Arrange
        CompteBancaire compte = new CompteBancaire(100.0);

        // Act
        compte.deposer(50.0);
        compte.retirer(30.0);
        compte.deposer(20.0);

        // Assert
        List<Operation> historique = compte.getHistorique();

        assertEquals(3, historique.size());

        assertAll(
                () -> assertEquals(TypeOperation.DEPOT, historique.get(0).getType()),
                () -> assertEquals(50.0, historique.get(0).getMontant(), DELTA),
                () -> assertEquals(150.0, historique.get(0).getSoldeApres(), DELTA),

                () -> assertEquals(TypeOperation.RETRAIT, historique.get(1).getType()),
                () -> assertEquals(30.0, historique.get(1).getMontant(), DELTA),
                () -> assertEquals(120.0, historique.get(1).getSoldeApres(), DELTA),

                () -> assertEquals(TypeOperation.DEPOT, historique.get(2).getType()),
                () -> assertEquals(20.0, historique.get(2).getMontant(), DELTA),
                () -> assertEquals(140.0, historique.get(2).getSoldeApres(), DELTA)
        );
    }
}