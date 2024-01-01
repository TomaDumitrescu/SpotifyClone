package app.pages;

import app.audio.Collections.Podcast;
import app.user.Announcement;

import java.util.List;

/**
 * The type Host page.
 */
public final class HostPage implements Page {
    private List<Podcast> podcasts;
    private List<Announcement> announcements;

    /**
     * Instantiates a new Host page.
     *
     * @param podcasts the podcasts
     * @param announcements the announcements
     */
    public HostPage(final List<Podcast> podcasts,
                    final List<Announcement> announcements) {
        this.podcasts = podcasts;
        this.announcements = announcements;
    }

    @Override
    public String printCurrentPage() {
        return "Podcasts:\n\t%s\n\nAnnouncements:\n\t%s"
               .formatted(podcasts.stream().map(podcast -> "%s:\n\t%s\n"
                          .formatted(podcast.getName(),
                                     podcast.getEpisodes().stream().map(episode -> "%s - %s"
                          .formatted(episode.getName(), episode.getDescription())).toList()))
                          .toList(),
                          announcements.stream().map(announcement -> "%s:\n\t%s\n"
                          .formatted(announcement.getName(), announcement.getDescription()))
                          .toList());
    }
}
