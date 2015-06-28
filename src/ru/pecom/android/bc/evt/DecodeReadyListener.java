package ru.pecom.android.bc.evt;

import com.honeywell.decodemanager.DecodeManager;
import ru.pecom.android.bc.hardware.honeywell.DecoderImpl;

/**
 * Обработчие события успешной инициализации оборудования.
 * @see ru.pecom.android.bc.Decoder
 * @see ru.pecom.android.bc.Decoder#addReadyHandler(DecodeReadyListener)
 */
public interface DecodeReadyListener {
    /**
     * Метод вызываемый после успешной инициализации оборудования
     * @param sender
     */
    void onDecodeReady(DecodeManager sender);
}
