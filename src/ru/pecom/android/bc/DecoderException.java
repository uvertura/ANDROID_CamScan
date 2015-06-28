package ru.pecom.android.bc;

/**
 * Created by volhonskiy.ro on 28.06.15.
 */
public class DecoderException extends Exception {
    public DecoderException(String detailMessage) {
        super(detailMessage);
    }

    public DecoderException(String message, Throwable t) {
        super(message == null || message.isEmpty() ? t.getMessage() : message, t);
    }

    public DecoderException(Throwable t) {
        super(t.getMessage(), t);
    }
}
