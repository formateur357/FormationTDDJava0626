import java.time.LocalDateTime;

public class Commande {

    private Long id;
    private final Panier panier;
    private final String transactionId;
    private final StatutCommande statut;
    private final LocalDateTime dateCreation;

    public Commande(Panier panier, String transactionId) {
        if (panier == null) {
            throw new IllegalArgumentException("Le panier est obligatoire");
        }

        if (transactionId == null || transactionId.isBlank()) {
            throw new IllegalArgumentException("L'identifiant de transaction est obligatoire");
        }

        this.panier = panier;
        this.transactionId = transactionId;
        this.statut = StatutCommande.PAYEE;
        this.dateCreation = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Panier getPanier() {
        return panier;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public StatutCommande getStatut() {
        return statut;
    }

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    public double getMontantTotal() {
        return panier.calculerTotal();
    }

    public String getEmailClient() {
        return panier.getEmailClient();
    }

    public void setId(Long id) {
        this.id = id;
    }
}