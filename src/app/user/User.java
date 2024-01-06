package app.user;

import app.Admin;
import app.audio.Collections.AudioCollection;
import app.audio.Collections.Playlist;
import app.audio.Collections.PlaylistOutput;
import app.audio.Collections.Album;
import app.audio.Collections.Podcast;
import app.audio.Files.AudioFile;
import app.audio.Files.Song;
import app.audio.LibraryEntry;
import app.audio.RecordedEntry;
import app.pages.HomePage;
import app.pages.LikedContentPage;
import app.pages.Page;
import app.player.Player;
import app.player.PlayerStats;
import app.searchBar.Filters;
import app.searchBar.SearchBar;
import app.utils.Enums;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Comparator;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;

/**
 * The type User.
 */
public final class User extends UserAbstract {
    @Getter
    private ArrayList<Playlist> playlists;
    @Getter
    private ArrayList<Song> songRecommendations;
    @Getter
    private ArrayList<Playlist> playlistRecommendations;
    @Getter
    private ArrayList<Song> likedSongs;
    @Getter
    private ArrayList<Playlist> followedPlaylists;
    @Getter
    private final Player player;
    @Getter
    private boolean status;
    private final SearchBar searchBar;
    private boolean lastSearched;
    @Getter
    @Setter
    private Page currentPage;
    @Getter
    @Setter
    private ArrayList<Page> pageHistory;
    @Getter
    @Setter
    private int pageIndex;
    @Getter
    @Setter
    private HomePage homePage;
    @Getter
    @Setter
    private LikedContentPage likedContentPage;
    private Song recommendedSong;
    private Playlist recommendedPlaylist;
    private final int audioSeed = 30;
    private final int topRecommended = 3;
    private final int randSongsFirstGenre = 5;
    private final int randSongsSecondGenre = 3;
    private final int randSongsThirdGenre = 2;
    private final int topReference = 5;

    /**
     * Instantiates a new User.
     *
     * @param username the username
     * @param age      the age
     * @param city     the city
     */
    public User(final String username, final int age, final String city) {
        super(username, age, city);
        playlists = new ArrayList<>();
        likedSongs = new ArrayList<>();
        songRecommendations = new ArrayList<>();
        playlistRecommendations = new ArrayList<>();
        followedPlaylists = new ArrayList<>();
        player = new Player();
        searchBar = new SearchBar(username);
        lastSearched = false;
        status = true;

        homePage = new HomePage(getLikedSongs(), getFollowedPlaylists());
        currentPage = homePage;
        pageHistory = new ArrayList<>();
        pageIndex = -1;
        likedContentPage = new LikedContentPage(getLikedSongs(), getFollowedPlaylists());
    }

    @Override
    public String userType() {
        return "user";
    }

    /**
     * Search array list.
     *
     * @param filters the filters
     * @param type    the type
     * @return the array list
     */
    public ArrayList<String> search(final Filters filters, final String type) {
        searchBar.clearSelection();
        player.stop();

        lastSearched = true;
        ArrayList<String> results = new ArrayList<>();

        if (type.equals("artist") || type.equals("host")) {
            List<ContentCreator> contentCreatorsEntries =
                    searchBar.searchContentCreator(filters, type);

            for (ContentCreator contentCreator : contentCreatorsEntries) {
                results.add(contentCreator.getUsername());
            }
        } else {
            List<LibraryEntry> libraryEntries = searchBar.search(filters, type);

            for (LibraryEntry libraryEntry : libraryEntries) {
                results.add(libraryEntry.getName());
            }
        }
        return results;
    }

    /**
     * Select string.
     *
     * @param itemNumber the item number
     * @return the string
     */
    public String select(final int itemNumber) {
        if (!status) {
            return "%s is offline.".formatted(getUsername());
        }

        if (!lastSearched) {
            return "Please conduct a search before making a selection.";
        }

        lastSearched = false;

        if (searchBar.getLastSearchType().equals("artist")
            || searchBar.getLastSearchType().equals("host")) {
            ContentCreator selected = searchBar.selectContentCreator(itemNumber);

            if (selected == null) {
                return "The selected ID is too high.";
            }

            currentPage = selected.getPage();
            pageHistory.add(currentPage);
            pageIndex++;
            return "Successfully selected %s's page.".formatted(selected.getUsername());
        } else {
            LibraryEntry selected = searchBar.select(itemNumber);

            if (selected == null) {
                return "The selected ID is too high.";
            }

            return "Successfully selected %s.".formatted(selected.getName());
        }
    }

    /**
     * Load string.
     *
     * @return the string
     */
    public String load() {
        if (!status) {
            return "%s is offline.".formatted(getUsername());
        }

        if (searchBar.getLastSelected() == null) {
            return "Please select a source before attempting to load.";
        }

        if (!searchBar.getLastSearchType().equals("song")
            && ((AudioCollection) searchBar.getLastSelected()).getNumberOfTracks() == 0) {
            return "You can't load an empty audio collection!";
        }

        player.setSource(searchBar.getLastSelected(), searchBar.getLastSearchType());
        searchBar.clearSelection();

        player.pause();

        return "Playback loaded successfully.";
    }

    /**
     * Load string.
     *
     * @return the string
     */
    public String loadRecommendations() {
        if (!status) {
            return "%s is offline.".formatted(getUsername());
        }

        if (recommendedSong == null && recommendedPlaylist == null) {
            return "No recommendations available.";
        }

        if (recommendedSong != null) {
            player.setSource(recommendedSong, "song");
            player.pause();
        } else {
            if (recommendedPlaylist.getSongs().isEmpty()) {
                return "No recommendations available.";
            }
            player.setSource(recommendedPlaylist, "playlist");
            player.pause();
        }

        return "Playback loaded successfully.";
    }

    /**
     * Play pause string.
     *
     * @return the string
     */
    public String playPause() {
        if (!status) {
            return "%s is offline.".formatted(getUsername());
        }

        if (player.getCurrentAudioFile() == null) {
            return "Please load a source before attempting to pause or resume playback.";
        }

        player.pause();

        if (player.getPaused()) {
            return "Playback paused successfully.";
        } else {
            return "Playback resumed successfully.";
        }
    }

    /**
     * Repeat string.
     *
     * @return the string
     */
    public String repeat() {
        if (!status) {
            return "%s is offline.".formatted(getUsername());
        }

        if (player.getCurrentAudioFile() == null) {
            return "Please load a source before setting the repeat status.";
        }

        Enums.RepeatMode repeatMode = player.repeat();
        String repeatStatus = "";

        switch (repeatMode) {
            case NO_REPEAT -> {
                repeatStatus = "no repeat";
            }
            case REPEAT_ONCE -> {
                repeatStatus = "repeat once";
            }
            case REPEAT_ALL -> {
                repeatStatus = "repeat all";
            }
            case REPEAT_INFINITE -> {
                repeatStatus = "repeat infinite";
            }
            case REPEAT_CURRENT_SONG -> {
                repeatStatus = "repeat current song";
            }
            default -> {
                repeatStatus = "";
            }
        }

        return "Repeat mode changed to %s.".formatted(repeatStatus);
    }

    /**
     * Shuffle string.
     *
     * @param seed the seed
     * @return the string
     */
    public String shuffle(final Integer seed) {
        if (!status) {
            return "%s is offline.".formatted(getUsername());
        }

        if (player.getCurrentAudioFile() == null) {
            return "Please load a source before using the shuffle function.";
        }

        if (!player.getType().equals("playlist")
            && !player.getType().equals("album")) {
            return "The loaded source is not a playlist or an album.";
        }

        player.shuffle(seed);

        if (player.getShuffle()) {
            return "Shuffle function activated successfully.";
        }
        return "Shuffle function deactivated successfully.";
    }

    /**
     * Forward string.
     *
     * @return the string
     */
    public String forward() {
        if (!status) {
            return "%s is offline.".formatted(getUsername());
        }

        if (player.getCurrentAudioFile() == null) {
            return "Please load a source before attempting to forward.";
        }

        if (!player.getType().equals("podcast")) {
            return "The loaded source is not a podcast.";
        }

        player.skipNext();

        return "Skipped forward successfully.";
    }

    /**
     * Backward string.
     *
     * @return the string
     */
    public String backward() {
        if (!status) {
            return "%s is offline.".formatted(getUsername());
        }

        if (player.getCurrentAudioFile() == null) {
            return "Please select a source before rewinding.";
        }

        if (!player.getType().equals("podcast")) {
            return "The loaded source is not a podcast.";
        }

        player.skipPrev();

        return "Rewound successfully.";
    }

    /**
     * Like string.
     *
     * @return the string
     */
    public String like() {
        if (!status) {
            return "%s is offline.".formatted(getUsername());
        }

        if (player.getCurrentAudioFile() == null) {
            return "Please load a source before liking or unliking.";
        }

        if (!player.getType().equals("song") && !player.getType().equals("playlist")
            && !player.getType().equals("album")) {
            return "Loaded source is not a song.";
        }

        Song song = (Song) player.getCurrentAudioFile();

        if (likedSongs.contains(song)) {
            likedSongs.remove(song);
            song.dislike();

            return "Unlike registered successfully.";
        }

        likedSongs.add(song);
        song.like();
        return "Like registered successfully.";
    }

    /**
     * Next string.
     *
     * @return the string
     */
    public String next() {
        if (!status) {
            return "%s is offline.".formatted(getUsername());
        }

        if (player.getCurrentAudioFile() == null) {
            return "Please load a source before skipping to the next track.";
        }

        player.next();

        if (player.getCurrentAudioFile() == null) {
            return "Please load a source before skipping to the next track.";
        }

        return "Skipped to next track successfully. The current track is %s."
                .formatted(player.getCurrentAudioFile().getName());
    }

    /**
     * Prev string.
     *
     * @return the string
     */
    public String prev() {
        if (!status) {
            return "%s is offline.".formatted(getUsername());
        }

        if (player.getCurrentAudioFile() == null) {
            return "Please load a source before returning to the previous track.";
        }

        player.prev();

        return "Returned to previous track successfully. The current track is %s."
                .formatted(player.getCurrentAudioFile().getName());
    }

    /**
     * Create playlist string.
     *
     * @param name      the name
     * @param timestamp the timestamp
     * @return the string
     */
    public String createPlaylist(final String name, final int timestamp) {
        if (!status) {
            return "%s is offline.".formatted(getUsername());
        }

        if (playlists.stream().anyMatch(playlist -> playlist.getName().equals(name))) {
            return "A playlist with the same name already exists.";
        }

        playlists.add(new Playlist(name, getUsername(), timestamp));

        return "Playlist created successfully.";
    }

    /**
     * Add remove in playlist string.
     *
     * @param id the id
     * @return the string
     */
    public String addRemoveInPlaylist(final int id) {
        if (!status) {
            return "%s is offline.".formatted(getUsername());
        }

        if (player.getCurrentAudioFile() == null) {
            return "Please load a source before adding to or removing from the playlist.";
        }

        if (player.getType().equals("podcast")) {
            return "The loaded source is not a song.";
        }

        if (id > playlists.size()) {
            return "The specified playlist does not exist.";
        }

        Playlist playlist = playlists.get(id - 1);

        if (playlist.containsSong((Song) player.getCurrentAudioFile())) {
            playlist.removeSong((Song) player.getCurrentAudioFile());
            return "Successfully removed from playlist.";
        }

        playlist.addSong((Song) player.getCurrentAudioFile());
        return "Successfully added to playlist.";
    }

    /**
     * Switch playlist visibility string.
     *
     * @param playlistId the playlist id
     * @return the string
     */
    public String switchPlaylistVisibility(final Integer playlistId) {
        if (!status) {
            return "%s is offline.".formatted(getUsername());
        }

        if (playlistId > playlists.size()) {
            return "The specified playlist ID is too high.";
        }

        Playlist playlist = playlists.get(playlistId - 1);
        playlist.switchVisibility();

        if (playlist.getVisibility() == Enums.Visibility.PUBLIC) {
            return "Visibility status updated successfully to public.";
        }

        return "Visibility status updated successfully to private.";
    }

    /**
     * Show playlists array list.
     *
     * @return the array list
     */
    public ArrayList<PlaylistOutput> showPlaylists() {
        ArrayList<PlaylistOutput> playlistOutputs = new ArrayList<>();
        for (Playlist playlist : playlists) {
            playlistOutputs.add(new PlaylistOutput(playlist));
        }

        return playlistOutputs;
    }

    /**
     * Follow string.
     *
     * @return the string
     */
    public String follow() {
        if (!status) {
            return "%s is offline.".formatted(getUsername());
        }

        LibraryEntry selection = searchBar.getLastSelected();
        String type = searchBar.getLastSearchType();

        if (selection == null) {
            return "Please select a source before following or unfollowing.";
        }

        if (!type.equals("playlist")) {
            return "The selected source is not a playlist.";
        }

        Playlist playlist = (Playlist) selection;

        if (playlist.getOwner().equals(getUsername())) {
            return "You cannot follow or unfollow your own playlist.";
        }

        if (followedPlaylists.contains(playlist)) {
            followedPlaylists.remove(playlist);
            playlist.decreaseFollowers();

            return "Playlist unfollowed successfully.";
        }

        followedPlaylists.add(playlist);
        playlist.increaseFollowers();


        return "Playlist followed successfully.";
    }

    /**
     * Gets player stats.
     *
     * @return the player stats
     */
    public PlayerStats getPlayerStats() {
        return player.getStats();
    }

    /**
     * Show preferred songs array list.
     *
     * @return the array list
     */
    public ArrayList<String> showPreferredSongs() {
        ArrayList<String> results = new ArrayList<>();
        for (AudioFile audioFile : likedSongs) {
            results.add(audioFile.getName());
        }

        return results;
    }

    /**
     * Gets preferred genre.
     *
     * @return the preferred genre
     */
    public String getPreferredGenre() {
        String[] genres = {"pop", "rock", "rap"};
        int[] counts = new int[genres.length];
        int mostLikedIndex = -1;
        int mostLikedCount = 0;

        for (Song song : likedSongs) {
            for (int i = 0; i < genres.length; i++) {
                if (song.getGenre().equals(genres[i])) {
                    counts[i]++;
                    if (counts[i] > mostLikedCount) {
                        mostLikedCount = counts[i];
                        mostLikedIndex = i;
                    }
                    break;
                }
            }
        }

        String preferredGenre = mostLikedIndex != -1 ? genres[mostLikedIndex] : "unknown";
        return "This user's preferred genre is %s.".formatted(preferredGenre);
    }

    /**
     * Switch status.
     */
    public void switchStatus() {
        status = !status;
    }

    /**
     * Simulate time.
     *
     * @param time the time
     */
    public void simulateTime(final int time) {
        if (!status) {
            return;
        }

        player.simulatePlayer(time);
    }

    /**
     * Updates the recommendations for this user
     *
     * @param recommend the recommendation type
     * @param songs the admin list of songs
     */
    public void updateRecommendations(final String recommend,
                                      final List<Song> songs,
                                      final List<User> users) {
        switch (recommend) {
            case "random_song" -> recommendSong(new ArrayList<>(songs));
            case "random_playlist" -> recommendPlaylist(new ArrayList<>(songs));
            case "fans_playlist" -> recommendFanPlaylist(new ArrayList<>(users));
            default -> { }
        }
    }

    /**
     * Adds to the list of recommended songs
     *
     * @param songs the admin list of songs
     */
    private void recommendSong(final List<Song> songs) {
        AudioFile current = player.getCurrentAudioFile();
        if (current == null || player.getCurrentAudioCollection() != null) {
            return;
        }

        if (!Song.class.isAssignableFrom(current.getClass())) {
            return;
        }

        int remained = player.getSource().getRemainedDuration();
        String genre = ((Song) current).getGenre();
        int elapsed = current.getDuration() - remained;

        if (elapsed < audioSeed) {
            return;
        }

        List<Song> specificSongs = new ArrayList<>();
        for (Song song: songs) {
            if (song.getGenre().equalsIgnoreCase(genre)) {
                specificSongs.add(song);
            }
        }

        Random random = new Random(elapsed);
        int index = random.nextInt(specificSongs.size());
        songRecommendations.add(specificSongs.get(index));

        // update the last recommended audio products
        recommendedSong = specificSongs.get(index);
        recommendedPlaylist = null;

        homePage = new HomePage(getLikedSongs(), getFollowedPlaylists(),
                getSongRecommendations(), getPlaylistRecommendations());

        currentPage = homePage;
    }

    /**
     * Adds to the list of recommended playlists
     */
    private void recommendPlaylist(final List<Song> songs) {
        HashMap<String, Integer> gMap = getGenresMap();

        LinkedHashMap<String, Integer> orderedMap = gMap.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (obj1, obj2) -> obj1,
                        LinkedHashMap::new
                ));

        if (orderedMap.size() > topRecommended) {
            List<String> trashKeys = new ArrayList<>(orderedMap.keySet())
                    .subList(topRecommended, orderedMap.size());
            for (String key: trashKeys) {
                orderedMap.remove(key);
            }
        }

        List<String> genres = new ArrayList<>();
        for (Map.Entry<String, Integer> genre: orderedMap.entrySet()) {
            genres.add(genre.getKey());
        }

        List<Song> playlistSongs = new ArrayList<>();

        Random rand = new Random();

        int numGenres = orderedMap.size(), cnt = 0;
        while (cnt < numGenres) {
            String genre = genres.get(cnt);
            List<Song> randSongs = new ArrayList<>();
            List<Song> genreSongs = new ArrayList<>();

            for (Song song: songs) {
                if (song.getGenre().equals(genre)) {
                    genreSongs.add(song);
                }
            }

            int numSongs = 0;
            if (cnt == 0) {
                numSongs = randSongsFirstGenre;
            } else if (cnt == 1) {
                numSongs = randSongsSecondGenre;
            } else if (cnt == 2) {
                numSongs = randSongsThirdGenre;
            }

            while (numSongs > 0) {
                randSongs.add(genreSongs.get(rand.nextInt(genreSongs.size())));
                numSongs--;
            }

            playlistSongs.addAll(randSongs);
            cnt++;
        }

        int len = playlistSongs.size();
        for (int i = 0; i < len; i++) {
            for (int j = i + 1; j < len; j++) {
                if (playlistSongs.get(i).getLikes() < playlistSongs.get(j).getLikes()) {
                    Song temp = playlistSongs.get(i);
                    playlistSongs.set(i, playlistSongs.get(j));
                    playlistSongs.set(j, temp);
                }
            }
        }

        String pName = "%s's recommendations".formatted(getUsername());
        Playlist playlist = new Playlist(pName, getUsername());
        for (Song song: playlistSongs) {
            playlist.addSong(song);
        }

        playlistRecommendations.add(playlist);

        homePage = new HomePage(getLikedSongs(), getFollowedPlaylists(),
                getSongRecommendations(), getPlaylistRecommendations());

        currentPage = homePage;


        // update recommended audio products
        recommendedPlaylist = playlist;
        recommendedSong = null;
    }

    /**
     * Counts genres from songs, playlists and followedPlaylists
     *
     * @return the genres hashmap for playlist recommendations
     */
    private HashMap<String, Integer> getGenresMap() {
        HashMap<String, Integer> gMap = new HashMap<>();
        for (Song song: likedSongs) {
            gMap.put(song.getGenre(), gMap.getOrDefault(song.getGenre(), 0) + 1);
        }

        for (Playlist playlist: playlists) {
            for (Song song: playlist.getSongs()) {
                gMap.put(song.getGenre(), gMap.getOrDefault(song.getGenre(), 0) + 1);
            }
        }

        for (Playlist playlist: followedPlaylists) {
            for (Song song: playlist.getSongs()) {
                gMap.put(song.getGenre(), gMap.getOrDefault(song.getGenre(), 0) + 1);
            }
        }
        return gMap;
    }

    /**
     * Recommends a playlist based on the user's history of likes
     *
     */
    public void recommendFanPlaylist(final List<User> users) {
        AudioFile audioFile = player.getCurrentAudioFile();
        if (audioFile == null || !Song.class.isAssignableFrom(audioFile.getClass())) {
            return;
        }

        List<Integer> userListens = getListens(users, (Song) audioFile);

        int len = userListens.size() - 1;
        for (int i = 0; i < len - 1; i++) {
            for (int j = 0; j < len; j++) {
                if (userListens.get(i) < userListens.get(j)) {
                    int tmpListens = userListens.get(i);
                    userListens.set(i, userListens.get(j));
                    userListens.set(j, tmpListens);

                    User tmpUser = users.get(i);
                    users.set(i, users.get(j));
                    users.set(j, tmpUser);
                }
            }
        }

        while (users.size() > topReference) {
            users.remove(users.size() - 1);
        }

        List<Song> playlistSongs = new ArrayList<>();
        for (User user: users) {
            List<Song> likedSongsItr = user.getLikedSongs();
            likedSongsItr.sort(Comparator.comparingInt(Song::getLikes));
            int i = 0;
            while (i < likedSongsItr.size() && i < topReference) {
                playlistSongs.add(likedSongsItr.get(i));
                i++;
            }
        }

        // eliminating duplicates
        Set<Song> auxSongs = new HashSet<>(playlistSongs);
        playlistSongs = new ArrayList<>(auxSongs);

        playlistSongs.sort(Comparator.comparingInt(Song::getLikes));
        Playlist playlist = new Playlist("%s Fan Club recommendations"
                .formatted(((Song) audioFile).getArtist()), getUsername());

        for (int i = 0; i < playlistSongs.size(); i++) {
            Song song = playlistSongs.get(i);
            playlist.addSong(song);
        }

        playlistRecommendations.add(playlist);

        homePage = new HomePage(getLikedSongs(), getFollowedPlaylists(),
                getSongRecommendations(), getPlaylistRecommendations());

        currentPage = homePage;

        recommendedPlaylist = playlist;
        recommendedSong = null;
    }

    /**
     * Calculates the number of listens of the current song artist
     * for each user
     *
     * @param users the admin borrowed list of users
     * @param current the loaded song
     * @return the list of users listens for current artist
     */
    private List<Integer> getListens(final List<User> users, final Song current) {
        String artist = current.getArtist();

        int currentListens;
        List<Integer> userListens = new ArrayList<>();
        for (User user: users) {
            currentListens = 0;
            HashMap<RecordedEntry, Integer> recList = user.player.getRecordedEntries();
            for (Map.Entry<RecordedEntry, Integer> rec: recList.entrySet()) {
                RecordedEntry audioProduct = rec.getKey();
                if (audioProduct.getCreator().equals(artist)
                    && audioProduct.getType().equals("song")) {
                    currentListens += rec.getValue();
                }
            }
            userListens.add(currentListens);
        }
        return userListens;
    }

    /**
     * Changes the page of the user with the previous one
     *
     * @return the success message of the command
     */
    public String prevPage() {
        if (pageIndex - 1 < 0 || pageHistory.isEmpty()) {
            return "There are no pages left to go back.";
        }
        pageIndex--;
        currentPage = pageHistory.get(pageIndex);

        return "The user %s has navigated successfully to the previous page."
                .formatted(getUsername());
    }

    /**
     * Changes the page of the user with the next one
     *
     * @return the success message of the command
     */
    public String nextPage() {
        if (pageIndex + 1 >= pageHistory.size()) {
            return "There are no pages left to go forward.";
        }
        pageIndex++;
        currentPage = pageHistory.get(pageIndex);

        return "The user %s has navigated successfully to the next page."
                .formatted(getUsername());
    }

    /**
     * Gets the creator page of the current track
     *
     * @return the command message
     */
    public String setLiveCreatorPage(final String pageType) {
        AudioCollection collection = player.getCurrentAudioCollection();
        AudioFile file = player.getCurrentAudioFile();
        if (collection == null && file == null) {
            return "%s is trying to access a non-existent page.".formatted(getUsername());
        }

        if (pageType.equals("host") && Album.class.isAssignableFrom(collection.getClass())) {
            return "%s is trying to access a non-existent page.".formatted(getUsername());
        }

        if (pageType.equals("artist")) {
            if (collection != null && Podcast.class.isAssignableFrom(collection.getClass())) {
                return "%s is trying to access a non-existent page.".formatted(getUsername());
            }

            if (file != null && !Song.class.isAssignableFrom(file.getClass())) {
                return "%s is trying to access a non-existent page.".formatted(getUsername());
            }
        }

        if (pageType.equals("artist")) {
            String artistName;
            if (collection != null) {
                artistName = collection.getOwner();
                Artist artist = Admin.getInstance().getArtist(artistName);
                setCurrentPage(artist.getPage());
            } else {
                artistName = ((Song) file).getArtist();
                Artist artist = Admin.getInstance().getArtist(artistName);
                setCurrentPage(artist.getPage());
            }

            pageHistory.add(currentPage);
            pageIndex++;

            return "%s accessed Artist successfully.".formatted(getUsername());
        } else {
            if (collection != null) {
                String hostName = collection.getOwner();
                Host host = Admin.getInstance().getHost(hostName);
                setCurrentPage(host.getPage());
                pageHistory.add(currentPage);
                pageIndex++;

                return "%s accessed Host successfully.".formatted(getUsername());
            }

            return "%s is trying to access a non-existent page.".formatted(getUsername());
        }
    }
}
