import java.util.Objects;

public class Article {

    private final String reference;
    private final int quantite;
    private final double prixUnitaire;

    public Article(String reference, int quantite, double prixUnitaire) {
        if (reference == null || reference.isBlank()) {
            throw new IllegalArgumentException("La référence article est obligatoire");
        }

        if (quantite <= 0) {
            throw new IllegalArgumentException("La quantité doit être positive");
        }

        if (prixUnitaire < 0) {
            throw new IllegalArgumentException("Le prix unitaire ne peut pas être négatif");
        }

        this.reference = reference;
        this.quantite = quantite;
        this.prixUnitaire = prixUnitaire;
    }

    public String getReference() {
        return reference;
    }

    public int getQuantite() {
        return quantite;
    }

    public double getPrixUnitaire() {
        return prixUnitaire;
    }

    public double calculerTotal() {
        return quantite * prixUnitaire;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Panier that)) return false;

        return quantite == that.quantite
            && Double.compare(prixUnitaire, that.prixUnitaire) == 0
            && Objects.equals(reference, that.reference);
    }

    @Override
    public int hashCode() {
        return Objects.hash(reference, quantite, prixUnitaire);
    }
}