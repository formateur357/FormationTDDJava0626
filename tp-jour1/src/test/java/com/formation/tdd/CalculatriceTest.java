package com.formation.tdd;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class CalculatriceTest {
	
	@Test
	void additionner_DeuxEntiers_RetourneLaSomme() {
		// Arrange
		Calculatrice calculatrice = new Calculatrice();
		
		// Act
		int resultat = calculatrice.additionner(2, 3);
		
		// Assert
		assertEquals(5, resultat);
	}
	
	@Test
	void soustraire_DeuxEntiersPositifs_RetourneLaDifference() {
		// Arrange
		Calculatrice calculatrice = new Calculatrice();
		
		// Act
		int resultat = calculatrice.soustraire(5, 3);
		
		// Assert
		assertEquals(2, resultat);
	}
	

}
