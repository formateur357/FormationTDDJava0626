package com.formation.tdd;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class CompteBancaireParametreTest {

    private static final double DELTA = 0.001;

    private CompteBancaire compte;

    @BeforeEach
    void setUp() {
        compte = new CompteBancaire(100.0);
    }

    @ParameterizedTest(name = "deposer({0}) doit lever IllegalArgumentException")
    @ValueSource(doubles = {-1.0, -100.0, 0.0})
    void deposer_MontantInvalide_LeveException(double montantInvalide) {
        // Act + Assert
        assertThrows(
                IllegalArgumentException.class,
                () -> compte.deposer(montantInvalide)
        );
    }

    @ParameterizedTest(name = "{1} de {2} depuis {0} donne un solde de {3}")
    @MethodSource("operationsPourHistorique")
    void operation_Valide_AjouteUneLigneDansHistorique(
            double soldeInitial,
            TypeOperation typeOperation,
            double montant,
            double soldeAttendu
    ) {
        // Arrange
        CompteBancaire compte = new CompteBancaire(soldeInitial);

        // Act
        if (typeOperation == TypeOperation.DEPOT) {
            compte.deposer(montant);
        } else {
            compte.retirer(montant);
        }

        // Assert
        List<Operation> historique = compte.getHistorique();

        assertEquals(1, historique.size());

        Operation operation = historique.get(0);

        assertAll(
                () -> assertEquals(typeOperation, operation.getType()),
                () -> assertEquals(montant, operation.getMontant(), DELTA),
                () -> assertEquals(soldeAttendu, operation.getSoldeApres(), DELTA),
                () -> assertEquals(soldeAttendu, compte.getSolde(), DELTA)
        );
    }

    static Stream<Arguments> operationsPourHistorique() {
        return Stream.of(
                Arguments.of(100.0, TypeOperation.DEPOT, 50.0, 150.0),
                Arguments.of(100.0, TypeOperation.RETRAIT, 40.0, 60.0),
                Arguments.of(0.0, TypeOperation.DEPOT, 25.0, 25.0),
                Arguments.of(200.0, TypeOperation.RETRAIT, 200.0, 0.0)
        );
    }
}