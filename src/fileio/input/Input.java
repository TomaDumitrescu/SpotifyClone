package fileio.input;

import java.util.ArrayList;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public final class Input {
    private LibraryInput library;
    private ArrayList<UserInput> users;
    private ArrayList<CommandInput> commands;

    public Input() {
    }

    @Override
    public String toString() {
        return "AppInput{"
                + "library=" + library
                + ", users=" + users
                + ", commands=" + commands
                + '}';
    }
}
