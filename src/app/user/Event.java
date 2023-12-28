package app.user;

import lombok.Getter;
import lombok.Setter;

/**
 * The type Event.
 */
@Getter
@Setter
public class Event {
    private String name;
    private String description;
    private String date;

    /**
     * Instantiates a new Event.
     *
     * @param name        the name
     * @param description the description
     * @param date        the date
     */
    public Event(final String name, final String description, final String date) {
        this.name = name;
        this.description = description;
        this.date = date;
    }
}
