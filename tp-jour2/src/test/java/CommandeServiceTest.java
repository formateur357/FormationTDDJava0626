package com.formation.tdd.jour2.ex23;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommandeServiceTest {

    @Mock
    private StockService stockService;

    @Mock
    private PaiementService paiementService;

    @Mock
    private EmailService emailService;

    @Mock
    private CommandeRepository commandeRepository;

    private CommandeService commandeService;

    @BeforeEach
    void setUp() {
        commandeService = new CommandeService(
            stockService,
            paiementService,
            emailService,
            commandeRepository
        );
    }

    @Test
    void passerCommande_Valide_ExecuteToutesLesEtapesDansLeBonOrdre() {
        // Arrange
        Article article = new Article("ART-001", 2, 25.0);
        Panier panier = new Panier("client@test.fr", List.of(article));

        when(stockService.verifierDisponibilite(article))
            .thenReturn(true);

        when(paiementService.debiter("CARD-123", 50.0))
            .thenReturn("TX-001");

        when(commandeRepository.save(any(Commande.class)))
            .thenAnswer(invocation -> {
                Commande commande = invocation.getArgument(0);
                commande.setId(1L);
                return commande;
            });

        // Act
        Commande commande = commandeService.passerCommande(panier, "CARD-123");

        // Assert
        assertNotNull(commande);
        assertEquals(1L, commande.getId());
        assertEquals("TX-001", commande.getTransactionId());
        assertEquals(StatutCommande.PAYEE, commande.getStatut());

        InOrder inOrder = inOrder(
            stockService,
            paiementService,
            commandeRepository,
            emailService
        );

        inOrder.verify(stockService).verifierDisponibilite(article);
        inOrder.verify(stockService).reserver(article);
        inOrder.verify(paiementService).debiter("CARD-123", 50.0);
        inOrder.verify(commandeRepository).save(any(Commande.class));
        inOrder.verify(emailService)
            .envoyerConfirmation(eq("client@test.fr"), any(Commande.class));
    }

    @Test
    void passerCommande_StockInsuffisant_NePayePas_NeSauvegardePas_NEnvoiePasEmail() {
        // Arrange
        Article article = new Article("ART-001", 2, 25.0);
        Panier panier = new Panier("client@test.fr", List.of(article));

        when(stockService.verifierDisponibilite(article))
            .thenReturn(false);

        // Act + Assert
        assertThrows(
            StockInsuffisantException.class,
            () -> commandeService.passerCommande(panier, "CARD-123")
        );

        verify(stockService).verifierDisponibilite(article);
        verify(stockService, never()).reserver(any());
        verify(stockService, never()).liberer(any());

        verifyNoInteractions(paiementService);
        verifyNoInteractions(commandeRepository);
        verifyNoInteractions(emailService);
    }

    @Test
    void passerCommande_PaiementEchoue_LibereLeStock_NeSauvegardePas_NEnvoiePasEmail() {
        // Arrange
        Article article = new Article("ART-001", 1, 100.0);
        Panier panier = new Panier("client@test.fr", List.of(article));

        when(stockService.verifierDisponibilite(article))
            .thenReturn(true);

        when(paiementService.debiter("CARD-123", 100.0))
            .thenThrow(new PaiementException("Carte refusée"));

        // Act + Assert
        PaiementException exception = assertThrows(
            PaiementException.class,
            () -> commandeService.passerCommande(panier, "CARD-123")
        );

        assertEquals("Carte refusée", exception.getMessage());

        verify(stockService).verifierDisponibilite(article);
        verify(stockService).reserver(article);
        verify(stockService).liberer(article);

        verify(paiementService).debiter("CARD-123", 100.0);

        verifyNoInteractions(commandeRepository);
        verifyNoInteractions(emailService);
    }

    @Test
    void passerCommande_DeuxArticles_VerifieEtReserveChaqueArticle() {
        // Arrange
        Article clavier = new Article("CLAVIER", 1, 40.0);
        Article souris = new Article("SOURIS", 2, 15.0);

        Panier panier = new Panier(
            "client@test.fr",
            List.of(clavier, souris)
        );

        when(stockService.verifierDisponibilite(any(Article.class)))
            .thenReturn(true);

        when(paiementService.debiter("CARD-123", 70.0))
            .thenReturn("TX-002");

        when(commandeRepository.save(any(Commande.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        commandeService.passerCommande(panier, "CARD-123");

        // Assert
        verify(stockService, times(2)).verifierDisponibilite(any(Article.class));
        verify(stockService).verifierDisponibilite(clavier);
        verify(stockService).verifierDisponibilite(souris);

        verify(stockService, times(2)).reserver(any(Article.class));
        verify(stockService).reserver(clavier);
        verify(stockService).reserver(souris);

        verify(paiementService).debiter("CARD-123", 70.0);
        verify(commandeRepository).save(any(Commande.class));
        verify(emailService).envoyerConfirmation(eq("client@test.fr"), any(Commande.class));
    }

    @Test
    void passerCommande_DeuxArticles_PremierDisponibleDeuxiemeIndisponible_StoppeAvantReservation() {
        // Arrange
        Article clavier = new Article("CLAVIER", 1, 40.0);
        Article souris = new Article("SOURIS", 2, 15.0);

        Panier panier = new Panier(
            "client@test.fr",
            List.of(clavier, souris)
        );

        when(stockService.verifierDisponibilite(clavier))
            .thenReturn(true);

        when(stockService.verifierDisponibilite(souris))
            .thenReturn(false);

        // Act + Assert
        assertThrows(
            StockInsuffisantException.class,
            () -> commandeService.passerCommande(panier, "CARD-123")
        );

        verify(stockService).verifierDisponibilite(clavier);
        verify(stockService).verifierDisponibilite(souris);

        verify(stockService, never()).reserver(any());
        verify(stockService, never()).liberer(any());

        verifyNoInteractions(paiementService);
        verifyNoInteractions(commandeRepository);
        verifyNoInteractions(emailService);
    }

    @Test
    void passerCommande_SauvegardeCommandeAvecBonPanierEtBonneTransaction() {
        // Arrange
        Article article = new Article("ART-001", 3, 10.0);
        Panier panier = new Panier("client@test.fr", List.of(article));

        when(stockService.verifierDisponibilite(article))
            .thenReturn(true);

        when(paiementService.debiter("CARD-123", 30.0))
            .thenReturn("TX-999");

        when(commandeRepository.save(any(Commande.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        commandeService.passerCommande(panier, "CARD-123");

        // Assert
        verify(commandeRepository).save(argThat(commande ->
            commande.getPanier() == panier
                && commande.getTransactionId().equals("TX-999")
                && commande.getStatut() == StatutCommande.PAYEE
                && commande.getMontantTotal() == 30.0
        ));
    }
}