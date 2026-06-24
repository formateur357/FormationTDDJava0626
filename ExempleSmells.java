public class CommandeService {

    public double traiter(String clientId, String typeClient, double montant,
                          String email, boolean envoyerEmail, boolean sauvegarder) {
        double total = montant;

        if (typeClient.equals("VIP")) {
            total = total * 0.9;
        }

        if (typeClient.equals("PREMIUM")) {
            total = total * 0.8;
        }

        if (total > 1000) {
            total = total - 50;
        }

        if (sauvegarder) {
            System.out.println("Sauvegarde commande client " + clientId);
        }

        if (envoyerEmail) {
            System.out.println("Email envoyé à " + email);
        }

        return total;
    }
}