package app;

import app.audio.Collections.Album;
import app.audio.Collections.AudioCollection;
import app.audio.Collections.Playlist;
import app.audio.Collections.Podcast;
import app.audio.Files.AudioFile;
import app.audio.Files.Episode;
import app.audio.Files.Song;
import app.audio.RecordedEntry;
import app.pages.Page;
import app.player.Player;
import app.user.User;
import app.user.Artist;
import app.user.Host;
import app.user.UserFactory;
import app.user.UserAbstract;
import app.user.Event;
import app.user.Announcement;
import app.user.Merchandise;
import app.notifications.Notification;
import app.notifications.NotificationManager;
import app.user.wrap.WrapStrategy;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fileio.input.CommandInput;
import fileio.input.EpisodeInput;
import fileio.input.PodcastInput;
import fileio.input.SongInput;
import fileio.input.UserInput;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.Objects;
import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;
import java.util.Comparator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The type Admin.
 */
public final class Admin {
    @Getter
    private List<User> users = new ArrayList<>();
    @Getter
    @Setter
    private List<Artist> artists = new ArrayList<>();
    @Getter
    private List<Host> hosts = new ArrayList<>();
    private List<Song> songs = new ArrayList<>();
    private List<Podcast> podcasts = new ArrayList<>();
    private int timestamp = 0;
    private final int limit = 5;
    private final int dateStringLength = 10;
    private final int dateFormatSize = 3;
    private final int dateYearLowerLimit = 1900;
    private final int dateYearHigherLimit = 2023;
    private final int dateMonthLowerLimit = 1;
    private final int dateMonthHigherLimit = 12;
    private final int dateDayLowerLimit = 1;
    private final int dateDayHigherLimit = 31;
    private final int dateFebHigherLimit = 28;
    private final double roundTool = 100.0;
    private final double premiumPrice = 1000000;
    private final int resetedOnce = -1;
    private NotificationManager notificationManager = new NotificationManager();
    @Setter
    private Song ad;
    private static Admin instance;

    private Admin() {
    }

    /**
     * Gets instance.
     *
     * @return the instance
     */
    public static Admin getInstance() {
        if (instance == null) {
            instance = new Admin();
        }
        return instance;
    }

    /**
     * Reset instance.
     */
    public static void resetInstance() {
        instance = null;
    }

    /**
     * Sets users.
     *
     * @param userInputList the user input list
     */
    public void setUsers(final List<UserInput> userInputList) {
        for (UserInput userInput : userInputList) {
            users.add(new User(userInput.getUsername(), userInput.getAge(), userInput.getCity()));
        }
    }

    /**
     * Sets songs.
     *
     * @param songInputList the song input list
     */
    public void setSongs(final List<SongInput> songInputList) {
        for (SongInput songInput : songInputList) {
            songs.add(new Song(songInput.getName(), songInput.getDuration(), songInput.getAlbum(),
                    songInput.getTags(), songInput.getLyrics(), songInput.getGenre(),
                    songInput.getReleaseYear(), songInput.getArtist()));
        }
    }

    /**
     * Store the ad type in a song class
     *
     */
    public void setAd() {
        for (Song song: songs) {
            if (song.getName().equals("Ad Break")) {
                ad = new Song(song.getName(), song.getDuration(), song.getAlbum(),
                        song.getTags(), song.getLyrics(), song.getGenre(), song.getReleaseYear(),
                        song.getArtist());
                break;
            }
        }
    }

    /**
     * Sets podcasts.
     *
     * @param podcastInputList the podcast input list
     */
    public void setPodcasts(final List<PodcastInput> podcastInputList) {
        for (PodcastInput podcastInput : podcastInputList) {
            List<Episode> episodes = new ArrayList<>();
            for (EpisodeInput episodeInput : podcastInput.getEpisodes()) {
                episodes.add(new Episode(episodeInput.getName(),
                                         episodeInput.getDuration(),
                                         episodeInput.getDescription()));
            }
            podcasts.add(new Podcast(podcastInput.getName(), podcastInput.getOwner(), episodes));
        }
    }

    /**
     * Gets songs.
     *
     * @return the songs
     */
    public List<Song> getSongs() {
        return new ArrayList<>(songs);
    }

    /**
     * Gets podcasts.
     *
     * @return the podcasts
     */
    public List<Podcast> getPodcasts() {
        return new ArrayList<>(podcasts);
    }

    /**
     * Gets playlists.
     *
     * @return the playlists
     */
    public List<Playlist> getPlaylists() {
        return users.stream()
                    .flatMap(user -> user.getPlaylists().stream())
                    .collect(Collectors.toList());
    }

    /**
     * Gets albums.
     *
     * @return the albums
     */
    public List<Album> getAlbums() {
        return artists.stream()
                      .flatMap(artist -> artist.getAlbums().stream())
                      .collect(Collectors.toList());
    }

    /**
     * Gets all users.
     *
     * @return the all users
     */
    public List<String> getAllUsers() {
        List<String> allUsers = new ArrayList<>();

        allUsers.addAll(users.stream().map(UserAbstract::getUsername).toList());
        allUsers.addAll(artists.stream().map(UserAbstract::getUsername).toList());
        allUsers.addAll(hosts.stream().map(UserAbstract::getUsername).toList());

        return allUsers;
    }

    /**
     * Gets user.
     *
     * @param username the username
     * @return the user
     */
    public User getUser(final String username) {
        return users.stream()
                    .filter(user -> user.getUsername().equals(username))
                    .findFirst()
                    .orElse(null);
    }

    /**
     * Gets artist.
     *
     * @param username the username
     * @return the artist
     */
    public Artist getArtist(final String username) {
        return artists.stream()
                .filter(artist -> artist.getUsername().equals(username))
                .findFirst()
                .orElse(null);
    }

    /**
     * Gets host.
     *
     * @param username the username
     * @return the host
     */
    public Host getHost(final String username) {
        return hosts.stream()
                .filter(artist -> artist.getUsername().equals(username))
                .findFirst()
                .orElse(null);
    }

    /**
     * Update timestamp.
     *
     * @param newTimestamp the new timestamp
     */
    public void updateTimestamp(final int newTimestamp) {
        int elapsed = newTimestamp - timestamp;
        timestamp = newTimestamp;

        if (elapsed == 0) {
            return;
        } else if (elapsed < 0) {
            throw new IllegalArgumentException("Invalid timestamp" + newTimestamp);
        }

        users.forEach(user -> user.simulateTime(elapsed));
    }

    private UserAbstract getAbstractUser(final String username) {
        ArrayList<UserAbstract> allUsers = new ArrayList<>();

        allUsers.addAll(users);
        allUsers.addAll(artists);
        allUsers.addAll(hosts);

        return allUsers.stream()
                       .filter(userPlatform -> userPlatform.getUsername().equals(username))
                       .findFirst()
                       .orElse(null);
    }

    /**
     * Add new user string.
     *
     * @param commandInput the command input
     * @return the string
     */
    public String addNewUser(final CommandInput commandInput) {
        String username = commandInput.getUsername();
        String type = commandInput.getType();
        int age = commandInput.getAge();
        String city = commandInput.getCity();

        UserAbstract currentUser = getAbstractUser(username);
        if (currentUser != null) {
            return "The username %s is already taken.".formatted(username);
        }

        if (type.equals("user")) {
            User user = (User) UserFactory.createUser(type, username, age, city);
            users.add(user);
            notificationManager.addObserver(user);
        } else if (type.equals("artist")) {
            Artist artist = (Artist) UserFactory.createUser(type, username, age, city);
            artists.add(artist);
        } else {
            hosts.add((Host) UserFactory.createUser(type, username, age, city));
        }

        return "The username %s has been added successfully.".formatted(username);
    }

    /**
     * Delete user string.
     *
     * @param username the username
     * @return the string
     */
    public String deleteUser(final String username) {
        UserAbstract currentUser = getAbstractUser(username);

        if (currentUser == null) {
            return "The username %s doesn't exist.".formatted(username);
        }

        if (currentUser.userType().equals("user")) {
            return deleteNormalUser((User) currentUser);
        }

        if (currentUser.userType().equals("host")) {
            return deleteHost((Host) currentUser);
        }

        return deleteArtist((Artist) currentUser);
    }

    private String deleteNormalUser(final User user) {
        if (user.getPlaylists().stream().anyMatch(playlist -> users.stream().map(User::getPlayer)
                .filter(player -> player != user.getPlayer())
                .map(Player::getCurrentAudioCollection)
                .filter(Objects::nonNull)
                .anyMatch(collection -> collection == playlist))) {
            return "%s can't be deleted.".formatted(user.getUsername());
        }

        user.getLikedSongs().forEach(Song::dislike);
        user.getFollowedPlaylists().forEach(Playlist::decreaseFollowers);

        users.stream().filter(otherUser -> otherUser != user)
             .forEach(otherUser -> otherUser.getFollowedPlaylists()
                                            .removeAll(user.getPlaylists()));

        users.remove(user);
        notificationManager.rmObserver(user);
        return "%s was successfully deleted.".formatted(user.getUsername());
    }

    private String deleteHost(final Host host) {
        if (host.getPodcasts().stream().anyMatch(podcast -> getAudioCollectionsStream()
                .anyMatch(collection -> collection == podcast))
                || users.stream().anyMatch(user -> user.getCurrentPage() == host.getPage())) {
            return "%s can't be deleted.".formatted(host.getUsername());
        }

        host.getPodcasts().forEach(podcast -> podcasts.remove(podcast));
        hosts.remove(host);

        return "%s was successfully deleted.".formatted(host.getUsername());
    }

    private String deleteArtist(final Artist artist) {
        if (artist.getAlbums().stream().anyMatch(album -> album.getSongs().stream()
            .anyMatch(song -> getAudioFilesStream().anyMatch(audioFile -> audioFile == song))
            || getAudioCollectionsStream().anyMatch(collection -> collection == album))
            || users.stream().anyMatch(user -> user.getCurrentPage() == artist.getPage())) {
            return "%s can't be deleted.".formatted(artist.getUsername());
        }

        users.forEach(user -> artist.getAlbums().forEach(album -> album.getSongs().forEach(song -> {
            user.getLikedSongs().remove(song);
            user.getPlaylists().forEach(playlist -> playlist.removeSong(song));
        })));

        songs.removeAll(artist.getAllSongs());
        artists.remove(artist);
        return "%s was successfully deleted.".formatted(artist.getUsername());
    }

    /**
     * Add album string.
     *
     * @param commandInput the command input
     * @return the string
     */
    public String addAlbum(final CommandInput commandInput) {
        String username = commandInput.getUsername();
        String albumName = commandInput.getName();
        UserAbstract currentUser = getAbstractUser(username);

        if (currentUser == null) {
            return "The username %s doesn't exist.".formatted(username);
        } else if (!currentUser.userType().equals("artist")) {
            return "%s is not an artist.".formatted(username);
        }

        Artist currentArtist = (Artist) currentUser;
        if (currentArtist.getAlbums().stream()
            .anyMatch(album -> album.getName().equals(albumName))) {
            return "%s has another album with the same name.".formatted(username);
        }

        List<Song> newSongs = commandInput.getSongs().stream()
                                       .map(songInput -> new Song(songInput.getName(),
                                                                  songInput.getDuration(),
                                                                  albumName,
                                                                  songInput.getTags(),
                                                                  songInput.getLyrics(),
                                                                  songInput.getGenre(),
                                                                  songInput.getReleaseYear(),
                                                                  currentArtist.getUsername()))
                                       .toList();

        Set<String> songNames = new HashSet<>();
        if (!newSongs.stream().filter(song -> !songNames.add(song.getName()))
                  .collect(Collectors.toSet()).isEmpty()) {
            return "%s has the same song at least twice in this album.".formatted(username);
        }

        songs.addAll(newSongs);
        Album album = new Album(albumName,
                commandInput.getDescription(),
                username,
                newSongs,
                commandInput.getReleaseYear());
        currentArtist.getAlbums().add(album);

        // real time notification
        Notification notification = new Notification("New Album",
                "New Album from " + username + ".");
        notificationManager.notifyObservers(notification, username);

        return "%s has added new album successfully.".formatted(username);
    }

    /**
     * Remove album string.
     *
     * @param commandInput the command input
     * @return the string
     */
    public String removeAlbum(final CommandInput commandInput) {
        String username = commandInput.getUsername();
        String albumName = commandInput.getName();

        UserAbstract currentUser = getAbstractUser(username);

        if (currentUser == null) {
            return "The username %s doesn't exist.".formatted(username);
        } else if (!currentUser.userType().equals("artist")) {
            return "%s is not an artist.".formatted(username);
        }

        Artist currentArtist = (Artist) currentUser;
        Album searchedAlbum = currentArtist.getAlbum(albumName);
        if (searchedAlbum == null) {
            return "%s doesn't have an album with the given name.".formatted(username);
        }

        if (getAudioCollectionsStream().anyMatch(collection -> collection == searchedAlbum)) {
            return "%s can't delete this album.".formatted(username);
        }

        for (Song song : searchedAlbum.getSongs()) {
            if (getAudioCollectionsStream().anyMatch(collection -> collection.containsTrack(song))
                || getAudioFilesStream().anyMatch(audioFile -> audioFile == song)) {
                return "%s can't delete this album.".formatted(username);
            }
        }

        for (Song song: searchedAlbum.getSongs()) {
            users.forEach(user -> {
                user.getLikedSongs().remove(song);
                user.getPlaylists().forEach(playlist -> playlist.removeSong(song));
            });
            songs.remove(song);
        }

        currentArtist.getAlbums().remove(searchedAlbum);
        return "%s deleted the album successfully.".formatted(username);
    }

    /**
     * Add podcast string.
     *
     * @param commandInput the command input
     * @return the string
     */
    public String addPodcast(final CommandInput commandInput) {
        String username = commandInput.getUsername();
        String podcastName = commandInput.getName();
        UserAbstract currentUser = getAbstractUser(username);

        if (currentUser == null) {
            return "The username %s doesn't exist.".formatted(username);
        } else if (!currentUser.userType().equals("host")) {
            return "%s is not a host.".formatted(username);
        }

        Host currentHost = (Host) currentUser;
        if (currentHost.getPodcasts().stream()
            .anyMatch(podcast -> podcast.getName().equals(podcastName))) {
            return "%s has another podcast with the same name.".formatted(username);
        }

        List<Episode> episodes = commandInput.getEpisodes().stream()
                                             .map(episodeInput ->
                                                     new Episode(episodeInput.getName(),
                                                                 episodeInput.getDuration(),
                                                                 episodeInput.getDescription()))
                                             .collect(Collectors.toList());

        Set<String> episodeNames = new HashSet<>();
        if (!episodes.stream().filter(episode -> !episodeNames.add(episode.getName()))
                     .collect(Collectors.toSet()).isEmpty()) {
            return "%s has the same episode in this podcast.".formatted(username);
        }

        Podcast newPodcast = new Podcast(podcastName, username, episodes);
        currentHost.getPodcasts().add(newPodcast);
        podcasts.add(newPodcast);

        Notification notification = new Notification("New Podcast",
                "New Podcast from " + username + ".");
        notificationManager.notifyObservers(notification, username);

        return "%s has added new podcast successfully.".formatted(username);
    }


    /**
     * Remove podcast string.
     *
     * @param commandInput the command input
     * @return the string
     */
    public String removePodcast(final CommandInput commandInput) {
        String username = commandInput.getUsername();
        String podcastName = commandInput.getName();
        UserAbstract currentUser = getAbstractUser(username);

        if (currentUser == null) {
            return "The username %s doesn't exist.".formatted(username);
        } else if (!currentUser.userType().equals("host")) {
            return "%s is not a host.".formatted(username);
        }

        Host currentHost = (Host) currentUser;
        Podcast searchedPodcast = currentHost.getPodcast(podcastName);

        if (searchedPodcast == null) {
            return "%s doesn't have a podcast with the given name.".formatted(username);
        }

        if (getAudioCollectionsStream().anyMatch(collection -> collection == searchedPodcast)) {
            return "%s can't delete this podcast.".formatted(username);
        }

        currentHost.getPodcasts().remove(searchedPodcast);
        podcasts.remove(searchedPodcast);
        return "%s deleted the podcast successfully.".formatted(username);
    }

    /**
     * Add event string.
     *
     * @param commandInput the command input
     * @return the string
     */
    public String addEvent(final CommandInput commandInput) {
        String username = commandInput.getUsername();
        String eventName = commandInput.getName();

        UserAbstract currentUser = getAbstractUser(username);

        if (currentUser == null) {
            return "The username %s doesn't exist.".formatted(username);
        } else if (!currentUser.userType().equals("artist")) {
            return "%s is not an artist.".formatted(username);
        }

        Artist currentArtist = (Artist) currentUser;
        if (currentArtist.getEvent(eventName) != null) {
            return "%s has another event with the same name.".formatted(username);
        }

        String date = commandInput.getDate();

        if (!checkDate(date)) {
            return "Event for %s does not have a valid date.".formatted(username);
        }

        currentArtist.getEvents().add(new Event(eventName,
                                                commandInput.getDescription(),
                                                commandInput.getDate()));

        Notification notification = new Notification("New Event",
                "New Event from " + username + ".");
        notificationManager.notifyObservers(notification, username);

        return "%s has added new event successfully.".formatted(username);
    }

    /**
     * Remove event string.
     *
     * @param commandInput the command input
     * @return the string
     */
    public String removeEvent(final CommandInput commandInput) {
        String username = commandInput.getUsername();
        String eventName = commandInput.getName();

        UserAbstract currentUser = getAbstractUser(username);

        if (currentUser == null) {
            return "The username %s doesn't exist.".formatted(username);
        } else if (!currentUser.userType().equals("artist")) {
            return "%s is not an artist.".formatted(username);
        }

        Artist currentArtist = (Artist) currentUser;
        Event searchedEvent = currentArtist.getEvent(eventName);
        if (searchedEvent == null) {
            return "%s doesn't have an event with the given name.".formatted(username);
        }

        currentArtist.getEvents().remove(searchedEvent);
        return "%s deleted the event successfully.".formatted(username);
    }

    private boolean checkDate(final String date) {
        if (date.length() != dateStringLength) {
            return false;
        }

        List<String> dateElements = Arrays.stream(date.split("-", dateFormatSize)).toList();

        if (dateElements.size() != dateFormatSize) {
            return false;
        }

        int day = Integer.parseInt(dateElements.get(0));
        int month = Integer.parseInt(dateElements.get(1));
        int year = Integer.parseInt(dateElements.get(2));

        if (day < dateDayLowerLimit
            || (month == 2 && day > dateFebHigherLimit)
            || day > dateDayHigherLimit
            || month < dateMonthLowerLimit || month > dateMonthHigherLimit
            || year < dateYearLowerLimit || year > dateYearHigherLimit) {
            return false;
        }

        return true;
    }

    /**
     * Add merch string.
     *
     * @param commandInput the command input
     * @return the string
     */
    public String addMerch(final CommandInput commandInput) {
        String username = commandInput.getUsername();
        UserAbstract currentUser = getAbstractUser(username);

        if (currentUser == null) {
            return "The username %s doesn't exist.".formatted(username);
        } else if (!currentUser.userType().equals("artist")) {
            return "%s is not an artist.".formatted(username);
        }

        Artist currentArtist = (Artist) currentUser;
        if (currentArtist.getMerch().stream()
                         .anyMatch(merch -> merch.getName().equals(commandInput.getName()))) {
            return "%s has merchandise with the same name.".formatted(currentArtist.getUsername());
        } else if (commandInput.getPrice() < 0) {
            return "Price for merchandise can not be negative.";
        }

        currentArtist.getMerch().add(new Merchandise(commandInput.getName(),
                                                     commandInput.getDescription(),
                                                     commandInput.getPrice()));

        Notification notification = new Notification("New Merchandise",
                "New Merchandise from " + username + ".");
        notificationManager.notifyObservers(notification, username);

        return "%s has added new merchandise successfully.".formatted(username);
    }

    /**
     * Increase the artist merch revenue and share merch with a user
     *
     * @param user the user
     * @param merchandiseName the merchandise name
     * @return the command message
     */
    public String buyMerch(final User user, final String merchandiseName) {
        Artist accessedArtist = null;
        for (Artist artist: artists) {
            if (user.getCurrentPage() == artist.getPage()) {
                accessedArtist = artist;
                break;
            }
        }

        if (accessedArtist == null) {
            return "Cannot buy merch from this page.";
        }

        Merchandise targetedMerch = null;

        for (Merchandise merch: accessedArtist.getMerch()) {
            if (merch.getName().equals(merchandiseName)) {
                targetedMerch = merch;
                break;
            }
        }

        if (targetedMerch == null) {
            return "The merch %s doesn't exist.".formatted(merchandiseName);
        }

        ArrayList<String> purchasedMerch = user.getPurchasedMerch();
        purchasedMerch.add(merchandiseName);
        user.setPurchasedMerch(purchasedMerch);
        user.pay((double) targetedMerch.getPrice(), accessedArtist);

        return "%s has added new merch successfully.".formatted(user.getUsername());
    }

    /**
     * Add announcement string.
     *
     * @param commandInput the command input
     * @return the string
     */
    public String addAnnouncement(final CommandInput commandInput) {
        String username = commandInput.getUsername();
        String announcementName = commandInput.getName();
        String announcementDescription = commandInput.getDescription();

        UserAbstract currentUser = getAbstractUser(username);

        if (currentUser == null) {
            return "The username %s doesn't exist.".formatted(username);
        } else if (!currentUser.userType().equals("host")) {
            return "%s is not a host.".formatted(username);
        }

        Host currentHost = (Host) currentUser;
        Announcement searchedAnnouncement = currentHost.getAnnouncement(announcementName);
        if (searchedAnnouncement != null) {
            return "%s has already added an announcement with this name.";
        }

        currentHost.getAnnouncements().add(new Announcement(announcementName,
                                                            announcementDescription));

        Notification notification = new Notification("New Announcement",
                "New Announcement from " + username + ".");
        notificationManager.notifyObservers(notification, username);

        return "%s has successfully added new announcement.".formatted(username);
    }

    /**
     * Remove announcement string.
     *
     * @param commandInput the command input
     * @return the string
     */
    public String removeAnnouncement(final CommandInput commandInput) {
        String username = commandInput.getUsername();
        String announcementName = commandInput.getName();

        UserAbstract currentUser = getAbstractUser(username);

        if (currentUser == null) {
            return "The username %s doesn't exist.".formatted(username);
        } else if (!currentUser.userType().equals("host")) {
            return "%s is not a host.".formatted(username);
        }

        Host currentHost = (Host) currentUser;
        Announcement searchAnnouncement = currentHost.getAnnouncement(announcementName);
        if (searchAnnouncement == null) {
            return "%s has no announcement with the given name.".formatted(username);
        }

        currentHost.getAnnouncements().remove(searchAnnouncement);
        return "%s has successfully deleted the announcement.".formatted(username);
    }

    /**
     * Change page string.
     *
     * @param commandInput the command input
     * @return the string
     */
    public String changePage(final CommandInput commandInput) {
        String username = commandInput.getUsername();
        String nextPage = commandInput.getNextPage();

        UserAbstract currentUser = getAbstractUser(username);

        if (currentUser == null) {
            return "The username %s doesn't exist.".formatted(username);
        } else if (!currentUser.userType().equals("user")) {
            return "%s is not a normal user.".formatted(username);
        }

        User user = (User) currentUser;
        if (!user.isStatus()) {
            return "%s is offline.".formatted(user.getUsername());
        }

        switch (nextPage) {
            case "Home" -> {
                user.setCurrentPage(user.getHomePage());
                ArrayList<Page> pageHistory = user.getPageHistory();
                pageHistory.add(user.getHomePage());

                // history reset for backward
                user.setPageIndex(pageHistory.size() - 1);
                user.setPageHistory(pageHistory);
            }
            case "LikedContent" -> {
                user.setCurrentPage(user.getLikedContentPage());
                ArrayList<Page> pageHistory = user.getPageHistory();
                pageHistory.add(user.getLikedContentPage());

                // history reset for forward
                user.setPageIndex(pageHistory.size() - 1);
                user.setPageHistory(pageHistory);
            }
            case "Host" -> {
                return user.setLiveCreatorPage("host");
            }
            case "Artist" -> {
                return user.setLiveCreatorPage("artist");
            }
            default -> {
                return "%s is trying to access a non-existent page.".formatted(username);
            }
        }

        return "%s accessed %s successfully.".formatted(username, nextPage);
    }

    /**
     * Print current page string.
     *
     * @param commandInput the command input
     * @return the string
     */
    public String printCurrentPage(final CommandInput commandInput) {
        String username = commandInput.getUsername();
        UserAbstract currentUser = getAbstractUser(username);

        if (currentUser == null) {
            return "The username %s doesn't exist.".formatted(username);
        } else if (!currentUser.userType().equals("user")) {
            return "%s is not a normal user.".formatted(username);
        }

        User user = (User) currentUser;
        if (!user.isStatus()) {
            return "%s is offline.".formatted(user.getUsername());
        }

        return user.getCurrentPage().printCurrentPage();
    }

    /**
     * Switch status string.
     *
     * @param username the username
     * @return the string
     */
    public String switchStatus(final String username) {
        UserAbstract currentUser = getAbstractUser(username);

        if (currentUser == null) {
            return "The username %s doesn't exist.".formatted(username);
        }

        if (currentUser.userType().equals("user")) {
            ((User) currentUser).switchStatus();
            return username + " has changed status successfully.";
        } else {
            return username + " is not a normal user.";
        }
    }

    /**
     * Gets online users.
     *
     * @return the online users
     */
    public List<String> getOnlineUsers() {
        return users.stream().filter(User::isStatus).map(User::getUsername).toList();
    }

    private Stream<AudioCollection> getAudioCollectionsStream() {
        return users.stream().map(User::getPlayer)
                    .map(Player::getCurrentAudioCollection).filter(Objects::nonNull);
    }

    private Stream<AudioFile> getAudioFilesStream() {
        return users.stream().map(User::getPlayer)
                    .map(Player::getCurrentAudioFile).filter(Objects::nonNull);
    }

    /**
     * Gets top 5 album list.
     *
     * @return the top 5 album list
     */
    public List<String> getTop5AlbumList() {
        List<Album> albums = artists.stream().map(Artist::getAlbums)
                                    .flatMap(List::stream).toList();

        final Map<Album, Integer> albumLikes = new HashMap<>();
        albums.forEach(album -> albumLikes.put(album, album.getSongs().stream()
                                          .map(Song::getLikes).reduce(0, Integer::sum)));

        return albums.stream().sorted((o1, o2) -> {
            if ((int) albumLikes.get(o1) == albumLikes.get(o2)) {
                return o1.getName().compareTo(o2.getName());
            }
            return albumLikes.get(o2) - albumLikes.get(o1);
        }).limit(limit).map(Album::getName).toList();
    }

    /**
     * Gets top 5 artist list.
     *
     * @return the top 5 artist list
     */
    public List<String> getTop5ArtistList() {
        final Map<Artist, Integer> artistLikes = new HashMap<>();
        artists.forEach(artist -> artistLikes.put(artist, artist.getAllSongs().stream()
                                              .map(Song::getLikes).reduce(0, Integer::sum)));

        return artists.stream().sorted(Comparator.comparingInt(artistLikes::get).reversed())
                               .limit(limit).map(Artist::getUsername).toList();
    }

    /**
     * Gets top 5 songs.
     *
     * @return the top 5 songs
     */
    public List<String> getTop5Songs() {
        List<Song> sortedSongs = new ArrayList<>(songs);
        sortedSongs.sort(Comparator.comparingInt(Song::getLikes).reversed());
        List<String> topSongs = new ArrayList<>();
        int count = 0;
        for (Song song : sortedSongs) {
            if (count >= limit) {
                break;
            }
            topSongs.add(song.getName());
            count++;
        }
        return topSongs;
    }

    /**
     * Gets top 5 playlists.
     *
     * @return the top 5 playlists
     */
    public List<String> getTop5Playlists() {
        List<Playlist> sortedPlaylists = new ArrayList<>(getPlaylists());
        sortedPlaylists.sort(Comparator.comparingInt(Playlist::getFollowers)
                .reversed()
                .thenComparing(Playlist::getTimestamp, Comparator.naturalOrder()));
        List<String> topPlaylists = new ArrayList<>();
        int count = 0;
        for (Playlist playlist : sortedPlaylists) {
            if (count >= limit) {
                break;
            }
            topPlaylists.add(playlist.getName());
            count++;
        }
        return topPlaylists;
    }

    /**
     * Calls methods from the wrap classes to generate statistics
     *
     * @param statisticsStrategy the statistics strategy
     * @return the object node
     */
    public ObjectNode wrapped(final WrapStrategy statisticsStrategy) {
        return statisticsStrategy.generateStatistics();
    }

    /**
     * Updates the lists with delete and sort operations, also
     * calculating the song revenue for each active artist and
     * sorting the list with total revenue critter
     *
     * @param activeArtists the empty set
     */
    public void setActiveArtists(final ArrayList<Artist> activeArtists) {
        for (Artist artist: artists) {
            if (artist.getListens() > 0 || artist.totalRevenue() > 0) {
                activeArtists.add(artist);
            }
        }

        activeArtists.sort(Comparator.comparing(Artist::getUsername));

        int rank = 1;
        for (Artist artist: activeArtists) {
            artist.setRanking(rank++);
        }

        calculateSongRevenues(activeArtists);

        int len = activeArtists.size();
        for (int i = 0; i < len - 1; i++) {
            for (int j = i + 1; j < len; j++) {
                Artist artistI = activeArtists.get(i);
                Artist artistJ = activeArtists.get(j);
                if (artistI.totalRevenue() < artistJ.totalRevenue()) {
                    int rankI = artistI.getRanking();
                    artistI.setRanking(artistJ.getRanking());
                    artistJ.setRanking(rankI);
                    activeArtists.set(i, artistJ);
                    activeArtists.set(j, artistI);
                }
            }
        }
    }

    /**
     * Calculates revenues from premium listens and updates the recordedSongs lists
     *
     * @param artist the artist
     */
    public void calculatePremiumRevenues(final Artist artist) {
        for (User user: users) {
            ArrayList<Song> recordedSongs = user.getPlayer().getRecordedSongs();

            int totalSongs = recordedSongs.size();
            int start = resetedOnce;
            int songArtist = 0, songTotal = 0;
            double premiumRevenue;

            for (int i = 0; i < totalSongs; i++) {
                Song song = recordedSongs.get(i);
                if (song.isPremiumListen()) {
                    songTotal++;
                }

                if (start == resetedOnce && song.isPremiumListen()) {
                    start = i;
                }

                if (song.isPremiumListen() && song.getArtist().equals(artist.getUsername())) {
                    songArtist++;
                }

                if ((!song.isPremiumListen() || i == totalSongs - 1)
                        && songTotal != 0) {
                    premiumRevenue = premiumPrice * songArtist / songTotal;

                    double currentRevenue;
                    for (int j = start; j < i; j++) {
                        Song songItr = recordedSongs.get(j);
                        if (!songItr.getArtist().equals(artist.getUsername())) {
                            continue;
                        }

                        if (!songItr.isPremiumListen()) {
                            continue;
                        }

                        currentRevenue = songItr.getRevenue();
                        songItr.setRevenue(currentRevenue + premiumRevenue
                                / songTotal);
                    }

                    Song songItr = recordedSongs.get(i);
                    if (i == totalSongs - 1 && songItr.isPremiumListen()
                            && songItr.getArtist().equals(artist.getUsername())) {
                        currentRevenue = recordedSongs.get(i).getRevenue();
                        recordedSongs.get(i).setRevenue(currentRevenue + premiumRevenue
                                / songTotal);
                    }

                    artist.setSongRevenue(artist.getSongRevenue() + premiumRevenue);
                    songTotal = 0;
                    songArtist = 0;
                    start = resetedOnce;
                }
            }
        }
    }

    /**
     * Calculates revenues from ads
     *
     * @param artist the artist
     */
    public void calculateAdRevenues(final Artist artist) {
        for (User user: users) {
            ArrayList<Song> recordedSongs = user.getPlayer().getRecordedSongs();
            int totalSongs = recordedSongs.size();
            int start = 0;
            int songLast = 0, songArtist = 0;
            double adRevenue;

            for (int i = 0; i < totalSongs; i++) {
                Song song = recordedSongs.get(i);
                if (!song.isPremiumListen() && song.getArtist().equals(artist.getUsername())) {
                    songArtist++;
                }

                if (!song.isPremiumListen() && !song.getName().equals("Ad Break")) {
                    songLast++;
                }

                if (song.getName().equals("Ad Break") && songLast != 0) {
                    adRevenue = ((double) song.getPrice()) * songArtist / songLast;

                    double currentRevenue;
                    for (int j = start; j < i; j++) {
                        Song songItr = recordedSongs.get(j);
                        if (songItr.isPremiumListen() || songItr.getName().equals("Ad Break")) {
                            continue;
                        }

                        currentRevenue = recordedSongs.get(j).getRevenue();
                        recordedSongs.get(j).setRevenue(currentRevenue + adRevenue / songLast);
                    }

                    if (i == totalSongs - 1 && !recordedSongs.get(i).isPremiumListen()
                        && !recordedSongs.get(i).getName().equals("Ad Break")) {
                        currentRevenue = recordedSongs.get(i).getRevenue();
                        recordedSongs.get(i).setRevenue(currentRevenue + adRevenue / songLast);
                    }

                    artist.setSongRevenue(artist.getSongRevenue() + adRevenue);
                    start = i + 1;
                    songLast = 0;
                    songArtist = 0;
                }
            }
        }
    }

    /**
     * Updates song revenues and most profitable songs based on what
     * users listened.
     *
     * @param activeArtists the arraylist of active artists
     */
    public void calculateSongRevenues(final ArrayList<Artist> activeArtists) {
        for (Artist artist: activeArtists) {
            calculatePremiumRevenues(artist);
            calculateAdRevenues(artist);
        }

        for (Artist artist: activeArtists) {
            updateMostProfitableSong(artist);
        }
    }

    /**
     * If the song exist in artistSongs, it increases the revenue.
     * Otherwise, the song is added in the list.
     *
     * @param song the song
     * @param artistSongs the list of artist songs
     */
    public void addSongRevenue(final Song song, final ArrayList<Song> artistSongs) {
        for (Song songItr: artistSongs) {
            if (song.getName().equals(songItr.getName())) {
                songItr.setRevenue(songItr.getRevenue() + song.getRevenue());
                return;
            }
        }

        artistSongs.add(song);
    }

    /**
     * Based on the revenues from recordedSongs, the most profitable
     * song per artist is updated
     *
     * @param artist the artists
     */
    public void updateMostProfitableSong(final Artist artist) {
        ArrayList<Song> artistSongs = new ArrayList<>();

        for (User user: users) {
            ArrayList<Song> recordedSongs = user.getPlayer().getRecordedSongs();
            recordedSongs.removeIf(song -> song.getName().equals("Ad Break"));
            for (Song song: recordedSongs) {
                if (song.getArtist().equals(artist.getUsername())) {
                    addSongRevenue(song, artistSongs);
                }
            }
        }

        String mostProfitableSong = getMostProfitableSong(artistSongs);
        artist.setMostProfitableSong(mostProfitableSong);
    }

    /**
     * Calculates the most profitable song, considering it's revenue
     * Equality case, then lexicographical order
     *
     * @param artistSongs the list of the artist songs listened by users
     * @return the most profitable song
     */
    private String getMostProfitableSong(final ArrayList<Song> artistSongs) {
        String mostProfitableSong = "N/A";
        double biggestRevenue = 0;

        for (Song artistSong : artistSongs) {
            if (artistSong.getRevenue() > biggestRevenue) {
                mostProfitableSong = artistSong.getName();
                biggestRevenue = artistSong.getRevenue();
            } else if (artistSong.getRevenue() == biggestRevenue && biggestRevenue != 0
                        && artistSong.getName().compareTo(mostProfitableSong) < 0) {
                mostProfitableSong = artistSong.getName();
            }
        }
        return mostProfitableSong;
    }

    /**
     * Statistics performed on content creators at the end of the program
     *
     * @return the object node
     */
    public ObjectNode endProgram() {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode objectNode = objectMapper.createObjectNode(), nodeIter;
        ObjectNode resultNode = objectMapper.createObjectNode();

        objectNode.put("command", "endProgram");

        ArrayList<Artist> activeArtists = new ArrayList<>();

        setActiveArtists(activeArtists);

        for (Artist artist: activeArtists) {
            nodeIter = objectMapper.createObjectNode();
            nodeIter.put("merchRevenue",
                    Math.round(artist.getMerchRevenue() * roundTool) / roundTool);
            nodeIter.put("songRevenue",
                    Math.round(artist.getSongRevenue() * roundTool) / roundTool);
            nodeIter.put("ranking", artist.getRanking());
            nodeIter.put("mostProfitableSong", artist.getMostProfitableSong());

            resultNode.set(artist.getUsername(), nodeIter);
        }

        objectNode.put("result", resultNode);

        return objectNode;
    }

    /**
     * Adds the users from the library to the observers list
     *
     */
    public void initializeObservers() {
        for (User user: users) {
            notificationManager.addObserver(user);
        }
    }

    /**
     * Register the user subscription in subscriptions to get notifications
     * The target content creator should be found on user current page
     *
     * @param user the user that subscribes
     * @return the command message
     */
    public String subscribe(final User user) {
        Artist accessedArtist = null;
        for (Artist artist: artists) {
            if (user.getCurrentPage() == artist.getPage()) {
                accessedArtist = artist;
            }
        }

        Host accessedHost = null;
        for (Host host: hosts) {
            if (user.getCurrentPage() == host.getPage()) {
                accessedHost = host;
            }
        }

        if (accessedArtist == null && accessedHost == null) {
            return "To subscribe you need to be on the page of an artist or host.";
        }

        String subscription;
        if (accessedArtist == null) {
            subscription = accessedHost.getUsername();

            if (user.addSubscription(subscription)) {
                return "%s subscribed to %s successfully."
                        .formatted(user.getUsername(), subscription);
            }

            return "%s unsubscribed from %s successfully."
                    .formatted(user.getUsername(), subscription);
        }

        subscription = accessedArtist.getUsername();
        if (user.addSubscription(subscription)) {
            return "%s subscribed to %s successfully."
                    .formatted(user.getUsername(), subscription);
        }

        return "%s unsubscribed from %s successfully."
                .formatted(user.getUsername(), subscription);
    }

    /**
     * Loads an ad on the user player if possible
     *
     * @param user the user
     * @param price the ad price
     * @return the command message
     */
    public String adBreak(final User user, final int price) {
        Player player = user.getPlayer();
        if (player.getCurrentAudioFile() == null) {
            return "%s is not playing any music.".formatted(user.getUsername());
        }

        boolean music = player.getType().equals("song") || player.getType().equals("album");

        if (!music) {
            return "%s is not playing any music.".formatted(user.getUsername());
        }

        ad.setPrice(price);

        // player will not actually load the ad, so no paused variable change
        player.setSource(ad, "song");

        return "Ad inserted successfully.";
    }
}
