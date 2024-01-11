package app.user.wrap;

import app.audio.RecordedEntry;
import app.user.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Getter
@Setter
public final class ArtistWrap implements Wrap {
    private static ArtistWrap instance = null;
    private List<User> users;
    private String username;
    private final int topReference = 5;

    private ArtistWrap() {
    }

    /**
     * Lazy instantiation
     *
     * @return Singleton instance
     */
    public static ArtistWrap getInstance() {
        if (instance == null) {
            instance = new ArtistWrap();
        }
        return instance;
    }

    /**
     * Adding in topSongs all user songs from a given artist
     *
     * @param currentUserRec the recorded entries for the user in iteration
     * @param topSongs the songs hashmap
     * @return the total number of listens from the user in iteration
     */
    public int insertSongs(final HashMap<RecordedEntry, Integer> currentUserRec,
                           final LinkedHashMap<RecordedEntry, Integer> topSongs) {
        int currentListens = 0;

        for (Map.Entry<RecordedEntry, Integer> rec: currentUserRec.entrySet()) {
            RecordedEntry recorded = rec.getKey();
            if (recorded.getType().equalsIgnoreCase("song")
                && recorded.getCreator().equalsIgnoreCase(username)) {
                currentListens += rec.getValue();
                topSongs.put(rec.getKey(),
                        topSongs.getOrDefault(rec.getKey(), 0) + rec.getValue());
            }
        }

        return currentListens;
    }

    /**
     * Adding in topAlbums all user albums from a given artist
     *
     * @param currentUserRec the recorded entries for the user in iteration
     * @param topAlbums the albums hashmap
     */
    public void insertAlbums(final HashMap<RecordedEntry, Integer> currentUserRec,
                             final LinkedHashMap<RecordedEntry, Integer> topAlbums) {
        for (Map.Entry<RecordedEntry, Integer> rec: currentUserRec.entrySet()) {
            RecordedEntry recorded = rec.getKey();
            if (recorded.getType().equalsIgnoreCase("album")
                && recorded.getCreator().equalsIgnoreCase(username)) {
                topAlbums.put(rec.getKey(),
                        topAlbums.getOrDefault(rec.getKey(), 0) + rec.getValue());
            }
        }
    }

    /**
     * Sorts the data using default methods from Wrap interface
     *
     * @param topAlbums the albums from that artist
     * @param topSongs the songs from the albums
     * @param topUsers the users that listened the artist
     */
    public void sortData(final LinkedHashMap<RecordedEntry, Integer> topAlbums,
                         final LinkedHashMap<RecordedEntry, Integer> topSongs,
                         final LinkedHashMap<String, Integer> topUsers) {

        sortRecorded(topAlbums, topReference);
        sortRecorded(topSongs, topReference);
        sortStringIntMaps(topUsers, topReference);
    }

    /**
     * Generates statistics for the artist
     *
     * @return the object node
     */
    @Override
    public ObjectNode generateStatistics() {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode result = objectMapper.createObjectNode();

        int listeners = 0, currentListens;

        LinkedHashMap<RecordedEntry, Integer> topSongs = new LinkedHashMap<>();
        LinkedHashMap<RecordedEntry, Integer> topAlbums = new LinkedHashMap<>();
        LinkedHashMap<String, Integer> topUsers = new LinkedHashMap<>();

        HashMap<RecordedEntry, Integer> currentUserRec;

        for (User user: users) {
            currentListens = 0;

            currentUserRec = user.getPlayer().getRecordedEntries();
            currentListens += insertSongs(currentUserRec, topSongs);
            insertAlbums(currentUserRec, topAlbums);
            topUsers.put(user.getUsername(), currentListens);

            if (currentListens != 0) {
                listeners++;
            }
        }

        sortData(topAlbums, topSongs, topUsers);

        if (emptyData(topSongs, topAlbums, topUsers) || listeners == 0) {
            return null;
        }

        LinkedHashMap<String, Integer> printSongs = new LinkedHashMap<>();
        LinkedHashMap<String, Integer> printAlbums = new LinkedHashMap<>();
        ArrayList<String> printUsers = new ArrayList<>();
        for (Map.Entry<RecordedEntry, Integer> song: topSongs.entrySet()) {
            printSongs.put(song.getKey().getName(), song.getValue());
        }
        for (Map.Entry<RecordedEntry, Integer> album: topAlbums.entrySet()) {
            printAlbums.put(album.getKey().getName(), album.getValue());
        }
        for (Map.Entry<String, Integer> user: topUsers.entrySet()) {
            if (user.getValue() == 0) {
                break;
            }

            printUsers.add(user.getKey());
        }

        result.set("topAlbums", objectMapper.valueToTree(printAlbums));
        result.set("topSongs", objectMapper.valueToTree(printSongs));
        result.set("topFans", objectMapper.valueToTree(printUsers));
        result.put("listeners", listeners);

        return result;
    }

    /**
     * Calculates the length of the maps songs, albums and users
     *
     * @param topSongs the songs
     * @param topAlbums the albums
     * @param topUsers the users
     * @return true if there are statistics to show for the artist
     */
    boolean emptyData(final LinkedHashMap<RecordedEntry, Integer> topSongs,
                      final LinkedHashMap<RecordedEntry, Integer> topAlbums,
                      final LinkedHashMap<String, Integer> topUsers) {
        return topSongs.isEmpty() && topAlbums.isEmpty() && topUsers.isEmpty();
    }
}
