package app.pages;

import app.audio.Collections.Playlist;
import app.audio.Files.Song;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * The type Home page.
 */
public final class HomePage implements Page {
    private List<Song> likedSongs;
    private List<Song> recommendedSongs;
    private List<Playlist> followedPlaylists;
    private List<Playlist> recommendedPlaylists;
    private boolean recommended = false;
    private final int limit = 5;

    /**
     * Instantiates a new Home page.
     *
     * @param songs the songs
     * @param playlists the playlists
     */
    public HomePage(final List<Song> songs, final List<Playlist> playlists) {
        likedSongs = songs;
        followedPlaylists = playlists;
        recommendedSongs = new ArrayList<>();
        recommendedPlaylists = new ArrayList<>();
    }

    /**
     * Instantiates a new Home page.
     *
     * @param songs the songs
     * @param playlists the playlists
     * @param recSongs the recommended songs
     * @param recPlaylists the recommended playlists
     */
    public HomePage(final List<Song> songs, final List<Playlist> playlists,
                    final List<Song> recSongs, final List<Playlist> recPlaylists) {
        likedSongs = songs;
        followedPlaylists = playlists;
        recommendedSongs = recSongs;
        recommendedPlaylists = recPlaylists;
        recommended = true;
    }

    @Override
    public String printCurrentPage() {
        String first = "Liked songs:\n\t%s\n\nFollowed playlists:\n\t%s"
                .formatted(likedSongs.stream()
                                .sorted(Comparator.comparing(Song::getLikes)
                                        .reversed()).limit(limit).map(Song::getName)
                                .toList(),
                        followedPlaylists.stream().sorted((o1, o2) ->
                                        o2.getSongs().stream().map(Song::getLikes)
                                                .reduce(Integer::sum).orElse(0)
                                                - o1.getSongs().stream().map(Song::getLikes)
                                                .reduce(Integer::sum)
                                                .orElse(0)).limit(limit).map(Playlist::getName)
                                .toList());

        String second = "\n\nSong recommendations:\n\t%s\n\nPlaylists recommendations:\n\t%s"
                .formatted(recommendedSongs.stream()
                                .sorted(Comparator.comparing(Song::getLikes)
                                        .reversed()).limit(limit).map(Song::getName)
                                .toList(),
                        recommendedPlaylists.stream().sorted((o1, o2) ->
                                        o2.getSongs().stream().map(Song::getLikes)
                                                .reduce(Integer::sum).orElse(0)
                                                - o1.getSongs().stream().map(Song::getLikes)
                                                .reduce(Integer::sum)
                                                .orElse(0)).limit(limit).map(Playlist::getName)
                                .toList());

        if (recommended) {
            return first + second;
        } else {
            return first;
        }
    }
}
