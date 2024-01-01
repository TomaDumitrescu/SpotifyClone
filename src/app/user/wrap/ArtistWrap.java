package app.user.wrap;

import app.audio.RecordedEntry;
import app.user.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import lombok.Getter;
import lombok.Setter;

import java.util.*;

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
    public int insertSongs(HashMap<RecordedEntry, Integer> currentUserRec,
                           LinkedHashMap<RecordedEntry, Integer> topSongs) {
        int currentListens = 0;

        for (Map.Entry<RecordedEntry, Integer> rec: currentUserRec.entrySet()) {
            RecordedEntry recorded = rec.getKey();
            if (recorded.getType().equalsIgnoreCase("song")) {
                currentListens += rec.getValue();
                topSongs.put(rec.getKey(), rec.getValue());
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
    public void insertAlbums(HashMap<RecordedEntry, Integer> currentUserRec,
                           LinkedHashMap<RecordedEntry, Integer> topAlbums) {
        for (Map.Entry<RecordedEntry, Integer> rec: currentUserRec.entrySet()) {
            RecordedEntry recorded = rec.getKey();
            if (recorded.getType().equalsIgnoreCase("album")) {
                topAlbums.put(rec.getKey(), rec.getValue());
            }
        }
    }

    public void sortData(LinkedHashMap<RecordedEntry, Integer> topAlbums,
                         LinkedHashMap<RecordedEntry, Integer> topSongs,
                         LinkedHashMap<String, Integer> topUsers) {

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
            if (user.getPlayer() == null) {
                continue;
            }
            currentUserRec = user.getPlayer().getRecordedEntries();
            currentListens += insertSongs(currentUserRec, topSongs);
            insertAlbums(currentUserRec, topAlbums);
            topUsers.put(user.getUsername(), currentListens);

            if (currentListens != 0) {
                listeners++;
            }
        }

        sortData(topAlbums, topSongs, topUsers);

        if (emptyData(topSongs, topAlbums, topUsers)) {
            String message = "No data to show for %s.".formatted(username);
            result.put("result", message);
            return result;
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
     * @param songs the songs
     * @param albums the albums
     * @param users the users
     * @return true if there are statistics to show for the artist
     */
    boolean emptyData(final LinkedHashMap<RecordedEntry, Integer> songs,
                      final LinkedHashMap<RecordedEntry, Integer> albums,
                      final LinkedHashMap<String, Integer> users) {
        return songs.isEmpty() && albums.isEmpty() && users.isEmpty();
    }
}
