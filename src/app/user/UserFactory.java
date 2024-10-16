package app.user;

public final class UserFactory {
    private UserFactory() {
    }

    /**
     * Centralizes the logic of abstract user objects creation
     *
     * @param userType specialized abstract user
     * @param username the username of the user
     * @param age the age of the user
     * @param city the city of the user
     * @return the new user instance
     */
    public static UserAbstract createUser(final String userType,
                                  final String username, final int age, final String city) {
        return switch (userType) {
            case "user" -> new User(username, age, city);
            case "artist" -> new Artist(username, age, city);
            case "host" -> new Host(username, age, city);
            default -> throw new IllegalArgumentException("Not recognized user " + userType);
        };
    }
}
