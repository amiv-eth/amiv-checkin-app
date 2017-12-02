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
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    public static String url = "http://www.google.com";
    EditText mPinField;
    int mCurrentPin;
    boolean mWaitingOnServer = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPinField = (EditText)findViewById(R.id.PinField);
    }

    @Override
    public void onResume() {
        super.onResume();  // Always call the superclass method first

        mWaitingOnServer = false;
        mPinField.setText("");  //clear pin field
    }

    public void pinSubmit(View view)
    {
        if(mWaitingOnServer || "".equals(mPinField.getText().toString()))  //prevents submitting a second pin while still waiting on the response for the first pin
            return;
        mWaitingOnServer = true;

        mCurrentPin = Integer.parseInt(mPinField.getText().toString());
/*
        Log.e("pin", "event pin submitted to server: " + mCurrentPin);
        RequestQueue queue = Volley.newRequestQueue(this);

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url + "?pin=" + mCurrentPin,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        Log.e("response", "Response from server: " + response.substring(0,500) + " on event pin: " + mCurrentPin);

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
                }
        });
        // Add the request to the RequestQueue.
        queue.add(stringRequest);
        */

        StartScanActivity();
    }

    private void StartScanActivity()
    {
        Intent intent = new Intent(this, ScanActivity.class);
        intent.putExtra(ScanActivity.PIN, mCurrentPin);
        intent.putExtra(ScanActivity.url, url);
        startActivity(intent);
    }
}
