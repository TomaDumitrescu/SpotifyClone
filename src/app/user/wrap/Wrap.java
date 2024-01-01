package app.user.wrap;

import com.fasterxml.jackson.databind.node.ObjectNode;

public interface Wrap {
    /**
     * Used to generate statics depending on the user type
     *
     * @return the object node
     */
    ObjectNode generateStatistics();
}
