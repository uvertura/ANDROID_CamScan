package ru.pecom.android.bc.hardware.honeywell;

import android.content.Context;
import android.os.*;
import com.honeywell.decodemanager.DecodeManager;
import com.honeywell.decodemanager.barcode.CommonDefine;
import ru.pecom.android.bc.DecodeResult;
import ru.pecom.android.bc.Decoder;
import ru.pecom.android.bc.DecoderException;
import ru.pecom.android.bc.STANDARD;
import ru.pecom.android.bc.evt.DecodeCompleteListener;
import ru.pecom.android.bc.evt.DecodeReadyListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by volhonskiy.ro on 24.06.15.
 */
public class DecoderImpl implements Decoder{
    private final Object managerSync = new Object();
    private final List<Integer> standards = new ArrayList<>();
    private final DecoderImpl self = this;

    protected DecodeManager manager = null;
    protected Context ctx;
    protected boolean readyToScan = false;

    private List<DecodeReadyListener> readyEventList = new ArrayList<>();
    private List<DecodeCompleteListener> completeEventList = new ArrayList<>();

    public DecoderImpl(Context ctx) {
        this.ctx = ctx;
    }

    @Override
    public void init() {
        synchronized (managerSync) {
            if (null == manager)
                manager = new DecodeManager(ctx, decodeHandler);
        }
    }

    @Override
    public void destroy() throws DecoderException {
        synchronized (managerSync) {
            if (null == manager) return;

            try {
                manager.cancelDecode();
                try {
                    manager.release();
                    readyToScan = false;
                } catch (IOException ex) {
                    throw new DecoderException(ex);
                }
                manager = null;
            }catch(RemoteException ex){
                throw new DecoderException(ex);
            }
        }

    }

    @Override
    public void decode(int timeout) throws DecoderException{
        if(!isReadyToScan())
            throw new DecoderException("Hardware is not ready to scan.");

        try {
            manager.doDecode(timeout);
        } catch (RemoteException ex) {
            throw new DecoderException(ex);
        }
    }

    @Override
    public boolean isInitialized(){
        synchronized (managerSync){
            return (null == manager);
        }
    }

    @Override
    public boolean isReadyToScan() {
        return readyToScan;
    }

    @Override
    public void addReadyHandler(DecodeReadyListener handler){
        readyEventList.add(handler);
    }

    @Override
    public void addCompleteHandler(DecodeCompleteListener handler){
        completeEventList.add(handler);
    }

    @Override
    public void addStandard(STANDARD standard) throws DecoderException {
        int symbologyID = standard2SymbologyID(standard);

        standards.add(symbologyID);
        if(isInitialized()) {
            try {
                manager.enableSymbology(symbologyID);
            } catch (RemoteException ex) {
                throw new DecoderException(ex);
            }
        }
    }

    private Handler decodeHandler =  new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case DecodeManager.MESSAGE_DECODER_COMPLETE:
                    for(DecodeCompleteListener listener : completeEventList){
                        listener.onComplete(self);
                    }

                    DecodeResult result = new DecodeResult();

                    Parcel p = Parcel.obtain();

                    com.honeywell.decodemanager.barcode.DecodeResult origResult = (com.honeywell.decodemanager.barcode.DecodeResult)msg.obj;
                    origResult.writeToParcel(p, Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
                    result.setData(p.createByteArray());

                    for(DecodeCompleteListener listener : completeEventList){
                        listener.onSuccess(self, result);
                    }
                    break;

                case DecodeManager.MESSAGE_DECODER_FAIL: {
                    for(DecodeCompleteListener listener : completeEventList){
                        listener.onComplete(self);
                    }
                    //TODO: Добавить информацию о причине неудачи
                    for(DecodeCompleteListener listener : completeEventList){
                        listener.onFail(self);
                    }
                }
                break;
                case DecodeManager.MESSAGE_DECODER_READY:
                    //TODO: инициализировать поддерживаемые ШК

                    try {
                        for (int symbologyID : standards) {
                            manager.enableSymbology(symbologyID);
                        }
                    }catch(RemoteException ex){
                        for(DecodeCompleteListener listener : completeEventList){
                            //TODO: Добавить причину
                            listener.onFail(self);
                        }
                    }

                    readyToScan = true;
                    for(DecodeReadyListener listener : readyEventList){
                        listener.onDecodeReady(manager);
                    }
                break;
                default:
                    super.handleMessage(msg);
                    break;
            }
        }
    };

    protected static int standard2SymbologyID(STANDARD standard) throws DecoderException{
        int symbologyID;
        switch(standard){
            case ALL:
                symbologyID = CommonDefine.SymbologyID.SYM_ALL;
                break;
            case AZTEC:
                symbologyID = CommonDefine.SymbologyID.SYM_AZTEC;
                break;
            case CODABAR:
                symbologyID = CommonDefine.SymbologyID.SYM_CODABAR;
                break;
            case CODE11:
                symbologyID = CommonDefine.SymbologyID.SYM_CODE11;
                break;
            case CODE128:
                symbologyID = CommonDefine.SymbologyID.SYM_CODE128;
                break;
            case CODE39:
                symbologyID = CommonDefine.SymbologyID.SYM_CODE39;
                break;
            case CODE49:
                symbologyID = CommonDefine.SymbologyID.SYM_CODE49;
                break;
            case CODE93:
                symbologyID = CommonDefine.SymbologyID.SYM_CODE93;
                break;
            case COMPOSITE:
                symbologyID = CommonDefine.SymbologyID.SYM_COMPOSITE;
                break;
            case DATAMATRIX:
                symbologyID = CommonDefine.SymbologyID.SYM_DATAMATRIX;
                break;
            case EAN8:
                symbologyID = CommonDefine.SymbologyID.SYM_EAN8;
                break;
            case EAN13:
                symbologyID = CommonDefine.SymbologyID.SYM_EAN13;
                break;
            case INT25:
                symbologyID = CommonDefine.SymbologyID.SYM_INT25;
                break;
            case MAXICODE:
                symbologyID = CommonDefine.SymbologyID.SYM_MAXICODE;
                break;
            case MICROPDF:
                symbologyID = CommonDefine.SymbologyID.SYM_MICROPDF;
                break;
            case OCR:
                symbologyID = CommonDefine.SymbologyID.SYM_OCR;
                break;
            case PDF417:
                symbologyID = CommonDefine.SymbologyID.SYM_PDF417;
                break;
            case POSTNET:
                symbologyID = CommonDefine.SymbologyID.SYM_POSTNET;
                break;
            case QR:
                symbologyID = CommonDefine.SymbologyID.SYM_QR;
                break;
            case RSS:
                symbologyID = CommonDefine.SymbologyID.SYM_RSS;
                break;
            case UPCA:
                symbologyID = CommonDefine.SymbologyID.SYM_UPCA;
                break;
            case UPCE0:
                symbologyID = CommonDefine.SymbologyID.SYM_UPCE0;
                break;
            case UPCE1:
                symbologyID = CommonDefine.SymbologyID.SYM_UPCE1;
                break;
            case ISBT:
                symbologyID = CommonDefine.SymbologyID.SYM_ISBT;
                break;
            case BPO:
                symbologyID = CommonDefine.SymbologyID.SYM_BPO;
                break;
            case CANPOST:
                symbologyID = CommonDefine.SymbologyID.SYM_CANPOST;
                break;
            case AUSPOST:
                symbologyID = CommonDefine.SymbologyID.SYM_AUSPOST;
                break;
            case IATA25:
                symbologyID = CommonDefine.SymbologyID.SYM_IATA25;
                break;
            case CODABLOCK:
                symbologyID = CommonDefine.SymbologyID.SYM_CODABLOCK;
                break;
            case JAPOST:
                symbologyID = CommonDefine.SymbologyID.SYM_JAPOST;
                break;
            case PLANET:
                symbologyID = CommonDefine.SymbologyID.SYM_PLANET;
                break;
            case DUTCHPOST:
                symbologyID = CommonDefine.SymbologyID.SYM_DUTCHPOST;
                break;
            case MSI:
                symbologyID = CommonDefine.SymbologyID.SYM_MSI;
                break;
            case TLCODE39:
                symbologyID = CommonDefine.SymbologyID.SYM_TLCODE39;
                break;
            case TRIOPTIC:
                symbologyID = CommonDefine.SymbologyID.SYM_TRIOPTIC;
                break;
            case CODE32:
                symbologyID = CommonDefine.SymbologyID.SYM_CODE32;
                break;
            case STRT25:
                symbologyID = CommonDefine.SymbologyID.SYM_STRT25;
                break;
            case MATRIX25:
                symbologyID = CommonDefine.SymbologyID.SYM_MATRIX25;
                break;
            case PLESSEY:
                symbologyID = CommonDefine.SymbologyID.SYM_PLESSEY;
                break;
            case CHINAPOST:
                symbologyID = CommonDefine.SymbologyID.SYM_CHINAPOST;
                break;
            case KOREAPOST:
                symbologyID = CommonDefine.SymbologyID.SYM_KOREAPOST;
                break;
            case TELEPEN:
                symbologyID = CommonDefine.SymbologyID.SYM_TELEPEN;
                break;
            case CODE16K:
                symbologyID = CommonDefine.SymbologyID.SYM_CODE16K;
                break;
            case POSICODE:
                symbologyID = CommonDefine.SymbologyID.SYM_POSICODE;
                break;
            case COUPONCODE:
                symbologyID = CommonDefine.SymbologyID.SYM_COUPONCODE;
                break;
            case USPS4CB:
                symbologyID = CommonDefine.SymbologyID.SYM_USPS4CB;
                break;
            case IDTAG:
                symbologyID = CommonDefine.SymbologyID.SYM_IDTAG;
                break;
            case LABEL:
                symbologyID = CommonDefine.SymbologyID.SYM_LABEL;
                break;
            case GS1_128:
                symbologyID = CommonDefine.SymbologyID.SYM_GS1_128;
                break;
            case HANXIN:
                symbologyID = CommonDefine.SymbologyID.SYM_HANXIN;
                break;
            case GRIDMATRIX:
                symbologyID = CommonDefine.SymbologyID.SYM_GRIDMATRIX;
                break;
            case POSTALS:
                symbologyID = CommonDefine.SymbologyID.SYM_POSTALS;
                break;
            case US_POSTALS1:
                symbologyID = CommonDefine.SymbologyID.SYM_US_POSTALS1;
                break;
            case SYMBOLOGIES:
                symbologyID = CommonDefine.SymbologyID.SYMBOLOGIES;
                break;
            default:
                throw new DecoderException("Unsupported code standard.");
        }
        return symbologyID;
    }
}
