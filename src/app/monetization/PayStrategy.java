package app.monetization;

import app.user.Artist;

public interface PayStrategy {
    /**
     * Encapsulates a strategy for payment to an artist
     *
     * @param price the product price
     * @param seller the artist
     * @param productType the product type
     */
    void pay(Double price, Artist seller, String productType);
}
