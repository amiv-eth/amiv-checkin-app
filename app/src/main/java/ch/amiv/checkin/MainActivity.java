package ch.amiv.checkin;

/**
 * Author: Roger Barton, rbarton@ethz.ch
 * Date Created: 2/12/17
 */

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Vibrator;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import com.android.volley.*;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    public static String CurrentPin;
    private boolean mWaitingOnServer = false;

    private EditText mPinField;
    private TextView mInvalidPinLabel;
    public static Vibrator vibrator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPinField = findViewById(R.id.PinField);
        mInvalidPinLabel = findViewById(R.id.InvalidPinLabel);
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        mPinField.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View view, int keyCode, KeyEvent keyevent) {
                if ((keyevent.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    SubmitPin(view);
                    return true;
                }
                return false;
            }
        });

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) { //Get permission for camera
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                //Add popup
            }
            else
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 0);
        }
    }

    @Override
    public void onResume() {        //When we return to the main activity
        super.onResume();

        mWaitingOnServer = false;
        mPinField.setText("");  //clear pin field
        mInvalidPinLabel.setVisibility(View.INVISIBLE);
        EventDatabase.instance = null;
    }

    /**
     * Submit a pin for an event to the server and act on response accondingly, ie open scanActivity if valid, or request pin entry again
     * @param view
     */
    public void SubmitPin(View view)
    {
        vibrator.vibrate(50);

        if(!ServerRequests.CheckConnection(getApplicationContext())) {
            ApplyServerResponse(0, "");
            return;
        }

        if(mWaitingOnServer || "".equals(mPinField.getText().toString()))  //prevents submitting a second pin while still waiting on the response for the first pin
            return;
        mWaitingOnServer = true;


        CurrentPin = mPinField.getText().toString();

        Log.e("pin", "event pin submitted to server: " + CurrentPin);

        //----POST Request----
        StringRequest postRequest = new StringRequest(Request.Method.POST, SettingsActivity.GetServerURL(getApplicationContext()) + "/checkpin"
                , new Response.Listener<String>() { @Override public void onResponse(String response){} }
                , new Response.ErrorListener() { @Override public void onErrorResponse(VolleyError error){} })
        {
            @Override
            protected Response<String> parseNetworkResponse(NetworkResponse response) { //Note: the parseNetworkResponse is only called if the response was successful (codes 2xx), else parseNetworkError is called.
                final NetworkResponse nr = response;
                mPinField.post(new Runnable() {    //delay to other thread by using a ui element, as this is in a callback on another thread
                public void run() {
                    ApplyServerResponse(nr.statusCode, new String(nr.data));  //will adjust UI elems to display response
                }});

                return super.parseNetworkResponse(response);
            }

            @Override
            protected VolleyError parseNetworkError(final VolleyError volleyError) {  //see comments at parseNetworkResponse()
                if(volleyError != null && volleyError.networkResponse != null) {
                    final VolleyError ve = volleyError;
                    mPinField.post(new Runnable() {
                        public void run() {
                            if (ve != null && ve.networkResponse != null)
                                ApplyServerResponse(ve.networkResponse.statusCode, new String(ve.networkResponse.data));
                            else
                                InvalidUrlResponse();
                        }
                    });
                }

                return super.parseNetworkError(volleyError);
            }

            @Override
            protected Map<String, String> getParams() {
                Log.e("postrequest", "Sent event pin to server with params: pin=" + MainActivity.CurrentPin);

                Map<String, String> params = new HashMap<String, String>(); //Parameters being sent to server in POST
                params.put("pin", MainActivity.CurrentPin);

                return params;
            }
        };

        if(ServerRequests.requestQueue == null)
            ServerRequests.requestQueue = Volley.newRequestQueue(getApplicationContext());  //Adds the defined post request to the queue to be sent to the server
        ServerRequests.requestQueue.add(postRequest);


        //StartScanActivity();    //NOTE: Uncomment for debugging without valid pin
    }

    /**
     * Submit a server response to the function, will apply UI feedback or start the main activity
     * @param statusCode http status code from the response, eg 200 or 400
     * @param responseText the text received from the server about our post request
     */
    private void ApplyServerResponse(int statusCode, String responseText)
    {
        mWaitingOnServer = false;

        Log.e("postrequest", "Response from server for pin submission: " + statusCode + " with text: " + responseText + " on event pin: " + MainActivity.CurrentPin);

        if(statusCode == 200) { //success
            StartScanActivity();
        }
        else if(statusCode == 401)//invalid pin
        {
            mInvalidPinLabel.setVisibility(View.VISIBLE);
            mInvalidPinLabel.setText(responseText);
            mPinField.setText("");
        }
        else if (statusCode == 0) //no internet connection
        {
            mInvalidPinLabel.setVisibility(View.VISIBLE);
            mInvalidPinLabel.setText(R.string.no_internet);
        }
        else                    //Other error
        {
            InvalidUrlResponse();
        }
    }

    private void InvalidUrlResponse()
    {
        mInvalidPinLabel.setVisibility(View.VISIBLE);
        mInvalidPinLabel.setText(R.string.invalid_url);
    }

    //Changes to the scanning screen
    private void StartScanActivity()
    {
        EventDatabase.instance = null;
        /*ServerRequests.UpdateEventData(this, new ServerRequests.OnDataReceivedCallback() {
            @Override
            public void OnDataReceived() {
                if(!EventDatabase.instance.eventData.name.equals("") && getActionBar() != null)
                    getActionBar().setTitle(EventDatabase.instance.eventData.name);
            }
        });*/

        Intent intent = new Intent(this, ScanActivity.class);
        startActivity(intent);
    }


    public void StartSettingsActivity(View view)
    {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }
}
