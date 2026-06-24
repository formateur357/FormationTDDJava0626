package com.formation.tdd;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CalculateurTVAParametreTest {

    private static final double DELTA = 0.001;

    private final CalculateurTVA calc = new CalculateurTVA();

    @ParameterizedTest(name = "TVA de {0} avec taux {1} = {2}")
    @CsvSource({
            "100.0, 0.20, 20.0",
            "200.0, 0.10, 20.0",
            "500.0, 0.055, 27.5"
    })
    void calculerTVA_MontantEtTaux_RetourneTVAAttendue(
            double montantHT,
            double tauxTVA,
            double tvaAttendue
    ) {
        // Act
        double resultat = calc.calculer(montantHT, tauxTVA);

        // Assert
        assertEquals(tvaAttendue, resultat, DELTA);
    }
}