package ru.pecom.android;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.honeywell.decodemanager.DecodeManager;
import ru.pecom.android.bc.Decoder;
import ru.pecom.android.bc.DecoderException;
import ru.pecom.android.bc.DecoderFactory;
import ru.pecom.android.bc.STANDARD;
import ru.pecom.android.bc.evt.DecodeCompleteListener;
import ru.pecom.android.bc.evt.DecodeReadyListener;

public class MainActivity extends Activity {
    private final int LEFT_KEY = 87;
    private final int RIGHT_KEY = 88;
    private final int CENTER_KEY = 0x94;

    static final int REQUEST_IMAGE_CAPTURE = 1;

    private final Decoder decoder;
    private final ProgressDialog waitDialog = new ProgressDialog(this);

    public MainActivity() {
        waitDialog.setTitle("Initialization");
        waitDialog.setMessage("Please wait...");
        waitDialog.setIndeterminate(true);
        decoder = DecoderFactory.getDecoder(this, DecoderFactory.HARDWARE.HONEYWELL);
        decoder.addReadyHandler(new DecodeReadyListener() {
            @Override
            public void onDecodeReady(DecodeManager sender) {
                waitDialog.dismiss();
                scan();
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                this.finish();
                return true;
            case KeyEvent.KEYCODE_UNKNOWN:
                if(RIGHT_KEY == event.getScanCode()) {
                    scan();
                }else if(LEFT_KEY == event.getScanCode()){
                    photo();
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

        try {
            decoder.addStandard(STANDARD.ALL);
        } catch (DecoderException e) {
            e.printStackTrace();
        }

        final MainActivity self = this;
        decoder.addCompleteHandler(new DecodeCompleteListener() {
            @Override
            public void onSuccess(Decoder sender, ru.pecom.android.bc.DecodeResult result) {
                SoundManager.playSound(1, 1);

                TextView resultText = new TextView(self);
                resultText.setTextColor(Color.BLACK);
                resultText.setText(new String(result.getData()));
                ((LinearLayout)findViewById(R.id.resullayout)).addView(resultText);
            }

            @Override
            public void onFail(Decoder sender) {
                SoundManager.playSound(2, 1);
            }

            @Override
            public void onComplete(Decoder sender) {}
        });

        final Button scanButton = (Button) findViewById(R.id.scannbutton);
        scanButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                scan();
                return false;
            }
        });

        final Button camButton = (Button) findViewById(R.id.cambutton);
        camButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                photo();
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

        if(decoder.isInitialized()) {
            try {
                decoder.destroy();
            } catch (DecoderException e) {
                e.printStackTrace();
            }
        }
    }

    private void photo(){
        if(decoder.isInitialized()) {
            try {
                decoder.destroy();
            } catch (DecoderException e) {
                e.printStackTrace();
            }
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

    private void scan(){
        if(decoder.isInitialized()) {
            try {
                waitDialog.show();
                decoder.init();
            } catch (DecoderException e) {
                e.printStackTrace();
            }
        }else{
            try {
                decoder.decode(5000);
            } catch (DecoderException e) {
                e.printStackTrace();
            }
        }
    }

}
