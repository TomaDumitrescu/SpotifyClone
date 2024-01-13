package app.user.wrap;

import app.audio.RecordedEntry;
import app.user.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public final class HostWrap implements WrapStrategy {
    private static HostWrap instance = null;
    private List<User> users;
    private String username;
    private final int topReference = 5;

    private HostWrap() {
    }

    /**
     * Lazy instantiation
     *
     * @return Singleton instance
     */
    public static HostWrap getInstance() {
        if (instance == null) {
            instance = new HostWrap();
        }
        return instance;
    }

    /**
     * Adding in topSongs all user songs from a given artist
     *
     * @param currentUserRec the recorded entries for the user in iteration
     * @param topEpisodes the episodes hashmap
     * @return the total number of listens from the user in iteration
     */
    public int insertEpisodes(final HashMap<RecordedEntry, Integer> currentUserRec,
                           final LinkedHashMap<RecordedEntry, Integer> topEpisodes) {
        int currentListens = 0;

        for (Map.Entry<RecordedEntry, Integer> rec: currentUserRec.entrySet()) {
            RecordedEntry recorded = rec.getKey();
            if (recorded.getType().equalsIgnoreCase("episode")
                    && recorded.getCreator().equalsIgnoreCase(username)) {
                currentListens += rec.getValue();
                topEpisodes.put(rec.getKey(),
                        topEpisodes.getOrDefault(rec.getKey(), 0) + rec.getValue());
            }
        }

        return currentListens;
    }

    @Override
    public ObjectNode generateStatistics() {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode result = objectMapper.createObjectNode();

        int listeners = 0, currentListens;

        LinkedHashMap<RecordedEntry, Integer> topEpisodes = new LinkedHashMap<>();
        HashMap<RecordedEntry, Integer> currentUserRec;

        for (User user: users) {
            currentListens = 0;

            currentUserRec = user.getPlayer().getRecordedEntries();

            // avoids duplicates and counts listens
            currentListens += insertEpisodes(currentUserRec, topEpisodes);

            if (currentListens != 0) {
                listeners++;
            }
        }

        sortRecorded(topEpisodes, topReference);

        if (topEpisodes.isEmpty()) {
            return null;
        }

        LinkedHashMap<String, Integer> printData = new LinkedHashMap<>();
        for (Map.Entry<RecordedEntry, Integer> episode: topEpisodes.entrySet()) {
            printData.put(episode.getKey().getName(), episode.getValue());
        }

        result.set("topEpisodes", objectMapper.valueToTree(printData));
        result.put("listeners", listeners);

        return result;
    }
}
