package app.user.wrap;

import app.audio.RecordedEntry;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Comparator;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

public interface Wrap {
    /**
     * Sorting descending by value, then by key audio product name
     *
     * @param map the map with (String, Integer) entry
     */
    default void sortStringIntMaps(final LinkedHashMap<String, Integer> map,
                                   int topReference) {
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
    default void sortRecorded(final LinkedHashMap<RecordedEntry, Integer> map,
                              int topReference) {
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
     * Used to generate statics depending on the user type
     *
     * @return the object node
     */
    ObjectNode generateStatistics();
}
