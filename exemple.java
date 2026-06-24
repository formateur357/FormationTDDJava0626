@Test
void retirer_SoldeInsuffisant_LeveException() {
    CompteBancaire compte = new CompteBanquaire(100.0);

    assertThrows(
        SoldeInsuffisantException.class, () -> compte.retirer(200.0)
    );
}

additionner(int a, int b)

additionner_DeuxEntiers_RetourneLaSomme()