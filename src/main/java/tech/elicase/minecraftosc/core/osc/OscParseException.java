package tech.elicase.minecraftosc.core.osc;

/**
 * OSC 解析异常
 */
public final class OscParseException extends Exception {

    public OscParseException(String message) {
        super(message);
    }

    public OscParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
