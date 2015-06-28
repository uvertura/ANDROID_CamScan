package ru.pecom.android;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.honeywell.decodemanager.DecodeManager;
import com.honeywell.decodemanager.SymbologyConfigs;
import com.honeywell.decodemanager.barcode.CommonDefine;
import com.honeywell.decodemanager.barcode.DecodeResult;
import com.honeywell.decodemanager.symbologyconfig.SymbologyConfigCode39;
import com.honeywell.decodemanager.symbologyconfig.SymbologyConfigCodeEan13;
import com.honeywell.decodemanager.symbologyconfig.SymbologyConfigCodeEan8;

import java.io.IOException;

public class MainActivity extends Activity {
    private final int LEFT_KEY = 87;
    private final int RIGHT_KEY = 88;
    private final int CENTER_KEY = 0x94;

    static final int REQUEST_IMAGE_CAPTURE = 1;

    private DecodeManager mDecodeManager = null;


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                this.finish();
                return true;
            case KeyEvent.KEYCODE_UNKNOWN:
                if(RIGHT_KEY == event.getScanCode()) {
                    try {
                        scan();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }else if(LEFT_KEY == event.getScanCode()){
                    try {
                        photo();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }else if(CENTER_KEY == event.getScanCode()){
                    ((LinearLayout)findViewById(R.id.resullayout)).removeAllViews();
                }
                return true;
            default:
                return super.onKeyDown(keyCode, event);
        }
    }

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        final Button scanButton = (Button) findViewById(R.id.scannbutton);
        scanButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                try {
                    scan();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return false;
            }
        });

        final Button camButton = (Button) findViewById(R.id.cambutton);
        camButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                try {
                    photo();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return false;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        SoundManager.getInstance();
        SoundManager.initSounds(getBaseContext());
        SoundManager.loadSounds();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mDecodeManager != null) {
            try {
                mDecodeManager.cancelDecode();
                mDecodeManager.release();
                mDecodeManager = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void photo() throws Exception {
        if(null != mDecodeManager){
            mDecodeManager.cancelDecode();
            mDecodeManager.release();
            mDecodeManager = null;
        }

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE){

        };
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(REQUEST_IMAGE_CAPTURE == requestCode && resultCode == RESULT_OK){
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            ImageView imageView = new ImageView(this);
            imageView.setPadding(5,5,5,5);
            imageView.setMaxHeight(200);
            imageView.setImageBitmap(imageBitmap);
            ((LinearLayout)findViewById(R.id.resullayout)).addView(imageView);
        }
    }

    private void scan() throws Exception {
        if (mDecodeManager == null) {
            mDecodeManager = new DecodeManager(this,ScanResultHandler);
        }else {
            try {
                mDecodeManager.doDecode(5000);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    private final Context self = this;

    private Handler ScanResultHandler = new Handler() {
        public void handleMessage(Message msg) {
            TextView resultText;
            switch (msg.what) {
                case DecodeManager.MESSAGE_DECODER_COMPLETE:
                    DecodeResult decodeResult = (DecodeResult) msg.obj;

                    SoundManager.playSound(1, 1);

                    resultText = new TextView(self);
                    resultText.setTextColor(Color.BLACK);
                    resultText.setText(decodeResult.barcodeData);
                    ((LinearLayout)findViewById(R.id.resullayout)).addView(resultText);
                    break;

                case DecodeManager.MESSAGE_DECODER_FAIL: {
                    SoundManager.playSound(2, 1);
                    resultText = new TextView(self);
                    resultText.setTextColor(Color.BLACK);
                    resultText.setText("[SCAN FAIL]");
                    ((LinearLayout)findViewById(R.id.resullayout)).addView(resultText);
                }
                break;
                case DecodeManager.MESSAGE_DECODER_READY:
                {
                    try {
                        mDecodeManager.enableSymbology(CommonDefine.SymbologyID.SYM_ALL); //Включить все поддерживаемые коды
                        scan();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
                default:
                    super.handleMessage(msg);
                    break;
            }
        }
    };
}
