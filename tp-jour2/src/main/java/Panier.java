import java.util.List;

public class Panier {

    private final String emailClient;
    private final List<Article> articles;

    public Panier(String emailClient, List<Article> articles) {
        if (emailClient == null || emailClient.isBlank()) {
            throw new IllegalArgumentException("L'email client est obligatoire");
        }

        if (articles == null || articles.isEmpty()) {
            throw new IllegalArgumentException("Le panier doit contenir au moins un article");
        }

        this.emailClient = emailClient;
        this.articles = List.copyOf(articles);
    }

    public String getEmailClient() {
        return emailClient;
    }

    public List<Article> getArticles() {
        return articles;
    }

    public double calculerTotal() {
        return articles.stream()
            .mapToDouble(Article::calculerTotal)
            .sum();
    }
}