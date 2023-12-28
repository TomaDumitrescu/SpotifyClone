package fileio.input;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public final class UserInput {
    private String username;
    private int age;
    private String city;

    public UserInput() {
    }

    @Override
    public String toString() {
        return "UserInput{"
                + "username='" + username + '\''
                + ", age=" + age
                + ", city='" + city + '\''
                + '}';
    }
}
