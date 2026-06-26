public class CommandeService {

    private final StockService stockService;
    private final PaiementService paiementService;
    private final EmailService emailService;
    private final CommandeRepository commandeRepository;

    public CommandeService(StockService stockService,
                           PaiementService paiementService,
                           EmailService emailService,
                           CommandeRepository commandeRepository) {
        this.stockService = stockService;
        this.paiementService = paiementService;
        this.emailService = emailService;
        this.commandeRepository = commandeRepository;
    }

    public Commande passerCommande(Panier panier, String carteId) {
        verifierStockDisponible(panier);
        reserverStock(panier);

        try {
            String transactionId = paiementService.debiter(
                carteId,
                panier.calculerTotal()
            );

            Commande commande = new Commande(panier, transactionId);

            Commande commandeSauvegardee = commandeRepository.save(commande);

            emailService.envoyerConfirmation(
                panier.getEmailClient(),
                commandeSauvegardee
            );

            return commandeSauvegardee;

        } catch (RuntimeException exception) {
            libererStock(panier);
            throw exception;
        }
    }

    private void verifierStockDisponible(Panier panier) {
        for (Article article : panier.getArticles()) {
            boolean disponible = stockService.verifierDisponibilite(article);

            if (!disponible) {
                throw new StockInsuffisantException(article.getReference());
            }
        }
    }

    private void reserverStock(Panier panier) {
        for (Article article : panier.getArticles()) {
            stockService.reserver(article);
        }
    }

    private void libererStock(Panier panier) {
        for (Article article : panier.getArticles()) {
            stockService.liberer(article);
        }
    }
}