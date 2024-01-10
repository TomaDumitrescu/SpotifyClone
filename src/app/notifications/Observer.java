package app.notifications;

public interface Observer {
    /**
     * The signature of a method that should store the received
     * notification in the classes that implements this interface
     *
     * @param notification the notification format
     */
    void update(Notification notification);
}
