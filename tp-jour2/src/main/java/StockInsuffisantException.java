
public class StockInsuffisantException extends RuntimeException {
    public StockInsuffisantException(String referenceArticle) {
        super("Stock insuffisant pour l'article " + referenceArticle);
    } 
}
