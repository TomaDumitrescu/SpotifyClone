package fileio.input;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public final class EpisodeInput {
    private String name;
    private Integer duration;
    private String description;

    public EpisodeInput() {
    }

    @Override
    public String toString() {
        return "EpisodeInput{"
                + "name='" + name + '\''
                + ", description='" + description + '\''
                + ", duration=" + duration
                + '}';
    }
}
