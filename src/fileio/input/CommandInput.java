package fileio.input;

import java.util.ArrayList;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public final class CommandInput {
    private String command;
    private String username;
    private Integer timestamp;
    private String type;
    private FiltersInput filters;
    private Integer itemNumber;
    private Integer repeatMode;
    private Integer playlistId;
    private String playlistName;
    private Integer seed;
    private String recommendationType;
    private int age;
    private String city;
    private ArrayList<EpisodeInput> episodes;
    private String name;
    private Integer price;
    private String date;
    private String description;
    private ArrayList<SongInput> songs;
    private Integer releaseYear;
    private String nextPage;

    public CommandInput() {
    }

    @Override
    public String toString() {
        return "CommandInput{"
                + "command='" + command + '\''
                + ", username='" + username + '\''
                + ", timestamp=" + timestamp
                + ", type='" + type + '\''
                + ", filters=" + filters
                + ", itemNumber=" + itemNumber
                + ", repeatMode=" + repeatMode
                + ", playlistId=" + playlistId
                + ", playlistName='" + playlistName + '\''
                + ", seed=" + seed
                + '}';
    }
}
