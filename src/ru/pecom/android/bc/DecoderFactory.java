package ru.pecom.android.bc;

import android.content.Context;

/**
 * Created by volhonskiy.ro on 28.06.15.
 */
public class DecoderFactory {
    public static enum HARDWARE{
        HONEYWELL
    }

    private DecoderFactory() {
    }

    public static Decoder getDecoder(Context ctx, HARDWARE hardware){
        switch(hardware){
            case HONEYWELL:
                return new ru.pecom.android.bc.hardware.honeywell.DecoderImpl(ctx);
            default:
                return null;
        }
    }
}
