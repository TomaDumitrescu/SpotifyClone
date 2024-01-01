package app.pages;

import app.audio.Collections.Album;
import app.user.Event;
import app.user.Merchandise;

import java.util.List;

/**
 * The type Artist page.
 */
public final class ArtistPage implements Page {
    private List<Album> albums;
    private List<Merchandise> merch;
    private List<Event> events;

    /**
     * Instantiates a new Artist page.
     *
     * @param albums the albums
     * @param merch the merch
     * @param events the events
     */
    public ArtistPage(final List<Album> albums, final List<Merchandise> merch,
                      final List<Event> events) {
        this.albums = albums;
        this.merch = merch;
        this.events = events;
    }

    @Override
    public String printCurrentPage() {
        return "Albums:\n\t%s\n\nMerch:\n\t%s\n\nEvents:\n\t%s"
                .formatted(albums.stream().map(Album::getName).toList(),
                           merch.stream().map(merchItem -> "%s - %d:\n\t%s"
                                .formatted(merchItem.getName(),
                                           merchItem.getPrice(),
                                           merchItem.getDescription()))
                                .toList(),
                           events.stream().map(event -> "%s - %s:\n\t%s"
                                 .formatted(event.getName(),
                                            event.getDate(),
                                            event.getDescription()))
                                 .toList());
    }
}
