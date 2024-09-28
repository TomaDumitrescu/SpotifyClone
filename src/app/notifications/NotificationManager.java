package app.notifications;

import java.util.ArrayList;

import app.user.User;
import lombok.Getter;

@Getter
public class NotificationManager {
    private ArrayList<Observer> observers;
    public NotificationManager() {
        observers = new ArrayList<>();
    }

    /**
     * Adds a user to the list of observers that receive notifications
     *
     * @param observer the user
     */
    public void addObserver(final Observer observer) {
        observers.add(observer);
    }

    /**
     * Deletes a user from the notification systems in the case yhe user is
     * removed from the platform
     *
     * @param observer the user
     */
    public void rmObserver(final Observer observer) {
        if (!observers.contains(observer)) {
            return;
        }

        observers.remove(observer);
    }

    /**
     * For a specific notification of a content creator, the notification
     * manager notifies all observers that contain in the subscriptions list
     * the content creator name
     *
     * @param notification the notification object
     * @param username the content creator that notifies subscribers
     */
    public void notifyObservers(final Notification notification, final String username) {
        ArrayList<String> subscriptions;

        for (Observer observer: observers) {
            subscriptions = ((User) observer).getSubscriptions();

            if (subscriptions.contains(username)) {
                observer.update(notification);
            }
        }
    }
}
