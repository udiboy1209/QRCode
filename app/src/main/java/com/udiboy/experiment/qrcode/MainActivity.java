package com.udiboy.experiment.qrcode;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.FormatException;
import com.google.zxing.LuminanceSource;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;

import java.util.List;


public class MainActivity extends ActionBarActivity {


    QRCodeReader reader;
    int[] pixels ;

    private Camera mCamera;
    private Camera.Size mSize;
    private CameraPreview mPreview;
    private Camera.PictureCallback mPicture;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mCamera = getCameraInstance();

        Camera.Parameters parameters = mCamera.getParameters();
        List<Camera.Size> sizes = parameters.getSupportedPictureSizes();

        for(Camera.Size size :sizes){
            mSize=size;

            Log.d("CameraSize" , size.width+"x"+size.height);

            if(size.width<800)
                break;
        }

        if(mSize==null){
            mSize=sizes.get(sizes.size()-1);
        }

        parameters.setPictureSize(mSize.width,mSize.height);
        pixels=new int[mSize.width*mSize.height];

        mCamera.setParameters(parameters);

        // Create our Preview view and set it as the content of our activity.
        mPreview = new CameraPreview(this, mCamera);
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(mPreview);

        reader = new QRCodeReader();

        mPicture = new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, final Camera camera) {
                Bitmap img = BitmapFactory.decodeByteArray(data,0,data.length);
                /*int[] pixelData = new int[img.getWidth()*img.getHeight()];
                img.getPixels(pixelData,0,img.getWidth(),0,0,img.getWidth(),img.getHeight());*/

                data = null;

                img.getPixels(pixels,0,mSize.width,0,0,mSize.width,mSize.height);

                LuminanceSource luminImg = new RGBLuminanceSource( img.getWidth(), img.getHeight(),pixels);
                BinaryBitmap binImg = new BinaryBitmap(new HybridBinarizer(luminImg));

                try {
                    Result decoded = reader.decode(binImg);

                    new AlertDialog.Builder(MainActivity.this)
                            .setMessage(decoded.getText())
                            .setTitle("Decoded Message")
                            .setNeutralButton(android.R.string.ok,
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int whichButton){
                                            camera.startPreview();
                                        }
                                    })
                            .show();

                } catch (NotFoundException e) {
                    Toast.makeText(MainActivity.this, "QR Code not found!",Toast.LENGTH_LONG).show();
                    camera.startPreview();
                } catch (ChecksumException e) {
                    Toast.makeText(MainActivity.this, "QR Code invalid! Please scan properly.",Toast.LENGTH_LONG).show();
                    camera.startPreview();
                } catch (FormatException e) {
                    Toast.makeText(MainActivity.this, "Wrong format",Toast.LENGTH_LONG).show();
                    camera.startPreview();
                }
            }
        };

        mCamera.setDisplayOrientation(90);
    }

    public static Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }

    public void scan(View v){

        mCamera.takePicture(null,null,mPicture);


    }

    @Override
    public void onDestroy(){
        mCamera.release();
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
