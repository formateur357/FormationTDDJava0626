
public interface StockService {
    boolean verifierDisponibilite(Article article);

    void reserver(Article article);

    void liberer(Article article);
}
