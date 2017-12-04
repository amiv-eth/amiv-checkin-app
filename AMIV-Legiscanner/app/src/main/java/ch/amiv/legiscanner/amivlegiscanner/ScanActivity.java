package ch.amiv.legiscanner.amivlegiscanner;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ScanActivity extends AppCompatActivity {
    boolean mWaitingOnServer = false;
    boolean mIsCheckingIn = true;   //sets whether we a checking people in or out, will be sent to the server
    boolean mCheckInOnLastBarcode = true;
    String mLastBarcodeScanned = "00000000";
    boolean mAllowNextBarcode = true;


    //-----UI Elements----
    Switch mCheckInSwitch;
    EditText mLegiInputField;
    TextView mWaitLabel;
    TextView mValidLabel;
    TextView mInvalidLabel;
    TextView mServerErrorLabel;
    Button mSubmitLeginrButton;


    BarcodeDetector mBarcodeDetector;
    CameraSource mCameraSource;
    SurfaceView mCameraView;
    TextView mBarcodeInfo;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        //----Initialising UI-----
        mLegiInputField = (EditText)findViewById(R.id.LegiInputField);
        mCheckInSwitch = (Switch)findViewById(R.id.CheckInSwitch);
        mCheckInSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mIsCheckingIn = isChecked;
            }
        });
        mWaitLabel = (TextView)findViewById(R.id.PleaseWaitLabel);
        mValidLabel = (TextView)findViewById(R.id.ValidLabel);
        mInvalidLabel = (TextView)findViewById(R.id.InvalidLabel);
        mServerErrorLabel = (TextView)findViewById(R.id.ServerErrorLabel);

        //----Setting up Camera and barcode tracking with callbacks------
        mCameraView = (SurfaceView)findViewById(R.id.CameraView);
        mBarcodeInfo = (TextView)findViewById(R.id.BarcodeOutput);

        mBarcodeDetector = new BarcodeDetector.Builder(this).setBarcodeFormats(Barcode.CODE_39).build();                 //IMPORTANT: Set the correct format for the barcode reader, can choose all formats but will be slower
        CameraSource.Builder camBuilder= new CameraSource.Builder(this, mBarcodeDetector).setRequestedPreviewSize(640, 480);
        camBuilder.setAutoFocusEnabled(true);
        //mCameraSource = new CameraSource.Builder(this, mBarcodeDetector).setRequestedPreviewSize(640, 480).build();
        mCameraSource = camBuilder.build();

        mCameraView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                try {

                    if ( ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA ) != PackageManager.PERMISSION_GRANTED ) {
                        //Ask for permission if we dont have it already, or just restart app
                        //ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 0);
                    }
                    else
                        mCameraSource.start(mCameraView.getHolder());
                } catch (IOException ie) {
                    Log.e("mCameraSource", ie.getMessage());
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                mCameraSource.stop();
            }
        });

        mBarcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {
            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {    //This is called every frame or so and will give the detected barcodes
                final SparseArray<Barcode> barcodes = detections.getDetectedItems();

                if (barcodes.size() == 0)
                    return;

                //Only allow another barcode if: time since the last scan has passed, the barcode is different or the checkmode has changed
                if((mAllowNextBarcode && (!mLastBarcodeScanned.equals(barcodes.valueAt(0).displayValue) || mCheckInOnLastBarcode != mIsCheckingIn))) {
                    Log.e("barcodeDetect", "detected barcode: " + barcodes.valueAt(0).displayValue);

                    mAllowNextBarcode = false;  //prevent the same barcode being submitted in the next frame until this is set to true again in the postDelayed call
                    mLastBarcodeScanned = barcodes.valueAt(0).displayValue;
                    mCheckInOnLastBarcode = mIsCheckingIn;

                    mBarcodeInfo.post(new Runnable() {    //delay to other thread by using a ui element, as this is in a callback on another thread
                        public void run() {
                            SubmitLegiNrToServer(barcodes.valueAt(0).displayValue);

                            mBarcodeInfo.setText(barcodes.valueAt(0).displayValue);

                            Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {    //Creates delay call to only allow scanning again after x seconds

                                @Override
                                public void run() {
                                    Log.e("barcodeDetect", "Next barcode scan possible");

                                    mAllowNextBarcode = true;
                                    mBarcodeInfo.setText(R.string.no_barcode_detected); //Reset UI
                                    mValidLabel.setVisibility(View.INVISIBLE);
                                    mInvalidLabel.setVisibility(View.INVISIBLE);
                                    mServerErrorLabel.setVisibility(View.INVISIBLE);
                                }
                            }, 2500);//in millisecs
                        }
                    });
                }
            }
        });
    }

    /**
     * Call this to submit a legi nr from a UI Element
     * @param view
     */
    public void SubmitLegiNrFromTextField(View view)
    {
        String s = mLegiInputField.getText().toString();
        if("".equals(s))
            return;

        mLegiInputField.setText("");

        SubmitLegiNrToServer(s);
    }

    /**
     * Will submit a legi nr to the server and will set the UI accondingly, GET Request is done with Volley
     * @param leginr
     */
    public void SubmitLegiNrToServer(String leginr)
    {
        final String formattedLeginr;
        if(leginr.charAt(0) == 'S')         //Remove prefix S from barcode if needed
            formattedLeginr = leginr.substring(1);
        else
            formattedLeginr = leginr;

        SetWaitingOnServer(true);       //Clear UI
        mValidLabel.setVisibility(View.INVISIBLE);
        mInvalidLabel.setVisibility(View.INVISIBLE);
        mServerErrorLabel.setVisibility(View.INVISIBLE);

        Log.e("postrequest", "Params sent: pin=" + MainActivity.CurrentPin + ", info=" + formattedLeginr + ", checkmode=" + (mIsCheckingIn ? "in" : "out") + ", URL used: " + SettingsActivity.GetServerURL(getApplicationContext()));

        //----POST Request----
        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest postRequest = new StringRequest(Request.Method.POST, SettingsActivity.GetServerURL(getApplicationContext()) + "/mutate", new Response.Listener<String>() {

                @Override
                public void onResponse(String response) {
                    SetWaitingOnServer(false);
                    Log.e("postrequest", "Response from server: " + response + " on event pin: " + MainActivity.CurrentPin);

                    if(response.equals("Checked-IN") || response.equals("Checked-OUT")) {   //XXXXXX Check with server response code 200, also check that string matches with server code
                        mValidLabel.setVisibility(View.VISIBLE);
                        //mValidLabel.setText("Insert response from server");
                        Log.e("postrequest", "Legi nr valid");
                    }
                    else {
                        mInvalidLabel.setVisibility(View.VISIBLE);
                        //mValidLabel.setText("Insert response from server");
                        Log.e("postrequest", "Legi nr NOT valid");
                    }
                }
            },
            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    SetWaitingOnServer(false);
                    mServerErrorLabel.setVisibility(View.VISIBLE);
                    Log.e("postrequest", "Server sent back error: " + error);
                }
            }
            ) {
            @Override
            protected Map<String, String> getParams() {
                Log.e("postrequest", "Params Set: pin=" + MainActivity.CurrentPin + ", info=" + formattedLeginr + ", checkmode=" + (mIsCheckingIn ? "in" : "out"));

                Map<String, String> params = new HashMap<String, String>(); //Parameters being sent to server in POST
                params.put("pin", MainActivity.CurrentPin);
                params.put("info", formattedLeginr);
                params.put("checkmode", (mIsCheckingIn ? "in" : "out"));

                return params;
            }
        };
        queue.add(postRequest);
    }

    /**
     * Use this to set UI accordingly and prevent a second request being sent before the first one returns
     * @param isWaiting true we are still waiting for the response from the server
     */
    private void SetWaitingOnServer (boolean isWaiting)
    {
        mWaitingOnServer = isWaiting;
        mWaitLabel.setVisibility((isWaiting ? View.VISIBLE : View.INVISIBLE));
    }
}
