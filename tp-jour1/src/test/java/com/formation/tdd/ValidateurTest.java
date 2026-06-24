package com.formation.tdd;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ValidateurTest {

    private Validateur validateur;

    @BeforeEach
    void setUp() {
        validateur = new Validateur();
    }

    @Test
    void validerAge_AgeNegatif_LeveIllegalArgumentException() {
        // Arrange
        int ageInvalide = -1;

        // Act + Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> validateur.validerAge(ageInvalide)
        );

        assertTrue(exception.getMessage().contains("-1"));
    }

    @Test
    void validerAge_AgeSuperieurA150_LeveIllegalArgumentException() {
        // Arrange
        int ageInvalide = 151;

        // Act + Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> validateur.validerAge(ageInvalide)
        );

        assertTrue(exception.getMessage().contains("151"));
    }

    @Test
    void validerAge_AgeValide_PasException() {
        // Arrange
        int ageValide = 25;

        // Act + Assert
        assertDoesNotThrow(() -> validateur.validerAge(ageValide));
    }

    @Test
    void validerEmail_EmailNull_LeveException() {
        // Arrange
        String emailInvalide = null;

        // Act + Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> validateur.validerEmail(emailInvalide)
        );

        assertTrue(exception.getMessage().contains("null"));
    }

    @Test
    void validerEmail_EmailVide_LeveException() {
        // Arrange
        String emailInvalide = "";

        // Act + Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> validateur.validerEmail(emailInvalide)
        );

        assertTrue(exception.getMessage().contains("vide"));
    }

    @Test
    void validerEmail_EmailSansArobase_LeveException() {
        // Arrange
        String emailInvalide = "alice.email.com";

        // Act + Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> validateur.validerEmail(emailInvalide)
        );

        assertTrue(exception.getMessage().contains("alice.email.com"));
    }

    @Test
    void validerEmail_EmailValide_PasException() {
        // Arrange
        String emailValide = "alice@email.com";

        // Act + Assert
        assertDoesNotThrow(() -> validateur.validerEmail(emailValide));
    }

    @Test
    void validerMontant_MontantNegatif_LeveExceptionAvecMessage() {
        // Arrange
        double montantInvalide = -50.0;

        // Act + Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> validateur.validerMontant(montantInvalide)
        );

        assertTrue(exception.getMessage().contains("-50.0"));
    }

    @Test
    void validerMontant_MontantZero_PasException() {
        // Arrange
        double montantValide = 0.0;

        // Act + Assert
        assertDoesNotThrow(() -> validateur.validerMontant(montantValide));
    }

    @Test
    void validerMontant_MontantPositif_PasException() {
        // Arrange
        double montantValide = 100.0;

        // Act + Assert
        assertDoesNotThrow(() -> validateur.validerMontant(montantValide));
    }
}