package app.user;

import app.pages.Page;

import lombok.Getter;
import lombok.Setter;

/**
 * The type Content creator.
 */
@Getter
@Setter
public abstract class ContentCreator extends UserAbstract {
    private String description;
    private Page page;

    /**
     * Instantiates a new Content creator.
     *
     * @param username the username
     * @param age      the age
     * @param city     the city
     */
    public ContentCreator(final String username, final int age, final String city) {
        super(username, age, city);
    }
}
