package ru.pecom.android.bc.evt;

import ru.pecom.android.bc.DecodeResult;
import ru.pecom.android.bc.Decoder;
import ru.pecom.android.bc.hardware.honeywell.DecoderImpl;

/**
 * Обработчик событий сканирования
 * @see DecoderImpl
 */
public interface DecodeCompleteListener {
    void onSuccess(Decoder sender, DecodeResult result);
    void onFail(Decoder sender);
    void onComplete(Decoder sender);
}
