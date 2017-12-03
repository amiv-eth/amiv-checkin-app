package ch.amiv.legiscanner.amivlegiscanner;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import com.android.volley.*;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    public static String CurrentPin;
    boolean mWaitingOnServer = false;

    EditText mPinField;
    TextView mInvalidPinLabel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPinField = (EditText)findViewById(R.id.PinField);
        mInvalidPinLabel = (TextView) findViewById(R.id.InvalidPinLabel);

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.CAMERA)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 0);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();  // Always call the superclass method first

        mWaitingOnServer = false;
        mPinField.setText("");  //clear pin field
        mInvalidPinLabel.setVisibility(View.INVISIBLE);
    }

    /**
     * Submit a pin for an event to the server and act on response accondingly, ie open scanActivity if valid, or request pin entry again
     * @param view
     */
    public void SubmitPin(View view)
    {
        if(mWaitingOnServer || "".equals(mPinField.getText().toString()))  //prevents submitting a second pin while still waiting on the response for the first pin
            return;
        mWaitingOnServer = true;

        CurrentPin = mPinField.getText().toString();

        Log.e("pin", "event pin submitted to server: " + CurrentPin);
        /*RequestQueue queue = Volley.newRequestQueue(this);

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, SettingsActivity.GetServerURL(getApplicationContext()) + "?pin=" + CurrentPin,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        Log.e("response", "Response from server: " + response.substring(0,500) + " on event pin: " + CurrentPin);

                        if(response == "200"){
                            StartScanActivity();
                            mWaitingOnServer = false;
                        }
                    }
                },
                new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    mWaitingOnServer = false;
                    Log.e("response", "Server sent back error: " + error);
                    mInvalidPinLabel.setVisibility(View.VISIBLE);
                }
        });
        // Add the request to the RequestQueue.
        queue.add(stringRequest);*/



        //----POST Request----
        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest postRequest = new StringRequest(Request.Method.POST, SettingsActivity.GetServerURL(getApplicationContext()) + "/checkpin", new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.e("response", "Response from server: " + response + " on event pin: " + CurrentPin);

                mWaitingOnServer = false;

                if(response.equals("PIN valid.")){  //HACK should check for code 200 instead but this works
                    StartScanActivity();
                }
                else
                    mInvalidPinLabel.setVisibility(View.VISIBLE);
            }
        },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("postrequest", "Server sent back error: " + error);
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                //Log.e("postrequest", "Params Set: pin=" + MainActivity.CurrentPin + ", info=" + formattedLeginr + ", checkmode=" + (mIsCheckingIn ? "in" : "out"));

                Map<String, String> params = new HashMap<String, String>(); //Parameters being sent to server in POST
                params.put("pin", MainActivity.CurrentPin);

                return params;
            }
        };
        queue.add(postRequest);


        //StartScanActivity();    //NOTE: Remove when server response is set up!
    }

    private void StartScanActivity()
    {
        Intent intent = new Intent(this, ScanActivity.class);
        startActivity(intent);
    }


    public void StartSettingsActivity(View view)
    {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }
}
