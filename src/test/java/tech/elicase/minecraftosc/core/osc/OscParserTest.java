package tech.elicase.minecraftosc.core.osc;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OscParserTest {

    private final OscParser parser = new OscParser();

    @Test
    void parsesSingleMessageWithCommonTypes() throws Exception {
        byte[] payload = oscMessage("/vrchat/chat", ",sifTFb", "Hello", 42, 1.5f, true, false, new byte[]{1, 2, 3});

        List<OscMessage> messages = parser.parseMessages(payload);

        assertEquals(1, messages.size());
        OscMessage message = messages.get(0);
        assertEquals("/vrchat/chat", message.address());
        assertEquals("Hello", message.arguments().get(0));
        assertEquals(42, message.arguments().get(1));
        assertEquals(1.5f, message.arguments().get(2));
        assertEquals(Boolean.TRUE, message.arguments().get(3));
        assertEquals(Boolean.FALSE, message.arguments().get(4));
        assertArrayEquals(new byte[]{1, 2, 3}, assertInstanceOf(byte[].class, message.arguments().get(5)));
    }

    @Test
    void flattensBundleIntoMessages() throws Exception {
        byte[] first = oscMessage("/vrchat/chat", ",s", "One");
        byte[] second = oscMessage("/chatbox/input", ",sT", "Two", true);
        byte[] bundle = oscBundle(first, second);

        List<OscMessage> messages = parser.parseMessages(bundle);

        assertEquals(2, messages.size());
        assertEquals("/vrchat/chat", messages.get(0).address());
        assertEquals("/chatbox/input", messages.get(1).address());
        assertEquals("Two", messages.get(1).arguments().get(0));
        assertEquals(Boolean.TRUE, messages.get(1).arguments().get(1));
    }

    private static byte[] oscBundle(byte[]... packets) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        writeOscString(output, "#bundle");
        DataOutputStream data = new DataOutputStream(output);
        data.writeLong(1L);
        for (byte[] packet : packets) {
            data.writeInt(packet.length);
            data.write(packet);
        }
        return output.toByteArray();
    }

    private static byte[] oscMessage(String address, String tags, Object... arguments) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        writeOscString(output, address);
        writeOscString(output, tags);
        for (int index = 0; index < arguments.length; index++) {
            char tag = tags.charAt(index + 1);
            Object argument = arguments[index];
            switch (tag) {
                case 's' -> writeOscString(output, (String) argument);
                case 'i' -> new DataOutputStream(output).writeInt((Integer) argument);
                case 'f' -> new DataOutputStream(output).writeFloat((Float) argument);
                case 'T', 'F', 'N' -> {
                    // 无负载
                }
                case 'b' -> writeBlob(output, (byte[]) argument);
                default -> throw new IllegalArgumentException("unsupported tag " + tag);
            }
        }
        return output.toByteArray();
    }

    private static void writeBlob(ByteArrayOutputStream output, byte[] bytes) throws IOException {
        DataOutputStream data = new DataOutputStream(output);
        data.writeInt(bytes.length);
        data.write(bytes);
        padTo4Bytes(output, bytes.length);
    }

    private static void writeOscString(ByteArrayOutputStream output, String value) {
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        output.writeBytes(bytes);
        output.write(0);
        padTo4Bytes(output, bytes.length + 1);
    }

    private static void padTo4Bytes(ByteArrayOutputStream output, int rawLength) {
        int padding = (4 - (rawLength % 4)) % 4;
        for (int index = 0; index < padding; index++) {
            output.write(0);
        }
    }
}
