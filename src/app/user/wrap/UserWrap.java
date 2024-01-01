package app.user.wrap;

import app.audio.RecordedEntry;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
@Setter
public final class UserWrap implements Wrap {
    private static UserWrap instance = null;
    private HashMap<RecordedEntry, Integer> recordedEntries;
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
                         final LinkedHashMap<RecordedEntry, Integer> albums,
                         final LinkedHashMap<RecordedEntry, Integer> episodes) {

        int listens;
        for (Map.Entry<RecordedEntry, Integer> song: songs.entrySet()) {
            RecordedEntry rec = song.getKey();
            listens = song.getValue();
            genres.put(rec.getGenre(), genres.getOrDefault(rec.getGenre(), 0) + listens);
        }

        for (Map.Entry<RecordedEntry, Integer> album: albums.entrySet()) {
            RecordedEntry rec = album.getKey();
            listens = album.getValue();
            artists.put(rec.getCreator(), artists.getOrDefault(rec.getCreator(), 0) + listens);
        }

        sortStringIntMaps(artists);
        sortStringIntMaps(genres);
        sortRecorded(songs);
        sortRecorded(albums);
        sortRecorded(episodes);
    }

    /**
     * Sorting descending by value, then by key audio product name
     *
     * @param map the map with (String, Integer) entry
     */
    private void sortStringIntMaps(final LinkedHashMap<String, Integer> map) {
        LinkedHashMap<String, Integer> orderedMap = map.entrySet()
                .stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue(Comparator.reverseOrder())
                        .thenComparing(Map.Entry::getKey))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (obj1, obj2) -> obj1,
                        LinkedHashMap::new
                ));
        map.clear();
        map.putAll(orderedMap);

        int len = map.size();
        if (len > topReference) {
            List<String> trashKeys = new ArrayList<>(map.keySet())
                                    .subList(topReference, len);
            for (String key: trashKeys) {
                map.remove(key);
            }
        }
    }

    /**
     * Sorting descending by value, then by key audio product name
     *
     * @param map the map with (RecordedEntry, Integer) entry
     */
    private void sortRecorded(final LinkedHashMap<RecordedEntry, Integer> map) {
        LinkedHashMap<RecordedEntry, Integer> orderedMap = map.entrySet()
                .stream()
                .sorted(Map.Entry.
                        <RecordedEntry, Integer>comparingByValue(Comparator.reverseOrder())
                        .thenComparing(obj -> obj.getKey().getName()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (obj1, obj2) -> obj1,
                        LinkedHashMap::new
                ));
        map.clear();
        map.putAll(orderedMap);

        int len = map.size();
        if (len > topReference) {
            List<RecordedEntry> trashKeys = new ArrayList<>(map.keySet())
                                            .subList(topReference, len);
            for (RecordedEntry key: trashKeys) {
                map.remove(key);
            }
        }
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

        LinkedHashMap<RecordedEntry, Integer> songs = recordedEntries.entrySet()
                .stream()
                .filter(obj -> "song".equals(obj.getKey().getType()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (obj1, obj22) -> obj1,
                        LinkedHashMap::new
                ));

        LinkedHashMap<RecordedEntry, Integer> albums = recordedEntries.entrySet()
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

        sortData(artists, genres, songs, albums, episodes);

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

        for (Map.Entry<RecordedEntry, Integer> album: albums.entrySet()) {
            printData.put(album.getKey().getName(), album.getValue());
        }
        result.set("topAlbums", objectMapper.valueToTree(printData));
        printData.clear();

        for (Map.Entry<RecordedEntry, Integer> episode: episodes.entrySet()) {
            printData.put(episode.getKey().getName(), episode.getValue());
        }
        result.set("topEpisodes", objectMapper.valueToTree(printData));
        printData.clear();

        return result;
    }
}
