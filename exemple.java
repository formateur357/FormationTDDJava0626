@Test
void retirer_SoldeInsuffisant_LeveException() {
    CompteBancaire compte = new CompteBanquaire(100.0);

    assertThrows(
        SoldeInsuffisantException.class, () -> compte.retirer(200.0)
    );
}

additionner(int a, int b)

additionner_DeuxEntiers_RetourneLaSomme()


// Exemple code trop couplé
public class FactureService {

    public double calculerTotal( String clientId) {
        Connection connection = DriverManager.getConnection("jdbc:mysql://prod...");
        // requetes SQL
        // logique métier
        // ecriture fichier
        // envoi email
        return total;
    }
}

// Exemple de test TDD

@Test
void calculerTVA_MontantHT100TauxTVA20_Retourne20() {
    calculateurTVA calculateur = new calculateurTVA();

    double tva = calculateur.calculer(100.0, 0.20);

    assertEquals(20.0, tva, 0.001);
}

// ❌ Trop d'assertions dans un seul test
@Test
void testCompteBancaire() {
    CompteBancaire c = new CompteBancaire();
    assertEquals(0, c.getSolde());
    c.deposer(100);
    assertEquals(100, c.getSolde());
    c.retirer(50);
    assertEquals(50, c.getSolde());
    // Si la 2e assertion échoue, les 3e et 4e ne sont pas exécutées
}

class CompteBancaire {
    private CompteBancaire compte;

    @BeforeEach
    void setup() {
        compte = new CompteBancaire(100.0);
    }

    @Test
    void retirer_MontantValide_Diminuesolde() {
        // Act 
        compte.retirer(50.0); 

        // Assert
        assertEquals(50.0, compte.getSolde(), 0.001);
        
    }

    @Test
    void deposer_MontantPositif_AugmenteSolde() {
        // Act 
        compte.deposer(100.0); 

        // Assert
        assertEquals(200.0, compte.getSolde(), 0.001);
        
    }
}

@Test
void creerCompte_SansSoldeInitial_SoldeEgalZero() {
    // Arrange
    CompteBancaire c = new CompteBancaire();

    // Act & Assert
    assertEquals(0.0, c.getSolde(), 0.001);
    
}

@Test
void deposer_MontantPositif_AugmenteSolde() {
    // Arrange
    CompteBancaire c = new CompteBancaire();

    // Act 
    c.deposer(100.0); 

    // Assert
    assertEquals(100.0, c.getSolde(), 0.001);
    
}

@Test
void retirer_MontantValide_Diminuesolde() {
    // Arrange
    CompteBancaire c = new CompteBancaire(100.0);

    // Act 
    c.retirer(50.0); 

    // Assert
    assertEquals(50.0, c.getSolde(), 0.001);
    
}