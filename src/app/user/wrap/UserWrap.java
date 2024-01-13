package app.user.wrap;

import app.audio.RecordedEntry;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
@Setter
public final class UserWrap implements WrapStrategy {
    private static UserWrap instance = null;
    private HashMap<RecordedEntry, Integer> recordedEntries;
    private HashMap<String, Integer> listenedGenres;
    private String username;
    private final int topReference = 5;

    private UserWrap() {
    }

    /**
     * Lazy instantiation
     *
     * @return Singleton instance
     */
    public static UserWrap getInstance() {
        if (instance == null) {
            instance = new UserWrap();
        }
        return instance;
    }

    /**
     * Finds the list of artists, genres, then sorts the parameters
     * lists, then the function gets the top 5 elements
     *
     * @param artists the artists, no elements
     * @param genres the genres, no elements
     * @param songs the songs
     * @param albums the albums
     * @param episodes the episodes
     */
    public void sortData(final LinkedHashMap<String, Integer> artists,
                         final LinkedHashMap<String, Integer> genres,
                         final LinkedHashMap<RecordedEntry, Integer> songs,
                         final LinkedHashMap<RecordedEntry, Integer> auxAlbums,
                         final LinkedHashMap<String, Integer> albums,
                         final LinkedHashMap<RecordedEntry, Integer> episodes) {

        int listens;
        genres.putAll(listenedGenres);

        for (Map.Entry<RecordedEntry, Integer> album: auxAlbums.entrySet()) {
            RecordedEntry rec = album.getKey();
            listens = album.getValue();
            artists.put(rec.getCreator(), artists.getOrDefault(rec.getCreator(), 0) + listens);
        }

        sortStringIntMaps(artists, topReference);
        sortStringIntMaps(genres, topReference);
        sortRecorded(songs, topReference);
        sortStringIntMaps(albums, topReference);
        sortRecorded(episodes, topReference);
    }

    /**
     * Generates statistics for the user
     *
     * @return the object node
     */
    @Override
    public ObjectNode generateStatistics() {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode result = objectMapper.createObjectNode();

        LinkedHashMap<String, Integer> artists = new LinkedHashMap<>();
        LinkedHashMap<String, Integer> genres = new LinkedHashMap<>();
        LinkedHashMap<String, Integer> albums = new LinkedHashMap<>();

        // specialized library entry lists from the recordedEntries
        LinkedHashMap<RecordedEntry, Integer> songs = recordedEntries.entrySet()
                .stream()
                .filter(obj -> "song".equals(obj.getKey().getType()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (obj1, obj2) -> obj1,
                        LinkedHashMap::new
                ));

        LinkedHashMap<RecordedEntry, Integer> auxAlbums = recordedEntries.entrySet()
                .stream()
                .filter(obj -> "album".equals(obj.getKey().getType()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (obj1, obj2) -> obj1,
                        LinkedHashMap::new
                ));

        LinkedHashMap<RecordedEntry, Integer> episodes = recordedEntries.entrySet()
                .stream()
                .filter(obj -> "episode".equals(obj.getKey().getType()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (obj1, obj2) -> obj1,
                        LinkedHashMap::new
                ));

        for (Map.Entry<RecordedEntry, Integer> album: auxAlbums.entrySet()) {
            RecordedEntry rec = album.getKey();
            int val = album.getValue();
            albums.put(rec.getName(), albums.getOrDefault(rec.getName(), 0) + val);
        }

        sortData(artists, genres, songs, auxAlbums, albums, episodes);

        if (emptyData(artists, genres, songs, albums, episodes)) {
            return null;
        }

        LinkedHashMap<String, Integer> printData = new LinkedHashMap<>(artists);
        result.set("topArtists", objectMapper.valueToTree(printData));
        printData.clear();

        printData.putAll(genres);
        result.set("topGenres", objectMapper.valueToTree(printData));
        printData.clear();

        for (Map.Entry<RecordedEntry, Integer> song: songs.entrySet()) {
            printData.put(song.getKey().getName(), song.getValue());
        }
        result.set("topSongs", objectMapper.valueToTree(printData));
        printData.clear();

        printData.putAll(albums);
        result.set("topAlbums", objectMapper.valueToTree(printData));
        printData.clear();

        for (Map.Entry<RecordedEntry, Integer> episode: episodes.entrySet()) {
            printData.put(episode.getKey().getName(), episode.getValue());
        }
        result.set("topEpisodes", objectMapper.valueToTree(printData));
        printData.clear();

        return result;
    }

    /**
     * Calculates the length of each linked hash maps
     *
     * @param artists the artists
     * @param genres the genres
     * @param songs the songs
     * @param albums the albums
     * @param episodes the episodes
     * @return true if there are statistics about the user to show
     */
    boolean emptyData(final LinkedHashMap<String, Integer> artists,
                      final LinkedHashMap<String, Integer> genres,
                      final LinkedHashMap<RecordedEntry, Integer> songs,
                      final LinkedHashMap<String, Integer> albums,
                      final LinkedHashMap<RecordedEntry, Integer> episodes) {
        return artists.isEmpty() && genres.isEmpty() && songs.isEmpty()
                && albums.isEmpty() && episodes.isEmpty();
    }
}
