package ru.pecom.android.bc;

import ru.pecom.android.bc.evt.DecodeCompleteListener;
import ru.pecom.android.bc.evt.DecodeReadyListener;

/**
 * Created by volhonskiy.ro on 28.06.15.
 */
public interface Decoder {
    public void init() throws DecoderException;
    public void destroy() throws DecoderException;
    public void decode(int timeout) throws DecoderException;

    public boolean isInitialized();
    public boolean isReadyToScan();

    public void addStandard(STANDARD standard) throws DecoderException;

    public void addReadyHandler(DecodeReadyListener handler);
    public void addCompleteHandler(DecodeCompleteListener handler);
}
