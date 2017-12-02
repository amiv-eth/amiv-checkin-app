package ch.amiv.legiscanner.amivlegiscanner;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

public class ScanActivity extends AppCompatActivity {
    public static String url = "";
    public  static final String PIN = "000000";
    boolean mWaitingOnServer = false;
    Switch mCheckInSwitch;
    boolean mIsCheckingIn = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        mCheckInSwitch = (Switch)findViewById(R.id.CheckInSwitch);
        mCheckInSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mIsCheckingIn = isChecked;
            }
        });
    }


    public void SubmitLegiNr(String leginr)
    {
        RequestQueue queue = Volley.newRequestQueue(this);

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url + "?pin=" + PIN + "&legi=" + leginr,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        Log.e("response", "Response from server: " + response.substring(0,500) + " on event pin: " + PIN);

                        if(response == "200")
                        {
                            Log.e("response", "Legi nr valid");
                        }
                        else
                        {
                            Log.e("response", "Legi nr NOT valid");
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
    }

}
