package tech.elicase.minecraftosc.core.osc;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * 基于 OSC 1.0 规范的轻量解析器
 */
public final class OscParser {

    /**
     * 解析字节流并展开为消息列表
     */
    public List<OscMessage> parseMessages(byte[] data) throws OscParseException {
        Reader reader = new Reader(data, 0, data.length);
        OscPacket packet = parsePacket(reader);
        List<OscMessage> messages = new ArrayList<>();
        flatten(packet, messages);
        return List.copyOf(messages);
    }

    private OscPacket parsePacket(Reader reader) throws OscParseException {
        String marker = reader.readString();
        if ("#bundle".equals(marker)) {
            long timeTag = reader.readLong();
            List<OscPacket> elements = new ArrayList<>();
            while (reader.hasRemaining()) {
                int size = reader.readInt();
                if (size <= 0 || size > reader.remaining()) {
                    throw new OscParseException("无效的 OSC bundle 元素长度: " + size);
                }
                elements.add(parsePacket(reader.readNested(size)));
            }
            return new OscBundle(timeTag, elements);
        }

        if (!marker.startsWith("/")) {
            throw new OscParseException("OSC 地址必须以 / 开头: " + marker);
        }

        String typeTags = reader.readString();
        if (!typeTags.startsWith(",")) {
            throw new OscParseException("OSC 类型标签必须以逗号开头");
        }

        List<Object> arguments = new ArrayList<>();
        for (int index = 1; index < typeTags.length(); index++) {
            arguments.add(readArgument(typeTags.charAt(index), reader));
        }
        return new OscMessage(marker, arguments);
    }

    private Object readArgument(char typeTag, Reader reader) throws OscParseException {
        return switch (typeTag) {
            case 'i' -> reader.readInt();
            case 'f' -> reader.readFloat();
            case 's' -> reader.readString();
            case 'h', 't' -> reader.readLong();
            case 'd' -> reader.readDouble();
            case 'b' -> reader.readBlob();
            case 'T' -> Boolean.TRUE;
            case 'F' -> Boolean.FALSE;
            case 'N' -> null;
            default -> throw new OscParseException("暂不支持的 OSC 类型标签: " + typeTag);
        };
    }

    private void flatten(OscPacket packet, List<OscMessage> messages) {
        if (packet instanceof OscMessage message) {
            messages.add(message);
            return;
        }

        OscBundle bundle = (OscBundle) packet;
        for (OscPacket element : bundle.elements()) {
            flatten(element, messages);
        }
    }

    /**
     * 受限字节读取器
     */
    private static final class Reader {

        private final byte[] data;
        private final int limit;
        private int position;

        private Reader(byte[] data, int offset, int length) {
            this.data = data;
            this.position = offset;
            this.limit = offset + length;
        }

        private boolean hasRemaining() {
            return position < limit;
        }

        private int remaining() {
            return limit - position;
        }

        private Reader readNested(int size) {
            Reader nested = new Reader(data, position, size);
            position += size;
            return nested;
        }

        private int readInt() throws OscParseException {
            ensureRemaining(4);
            int value = ((data[position] & 0xFF) << 24)
                    | ((data[position + 1] & 0xFF) << 16)
                    | ((data[position + 2] & 0xFF) << 8)
                    | (data[position + 3] & 0xFF);
            position += 4;
            return value;
        }

        private long readLong() throws OscParseException {
            ensureRemaining(8);
            long value = 0L;
            for (int index = 0; index < 8; index++) {
                value = (value << 8) | (data[position + index] & 0xFFL);
            }
            position += 8;
            return value;
        }

        private float readFloat() throws OscParseException {
            return Float.intBitsToFloat(readInt());
        }

        private double readDouble() throws OscParseException {
            return Double.longBitsToDouble(readLong());
        }

        private byte[] readBlob() throws OscParseException {
            int blobLength = readInt();
            if (blobLength < 0) {
                throw new OscParseException("Blob 长度不能为负数");
            }
            ensureRemaining(blobLength);
            byte[] blob = new byte[blobLength];
            System.arraycopy(data, position, blob, 0, blobLength);
            position += blobLength;
            skipPadding(blobLength);
            return blob;
        }

        private String readString() throws OscParseException {
            int start = position;
            while (position < limit && data[position] != 0) {
                position++;
            }
            if (position >= limit) {
                throw new OscParseException("OSC 字符串缺少结尾空字节");
            }

            String value = new String(data, start, position - start, StandardCharsets.UTF_8);
            int lengthWithTerminator = (position - start) + 1;
            position++;
            skipPadding(lengthWithTerminator);
            return value;
        }

        private void skipPadding(int unpaddedLength) {
            int padding = (4 - (unpaddedLength % 4)) % 4;
            position += padding;
        }

        private void ensureRemaining(int size) throws OscParseException {
            if (position + size > limit) {
                throw new OscParseException("OSC 数据长度不足，期望 " + size + " 字节，剩余 " + remaining() + " 字节");
            }
        }
    }
}
