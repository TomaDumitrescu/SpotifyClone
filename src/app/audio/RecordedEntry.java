package app.audio;

import lombok.Getter;
import lombok.Setter;

import java.util.Objects;


@Getter
@Setter
public class RecordedEntry extends LibraryEntry {
    private String creator;
    private String type;
    private String genre;
    double price;

    /**
     * Constructor for recorded entry
     *
     * @param name the name of the audio product
     * @param creator the name of the audio object creator
     */
    public RecordedEntry(final String name, final String creator,
                         final String type) {
        super(name);
        this.creator = creator;
        this.type = type;
        price = 0;
    }

    /**
     * Verifies if this is equal with an object
     *
     * @param o the object
     * @return the proposition truth value
     */
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        RecordedEntry cmp = (RecordedEntry) o;
        return getName().equalsIgnoreCase(cmp.getName())
                && getCreator().equalsIgnoreCase(cmp.getCreator());
    }

    /**
     * Creates a hash based on name and creator
     * @return the int hash
     */
    @Override
    public int hashCode() {
        return Objects.hash(getName(), getCreator(), getType());
    }
}
