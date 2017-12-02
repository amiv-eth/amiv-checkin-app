package ch.amiv.legiscanner.amivlegiscanner;

import android.content.Context;
import android.content.Intent;
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
        RequestQueue queue = Volley.newRequestQueue(this);

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
        queue.add(stringRequest);

        StartScanActivity();    //NOTE: Remove when server response is set up!
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
