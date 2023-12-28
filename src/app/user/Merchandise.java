package app.user;

import lombok.Getter;
import lombok.Setter;

/**
 * The type Merchandise.
 */
@Getter
@Setter
public class Merchandise {
    private String name;
    private String description;
    private int price;

    /**
     * Instantiates a new Merchandise.
     *
     * @param name        the name
     * @param description the description
     * @param price       the price
     */
    public Merchandise(final String name, final String description, final int price) {
        this.name = name;
        this.description = description;
        this.price = price;
    }
}
