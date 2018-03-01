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

    public interface OnDataReceivedCallback {  //used for doing callbacks when the memberDB has been updated
        void OnDataReceived();
    }

    //-----Updating Stats-----
    /**
     * Will Get the list of people for the event from the server, with stats, and save to the static memberDatabase.
     */
    public static void UpdateMemberDB (final Context context, final OnDataReceivedCallback callback)
    {
        if(!CheckConnection(context))
            return;

        Log.e("postrequest", "Params sent: pin=" + MainActivity.CurrentPin + ", URL used: " + SettingsActivity.GetServerURL(context) + "/checkin_update_data");

        JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET, SettingsActivity.GetServerURL(context) + "/checkin_update_data",
                (JSONObject) null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.e("postrequest", "MemberDb JSON file response received.");

                // Parsing json object response and save to the static memberDB
                try {
                    if(EventDatabase.instance == null) {
                        Log.e("postrequest", "Cancelled parsing json as assumed the scanning session was ended.");
                        return;
                    }

                    Log.e("postrequest", "response length:" + response.length() + "   keys: " + response.keys().next() + "   content: " + response.toString());

                    //1. Update List of People
                    if (response.has("signups"))
                        EventDatabase.instance.UpdateMemberData(response.getJSONArray("signups"));

                    //2. Update Event Infos
                    boolean hasEventInfos = response.has("eventinfos");   //If the json does not have the eventinfos or we cannot parse the event type then imply the event type from the stats
                    if(hasEventInfos) {
                        JSONObject eventInfo = response.getJSONObject("eventinfos");
                        Log.e("postrequest", "event infos content:" + eventInfo.toString());

                        hasEventInfos = EventDatabase.instance.eventData.Update(eventInfo.getInt("_id"), eventInfo.getString("event_type"), eventInfo.getString("title"),
                                eventInfo.getString("description"), eventInfo.getInt("signup_count"), eventInfo.getInt("spots"), eventInfo.getString("time_start"));
                    }

                    //3. Update Stats
                    if(response.has("statistics")) {
                        JSONArray stats = response.getJSONArray("statistics");

                        EventDatabase.instance.UpdateStats(stats, hasEventInfos);
                    }

                    if(callback != null)
                        callback.OnDataReceived();    //Ensure to call callback
                }
                catch (JSONException e) {
                    Log.e("postrequest", "Error parsing received JsonObject in GetStats().");
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() { @Override public void onErrorResponse(VolleyError error) {}})
        {
            @Override
            protected VolleyError parseNetworkError(VolleyError volleyError) {  //see comments at parseNetworkResponse()
                if(volleyError != null && volleyError.networkResponse != null)
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

    /**         // Note: Is currently commented out as the server side is giving us the eventData as part of the /gchecking_update_data json, however may change to be separate request, so it is only sent at the start of a session
     * Will update the event data, only call this when entering a session
     * @param callback Use this callback for executing code when the data has been received
     */
    /*public static void UpdateEventData(Context context, final OnDataReceivedCallback callback)
    {
        if(!CheckConnection(context))
            return;

        Log.e("postrequest", "Params sent: pin=" + MainActivity.CurrentPin + ", URL used: " + SettingsActivity.GetServerURL(context) + "/get_event_data");

        JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET, SettingsActivity.GetServerURL(context) + "/get_event_data",
                (JSONObject) null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.e("postrequest", "response length:" + response.length() + "   first key: " + response.keys().next() + "   content: " + response.toString());

                //Note the constuctor will set this as the static instance
                if(response.has("eventinfos"))
                    new EventData(response.optString("event_type"), response.optString("event_title")/*, response.optString("startDate"), response.optString("endDate")*///); //! Check keys with server project !
/*
                if(callback != null)
                    callback.OnDataReceived();    //Ensure to call callback
            }
        }, new Response.ErrorListener() { @Override public void onErrorResponse(VolleyError error) {}})
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
    }*/

    /**
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