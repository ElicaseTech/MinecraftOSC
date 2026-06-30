package tech.elicase.minecraftosc.core.osc;

import java.util.List;
import java.util.Objects;
import java.util.Collections;
import java.util.ArrayList;

/**
 * OSC Message
 */
public record OscMessage(String address, List<Object> arguments) implements OscPacket {

    public OscMessage {
        address = Objects.requireNonNull(address, "address");
        arguments = Collections.unmodifiableList(new ArrayList<>(Objects.requireNonNull(arguments, "arguments")));
    }
}
