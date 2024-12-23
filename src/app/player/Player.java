package app.player;

import app.Admin;
import app.audio.Collections.AudioCollection;
import app.audio.Collections.Podcast;
import app.audio.Files.AudioFile;
import app.audio.Files.Episode;
import app.audio.Files.Song;
import app.audio.LibraryEntry;
import app.audio.RecordedEntry;
import app.user.Artist;
import app.utils.Enums;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * The type Player.
 */
public final class Player {
    private Enums.RepeatMode repeatMode;
    private boolean shuffle;
    private boolean paused;
    @Getter
    private PlayerSource source;
    @Getter
    private String type;
    private final int skipTime = 90;
    private ArrayList<PodcastBookmark> bookmarks = new ArrayList<>();
    @Getter
    private HashMap<RecordedEntry, Integer> recordedEntries = new HashMap<>();
    @Getter
    private HashMap<String, Integer> listenedGenres = new HashMap<>();
    @Getter
    private ArrayList<Song> recordedSongs = new ArrayList<>();
    @Getter
    @Setter
    private boolean premiumListen = false;
    @Setter
    private Song ad;


    /**
     * Instantiates a new Player.
     */
    public Player() {
        this.repeatMode = Enums.RepeatMode.NO_REPEAT;
        this.paused = true;
        this.ad = null;
    }

    /**
     * Stop.
     */
    public void stop() {
        if ("podcast".equals(this.type)) {
            bookmarkPodcast();
        }

        repeatMode = Enums.RepeatMode.NO_REPEAT;
        paused = true;
        source = null;
        shuffle = false;
    }

    private void bookmarkPodcast() {
        if (source != null && source.getAudioFile() != null) {
            PodcastBookmark currentBookmark =
                    new PodcastBookmark(source.getAudioCollection().getName(),
                                        source.getIndex(),
                                        source.getDuration());
            bookmarks.removeIf(bookmark -> bookmark.getName().equals(currentBookmark.getName()));
            bookmarks.add(currentBookmark);
        }
    }

    /**
     * Create source player source.
     *
     * @param type      the type
     * @param entry     the entry
     * @param bookmarks the bookmarks
     * @return the player source
     */
    public static PlayerSource createSource(final String type,
                                            final LibraryEntry entry,
                                            final List<PodcastBookmark> bookmarks) {
        if ("song".equals(type)) {
            return new PlayerSource(Enums.PlayerSourceType.LIBRARY, (AudioFile) entry);
        } else if ("playlist".equals(type)) {
            return new PlayerSource(Enums.PlayerSourceType.PLAYLIST, (AudioCollection) entry);
        } else if ("podcast".equals(type)) {
            return createPodcastSource((AudioCollection) entry, bookmarks);
        } else if ("album".equals(type)) {
            return new PlayerSource(Enums.PlayerSourceType.ALBUM, (AudioCollection) entry);
        }

        return null;
    }

    private static PlayerSource createPodcastSource(final AudioCollection collection,
                                                    final List<PodcastBookmark> bookmarks) {
        for (PodcastBookmark bookmark : bookmarks) {
            if (bookmark.getName().equals(collection.getName())) {
                return new PlayerSource(Enums.PlayerSourceType.PODCAST, collection, bookmark);
            }
        }
        return new PlayerSource(Enums.PlayerSourceType.PODCAST, collection);
    }

    /**
     * Sets source.
     *
     * @param entry      the entry
     * @param sourceType the sourceType
     */
    public void setSource(final LibraryEntry entry, final String sourceType) {
        if ("podcast".equals(this.type)) {
            bookmarkPodcast();
        }

        if (entry.getName().equals("Ad Break")) {
            // waiting state
            ad = (Song) entry;
            return;
        } else {
            // ads can be overwritten by other loads
            ad = null;
        }

        this.type = sourceType;
        this.source = createSource(sourceType, entry, bookmarks);

        addRecord();

        this.repeatMode = Enums.RepeatMode.NO_REPEAT;
        this.shuffle = false;
        this.paused = true;
    }

    /**
     * Pause.
     */
    public void pause() {
        paused = !paused;
    }

    /**
     * Shuffle.
     *
     * @param seed the seed
     */
    public void shuffle(final Integer seed) {
        if (seed != null) {
            source.generateShuffleOrder(seed);
        }

        if (source.getType() == Enums.PlayerSourceType.PLAYLIST
            || source.getType() == Enums.PlayerSourceType.ALBUM) {
            shuffle = !shuffle;
            if (shuffle) {
                source.updateShuffleIndex();
            }
        }
    }

    /**
     * Repeat enums . repeat mode.
     *
     * @return the enums . repeat mode
     */
    public Enums.RepeatMode repeat() {
        if (repeatMode == Enums.RepeatMode.NO_REPEAT) {
            if (source.getType() == Enums.PlayerSourceType.LIBRARY) {
                repeatMode = Enums.RepeatMode.REPEAT_ONCE;
            } else {
                repeatMode = Enums.RepeatMode.REPEAT_ALL;
            }
        } else {
            if (repeatMode == Enums.RepeatMode.REPEAT_ONCE) {
                repeatMode = Enums.RepeatMode.REPEAT_INFINITE;
            } else {
                if (repeatMode == Enums.RepeatMode.REPEAT_ALL) {
                    repeatMode = Enums.RepeatMode.REPEAT_CURRENT_SONG;
                } else {
                    repeatMode = Enums.RepeatMode.NO_REPEAT;
                }
            }
        }

        return repeatMode;
    }

    /**
     * Simulate player.
     *
     * @param time the time
     */
    public void simulatePlayer(final int time) {
        int elapsedTime = time;

        if (!paused) {
            while (elapsedTime >= source.getDuration()) {
                elapsedTime -= source.getDuration();
                next();

                if (paused) {
                    break;
                }
            }
            if (!paused) {
                source.skip(-elapsedTime);
            }
        }
    }

    /**
     * Next.
     */
    public void next() {
        if (ad != null) {
            /* the current song finished, then record the ad,
             * since it can be interrupted after debuting
             */
            recordAd();
        }

        paused = source.setNextAudioFile(repeatMode, shuffle);
        if (repeatMode == Enums.RepeatMode.REPEAT_ONCE) {
            repeatMode = Enums.RepeatMode.NO_REPEAT;
        }

        if (source.getDuration() == 0 && paused) {
            stop();
        } else {
            addRecord();
        }
    }

    /**
     * Prev.
     */
    public void prev() {
        source.setPrevAudioFile(shuffle);
        addRecord();
        paused = false;
    }

    private void skip(final int duration) {
        boolean changed = source.skip(duration);
        if (changed) {
            addRecord();
        }

        paused = false;
    }

    /**
     * Skip next.
     */
    public void skipNext() {
        if (source.getType() == Enums.PlayerSourceType.PODCAST) {
            skip(-skipTime);
        }
    }

    /**
     * Skip prev.
     */
    public void skipPrev() {
        if (source.getType() == Enums.PlayerSourceType.PODCAST) {
            skip(skipTime);
        }
    }

    /**
     * Gets current audio file.
     *
     * @return the current audio file
     */
    public AudioFile getCurrentAudioFile() {
        if (source == null) {
            return null;
        }
        return source.getAudioFile();
    }

    /**
     * Gets current audio collection.
     *
     * @return the current audio collection
     */
    public AudioCollection getCurrentAudioCollection() {
        if (source == null) {
            return null;
        }
        return source.getAudioCollection();
    }

    /**
     * Gets paused.
     *
     * @return the paused
     */
    public boolean getPaused() {
        return paused;
    }

    /**
     * Gets shuffle.
     *
     * @return the shuffle
     */
    public boolean getShuffle() {
        return shuffle;
    }

    /**
     * Gets stats.
     *
     * @return the stats
     */
    public PlayerStats getStats() {
        String filename = "";
        int duration = 0;
        if (source != null && source.getAudioFile() != null) {
            filename = source.getAudioFile().getName();
            duration = source.getDuration();
        } else {
            stop();
        }

        return new PlayerStats(filename, duration, repeatMode, shuffle, paused);
    }

    /**
     * Sets a recorded entry to the list
     *
     * @param collection if the entry is collection or file
     */
    public void setRecord(final boolean collection) {
        if (source == null) {
            return;
        }

        RecordedEntry rec;

        if (collection) {
            AudioCollection current = getCurrentAudioCollection();

            if (current == null || current.getName().equals("Buy Premium")) {
                return;
            }

            String productType;

            if (type.equals("album")) {
                productType = "album";
            } else if (type.equals("podcast")) {
                productType = "podcast";
            } else {
                if (!type.equals("playlist")) {
                    throw new RuntimeException("Error. No valid audio collection type!");
                }

                productType = "playlist";
            }

            rec = new RecordedEntry(current.getName(), current.getOwner(), productType);
            recordedEntries.put(rec, recordedEntries.getOrDefault(rec, 0) + 1);

            return;
        }

        AudioFile current = getCurrentAudioFile();
        if (current == null) {
            return;
        }

        if (type.equals("song") || type.equals("album")) {
            Song song = (Song) current;
            rec = new RecordedEntry(song.getName(), song.getArtist(), "song");
            rec.setGenre(song.getGenre());

            Song copySong = new Song(song.getName(), song.getDuration(), song.getAlbum(),
                    song.getTags(), song.getLyrics(), song.getGenre(),
                    song.getReleaseYear(), song.getArtist());

            // useful for determining correct formula for monetization
            copySong.setPremiumListen(premiumListen);
            copySong.setPrice(song.getPrice());

            recordedSongs.add(copySong);

            // ads are not part of current statistical populations
            if (song.getName().equals("Ad Break")) {
                return;
            }

            listenedGenres.put(song.getGenre(),
                    listenedGenres.getOrDefault(song.getGenre(), 0) + 1);
            List<Artist> artists = Admin.getInstance().getArtists();

            for (Artist artist: artists) {
                if (artist.getUsername().equalsIgnoreCase(song.getArtist())) {
                    // 1 for the song, 1 for the album
                    artist.setListens(artist.getListens() + 2);
                    break;
                }
            }

            Admin.getInstance().setArtists(artists);
            recordedEntries.put(rec, recordedEntries.getOrDefault(rec, 0) + 1);

            if (getCurrentAudioCollection() == null) {
                rec = new RecordedEntry(song.getAlbum(), song.getArtist(), "album");
                recordedEntries.put(rec, recordedEntries.getOrDefault(rec, 0) + 1);
            }
        } else  {
            if (!type.equals("episode") && !type.equals("podcast")) {
                throw new RuntimeException("Error. No valid audio file type!");
            }

            Episode episode = (Episode) current;
            Podcast podcast = (Podcast) getCurrentAudioCollection();

            if (podcast == null) {
                throw new RuntimeException("Error. No podcast for the episode!");
            }

            rec = new RecordedEntry(episode.getName(), podcast.getOwner(), "episode");
            recordedEntries.put(rec, recordedEntries.getOrDefault(rec, 0) + 1);
        }
    }

    /**
     * Adds a recorded entry to the list, each object listened from a collection
     * incrementing the collection listens
     *
     */
    public void addRecord() {
        // listens count for the audio file and collection every time
        setRecord(true);
        setRecord(false);
    }

    /**
     * Records an ad, separately from the audio files recording
     *
     */
    public void recordAd() {
        Song copySong = new Song(ad.getName(), ad.getDuration(), ad.getAlbum(),
                ad.getTags(), ad.getLyrics(), ad.getGenre(),
                ad.getReleaseYear(), ad.getArtist());
        copySong.setPrice(ad.getPrice());

        // by default, ads will be listened in free mode
        recordedSongs.add(copySong);
    }
}
