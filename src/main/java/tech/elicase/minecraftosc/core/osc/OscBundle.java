package tech.elicase.minecraftosc.core.osc;

import java.util.List;
import java.util.Objects;

/**
 * OSC Bundle
 */
public record OscBundle(long timeTag, List<OscPacket> elements) implements OscPacket {

    public OscBundle {
        elements = List.copyOf(Objects.requireNonNull(elements, "elements"));
    }
}
