package ch.amiv.legiscanner.amivlegiscanner;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.view.View.OnKeyListener;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

public class ScanActivity extends AppCompatActivity {
    boolean mWaitingOnServer = false;
    boolean mIsCheckingIn = true;   //sets whether we a checking people in or out, will be sent to the server

    //-----UI Elements----
    Switch mCheckInSwitch;
    EditText mLegiInputField;
    TextView mWaitLabel;
    TextView mValidLabel;
    TextView mInvalidLabel;
    TextView mServerErrorLabel;
    Button mSubmitLeginrButton;

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
        SetWaitingOnServer(true);
        mValidLabel.setVisibility(View.INVISIBLE);
        mInvalidLabel.setVisibility(View.INVISIBLE);
        mServerErrorLabel.setVisibility(View.INVISIBLE);

        RequestQueue queue = Volley.newRequestQueue(this);


        Log.e("response", "URL used: " + SettingsActivity.GetServerURL(getApplicationContext()) + "?pin=" + MainActivity.CurrentPin + "&legi=" + leginr + "&checkin=" + mIsCheckingIn);

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, SettingsActivity.GetServerURL(getApplicationContext()) + "?pin=" + MainActivity.CurrentPin + "&legi=" + leginr + "&checkin=" + mIsCheckingIn,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        mWaitingOnServer = false;
                        SetWaitingOnServer(false);


                        // Display the first 500 characters of the response string.
                        Log.e("response", "Response from server: " + response.substring(0,500) + " on event pin: " + MainActivity.CurrentPin);

                        if(response == "200")
                        {
                            mValidLabel.setVisibility(View.VISIBLE);
                            Log.e("response", "Legi nr valid");
                        }
                        else
                        {
                            mInvalidLabel.setVisibility(View.VISIBLE);
                            Log.e("response", "Legi nr NOT valid");
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        SetWaitingOnServer(false);
                        mInvalidLabel.setVisibility(View.VISIBLE);
                        Log.e("response", "Server sent back error: " + error);
                    }
                });
        // Add the request to the RequestQueue.
        queue.add(stringRequest);
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
