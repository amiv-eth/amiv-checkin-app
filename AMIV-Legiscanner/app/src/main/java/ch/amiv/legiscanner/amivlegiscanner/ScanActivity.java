package ch.amiv.legiscanner.amivlegiscanner;

/**
 * Author: Roger Barton, rbarton@ethz.ch
 * Date Created: 2/12/17
 */

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.android.volley.NetworkResponse;
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
    private static int NEXT_LEGI_DELAY = 1000;   //delay between the response from the server and scanning the next legi (in ms)

    boolean mIsCheckingIn = true;   //sets whether we a checking people in or out, will be sent to the server
    boolean mCheckInOnLastBarcode = true;
    String mLastBarcodeScanned = "00000000";
    boolean mAllowNextBarcode = true;
    boolean mCanClearResponse = true;

    //----Server Communication-----
    boolean mWaitingOnServer = false;


    //-----UI Elements----
    Switch mCheckInSwitch;
    EditText mLegiInputField;
    TextView mWaitLabel;
    TextView mValidLabel;
    TextView mInvalidLabel;
    TextView mServerErrorLabel;
    ImageView mTickImage;
    ImageView mCrossImage;
    ImageView mBGTint;
    FloatingActionButton mStartMemberListActivity;

    //-----Barcode Scanning Related----
    BarcodeDetector mBarcodeDetector;
    CameraSource mCameraSource;
    SurfaceView mCameraView;
    TextView mBarcodeInfo;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        //----Initialising UI-----
        mStartMemberListActivity = (FloatingActionButton)findViewById(R.id.ShowMemberList);
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
        mTickImage = (ImageView)findViewById(R.id.TickImage);
        mCrossImage = (ImageView)findViewById(R.id.CrossImage);
        mBGTint = (ImageView)findViewById(R.id.BackgroundTint);
        mBGTint.setAlpha(0.4f);

        RelativeLayout mCameraLayout = (RelativeLayout) findViewById(R.id.CameraLayout);
        mCameraLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mCanClearResponse) {
                    mAllowNextBarcode = true;
                    ResetResponseUI();
                }
            }

        });

        //----Setting up Camera and barcode tracking with callbacks------
        mCameraView = (SurfaceView)findViewById(R.id.CameraView);
        mBarcodeInfo = (TextView)findViewById(R.id.BarcodeOutput);

        mBarcodeDetector = new BarcodeDetector.Builder(this).setBarcodeFormats(Barcode.CODE_39).build();                 //IMPORTANT: Set the correct format for the barcode reader, can choose all formats but will be slower
        CameraSource.Builder camBuilder= new CameraSource.Builder(this, mBarcodeDetector).setRequestedPreviewSize(640, 480);
        camBuilder.setAutoFocusEnabled(true);
        //mCameraSource = new CameraSource.Builder(this, mBarcodeDetector).setRequestedPreviewSize(640, 480).build();
        mCameraSource = camBuilder.build();

        //initialising the camera view, so we can see the camera and analyse the frames for barcodes
        mCameraView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                try {
                    if ( ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA ) != PackageManager.PERMISSION_GRANTED ) {
                        //IMPLEMENT: ask for camera permission
                    }
                    else
                        mCameraSource.start(mCameraView.getHolder());
                } catch (IOException ie) {
                    Log.e("mCameraSource", ie.getMessage());
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                mCameraSource.stop();
            }
        });

        //detecting barcodes
        mBarcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {}

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {    //This is called every frame or so and will give the detected barcodes
                final SparseArray<Barcode> barcodes = detections.getDetectedItems();

                if (barcodes.size() == 0)
                    return;

                //Only allow another barcode if: time since the last scan has passed, the barcode is different or the checkmode has changed
                if(mAllowNextBarcode) {
                    Log.e("barcodeDetect", "detected barcode: " + barcodes.valueAt(0).displayValue);

                    mAllowNextBarcode = false;  //prevent the same barcode being submitted in the next frame until this is set to true again in the postDelayed call
                    mCanClearResponse = false;
                    mLastBarcodeScanned = barcodes.valueAt(0).displayValue;
                    mCheckInOnLastBarcode = mIsCheckingIn;

                    mBarcodeInfo.post(new Runnable() {    //delay to other thread by using a ui element, as this is in a callback on another thread
                        public void run() {
                            SubmitLegiNrToServer(barcodes.valueAt(0).displayValue); //submit the legi value to the server on the main thread

                            mBarcodeInfo.setText(barcodes.valueAt(0).displayValue);

                            Handler handler = new Handler();    //Delayed call to only allow submission of another legi in x seconds
                            handler.postDelayed(new Runnable() {    //Creates delay call to only allow scanning again after x seconds

                                @Override
                                public void run() {
                                    Log.e("barcodeDetect", "Next barcode scan possible");
                                    mCanClearResponse = true;
                                }
                            }, NEXT_LEGI_DELAY);
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
        ResetResponseUI();

        SubmitLegiNrToServer(s);
    }

    /**
     * Will submit a legi nr to the server and will set the UI accondingly, POST Request is done with Volley.
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
        //Creates a post request which can then later be added to the queue, includes all the callback functionality as well. Use the \mutate, matches with server scripts
        StringRequest postRequest = new StringRequest(Request.Method.POST, SettingsActivity.GetServerURL(getApplicationContext()) + "/mutate"
                , new Response.Listener<String>() { @Override public void onResponse(String response) {}}       //initalise with empty response listeners as we will handle the response in the parseNetworkResponse and parseNetworkError functions
                , new Response.ErrorListener() {@Override public void onErrorResponse(VolleyError error) {}})
        {
            @Override
            protected Response<String> parseNetworkResponse(NetworkResponse response) { //Note: the parseNetworkResponse is only called if the response was successful (codes 2xx), else parseNetworkError is called.
                final NetworkResponse nr = response;
                mValidLabel.post(new Runnable() {    //delay to other thread by using a ui element, as this is in a callback on another thread
                    public void run() {
                        SetUIFromResponse(nr.statusCode, new String(nr.data));  //will adjust UI elems to display response
                    }
                });

                return super.parseNetworkResponse(response);
            }

            @Override
            protected VolleyError parseNetworkError(VolleyError volleyError) {  //see comments at parseNetworkResponse()
                final VolleyError ve = volleyError;
                mValidLabel.post(new Runnable() {    //delay to other thread by using a ui element, as this is in a callback on another thread
                    public void run() {
                        SetUIFromResponse(ve.networkResponse.statusCode, new String(ve.networkResponse.data));
                    }
                });

                return super.parseNetworkError(volleyError);
            }

            @Override
            protected Map<String, String> getParams() { //Adding the parameters to be sent to the server, with forms. Do not change strings as they match with the server scripts!
                Log.e("postrequest", "Params Set: pin=" + MainActivity.CurrentPin + ", info=" + formattedLeginr + ", checkmode=" + (mIsCheckingIn ? "in" : "out"));

                Map<String, String> params = new HashMap<String, String>(); //Parameters being sent to server in POST
                params.put("pin", MainActivity.CurrentPin);
                params.put("info", formattedLeginr);
                params.put("checkmode", (mIsCheckingIn ? "in" : "out"));

                return params;
            }
        };
        //----end of defining post request----


        RequestQueue queue = Volley.newRequestQueue(this);  //Add the request to the queue so it can be sent
        queue.add(postRequest);
    }

    /**
     * Will set UI elements correctly based on the response from the server on a legi submission.
     * @param statusCode http status code from the response, eg 200 or 400
     * @param responseText the text received from the server about our post request
     */
    private void SetUIFromResponse(int statusCode, String responseText)
    {
        SetWaitingOnServer(false);
        Log.e("postrequest", "Response from server for legi submission: " + statusCode + " with text: " + responseText + " on event pin: " + MainActivity.CurrentPin);

        if(statusCode == 200) { //success
            mValidLabel.setVisibility(View.VISIBLE);
            mValidLabel.setText(responseText);
            mTickImage.setVisibility(View.VISIBLE);
            mBGTint.setVisibility(View.VISIBLE);
            mBGTint.setColorFilter(getResources().getColor(R.color.colorValid));
        }
        else {                  //invalid legi/already checked in etc
            mInvalidLabel.setVisibility(View.VISIBLE);
            mInvalidLabel.setText(responseText);
            mCrossImage.setVisibility(View.VISIBLE);
            mBGTint.setVisibility(View.VISIBLE);
            mBGTint.setColorFilter(getResources().getColor(R.color.colorInvalid));
        }
    }

    private void ResetResponseUI ()
    {
        mBarcodeInfo.setText(R.string.no_barcode_detected); //Reset UI
        mValidLabel.setVisibility(View.INVISIBLE);
        mInvalidLabel.setVisibility(View.INVISIBLE);
        mServerErrorLabel.setVisibility(View.INVISIBLE);
        mTickImage.setVisibility(View.INVISIBLE);
        mCrossImage.setVisibility(View.INVISIBLE);
        mBGTint.setVisibility(View.INVISIBLE);
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

    //===Transition to  Member List Activity===
    public void StartMemberListActivity(View view)
    {
        Intent intent = new Intent(this, MemberListActivity.class);
        startActivity(intent);
    }

}
