package app.user.notifications;

import lombok.Getter;

@Getter
public class Notification {
    private String description;
    private String name;

    public Notification(final String name, final String description) {
        this.name = name;
        this.description = description;
    }
}
