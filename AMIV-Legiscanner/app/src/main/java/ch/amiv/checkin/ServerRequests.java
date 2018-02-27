package ch.amiv.checkin;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Roger on 13-Feb-18.
 *
 * Used for getting/sending data from/to the server, if it is required in several activities.
 */

public final class ServerRequests {
    public static RequestQueue requestQueue;

    public interface MemberDBUpdatedCallback {  //used for doing callbacks when the memberDB has been updated
        void OnMDBUpdated();
    }

    //-----Updating Stats-----
    /**
     * Will Get the list of people for the event from the server, with stats, and save to the static memberDatabase.
     */
    public static void UpdateMemberDB (Context context, final MemberDBUpdatedCallback callback)
    {
        if(!CheckConnection(context))
            return;

        Log.e("postrequest", "Params sent: pin=" + MainActivity.CurrentPin + ", URL used: " + SettingsActivity.GetServerURL(context) + "/checkin_update_data");

        JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET, SettingsActivity.GetServerURL(context) + "/checkin_update_data",
                (JSONObject) null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.e("postrequest", "JSON file response received.");

                // Parsing json object response and save to the static memberDB
                try {
                    if(MemberDatabase.instance == null) {
                        Log.e("postrequest", "Cancelled parsing json as assumed the scanning session was ended.");
                        return;
                    }

                    Log.e("postrequest", "response length:" + response.length() + "   keys: " + response.keys().next() + "   content: " + response.toString());

                    //1. Update List of People
                    if (response.has("signups"))
                        MemberDatabase.instance.UpdateMemberData(response.getJSONArray("signups"));

                    //2. Update Stats
                    JSONArray stats = response.getJSONArray("statistics");

                    for (int i = 0; i < stats.length(); i++) {
                        JSONObject j = stats.getJSONObject(i);

                        switch (j.getString("key")) //Assumes 'odd' format of having an array of Json objects each with only two entries, named "key" and "value", see sample JSON under "app/misc/"
                        {
                            case "Total Signups":           //Event type
                                MemberDatabase.instance.totalSignups = j.getInt("value");
                                break;
                            case "Current Attendance":
                                MemberDatabase.instance.currentAttendance = j.getInt("value");
                                break;
                            case "Regular Members":         //GV type
                                MemberDatabase.instance.regularMembers = j.getInt("value");
                                break;
                            case "Extraordinary Members":
                                if(MemberDatabase.instance.eventType == MemberDatabase.EventType.none)  //also imply event type
                                    MemberDatabase.instance.eventType = MemberDatabase.EventType.GV;
                                MemberDatabase.instance.extraordinaryMembers = j.getInt("value");
                                break;
                            case "Honorary Members":
                                MemberDatabase.instance.honoraryMembers = j.getInt("value");
                                break;
                            case "Total Members Present":
                                MemberDatabase.instance.totalMembers = j.getInt("value");
                                break;
                            case "Total Non-Members Present":
                                MemberDatabase.instance.totalNonMembers = j.getInt("value");
                                break;
                            case "Total Attendance":
                                MemberDatabase.instance.currentAttendance = j.getInt("value");
                                break;
                            case "Maximum Attendance":
                                MemberDatabase.instance.maxAttendance = j.getInt("value");
                                break;
                            /*default:
                                Log.e("postrequest", "Unknown/unhandled statistics key found in json during UpdateMemberDB(), key: " + j.get(key).toString() + ", value: " + j.get("value").toString());
                                break;*/
                        }
                    }
                    if(MemberDatabase.instance.eventType == MemberDatabase.EventType.none)  //tmp way of finding out eventType, should find out directly from server
                        MemberDatabase.instance.eventType = MemberDatabase.EventType.Event;

                    if(callback != null)
                        callback.OnMDBUpdated();    //Ensure to call callback
                }
                catch (JSONException e) {
                    Log.e("postrequest", "Error parsing received JsonObject in GetStats().");
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("postrequest", "Response from server for JSON data: " + error.networkResponse.statusCode + " with text: " + new String(error.networkResponse.data) + " on event pin: " + MainActivity.CurrentPin);
            }
        })
        {
            @Override
            protected VolleyError parseNetworkError(VolleyError volleyError) {  //see comments at parseNetworkResponse()
                if(volleyError != null)
                    Log.e("postrequest", "parseNetworkError Response from server for JSON data: " + volleyError.networkResponse.statusCode + " with text: " + new String(volleyError.networkResponse.data) + " on event pin: " + MainActivity.CurrentPin);
                return super.parseNetworkError(volleyError);
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();

                headers.put("pin", MainActivity.CurrentPin);
                return headers;
            }
        };

        if(requestQueue == null)
            requestQueue = Volley.newRequestQueue(context);  //Adds the defined post request to the queue to be sent to the server
        requestQueue.add(req);
    }

    /**
     * @param context
     * @return returns true if there is an active internet connection, test this before requesting something from the server
     */

    public static boolean CheckConnection(Context context)
    {
        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        if((activeNetwork == null || !activeNetwork.isConnectedOrConnecting()))
        {
            Log.e("postrequest", "No active internet connection");
            return false;
        }
        return true;
    }
}