package ch.amiv.checkin;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Roger on 06-Feb-18.
 * Contains all the data received by the server with GET /checkin_update_data in a android friendly format. The JSON file received by the server fills this.
 * See README_API on the server side repo
 */
public class EventDatabase {
    public static EventDatabase instance;

    final List<Member> members = new ArrayList<Member>();
    public EventData eventData = new EventData();
    public List<KeyValuePair> stats = new ArrayList<KeyValuePair>();

    //-----Statistics------
    public int totalSignups;        //For Events
    public int currentAttendance;   //GV and Events
    public int regularMembers;      //For GV
    public int extraordinaryMembers;
    public int honoraryMembers;
    public int totalMembers;
    public int totalNonMembers;
    public int maxAttendance;

    public EventDatabase()
    {
        if(instance != null)
            Log.e("memberDatabase", "A Member Database already exists, cannot create another. Should delete old MemberDB if this is wanted");

        instance = this;
    }

    public void UpdateMemberData(JSONArray _members)
    {
        members.clear();
        for (int i = 0; i < _members.length(); i++) {
            try {
                Member m = new Member(_members.getJSONObject(i));
                members.add(m);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void UpdateStats(JSONArray statSource, boolean hasEventInfos)
    {
        stats.clear();

        try {
            for (int i = 0; i < statSource.length(); i++) {
                JSONObject j = statSource.getJSONObject(i);

                stats.add(new KeyValuePair(j.getString("key"), j.get("value").toString()));

                switch (j.getString("key")) //Assumes 'odd' format of having an array of Json objects each with only two entries, named "key" and "value", see sample JSON under "app/misc/"
                {
                    case "Total Signups":           //Event type
                        EventDatabase.instance.totalSignups = j.getInt("value");
                        break;
                    case "Current Attendance":
                        EventDatabase.instance.currentAttendance = j.getInt("value");
                        break;
                    case "Regular Members":         //GV type
                        EventDatabase.instance.regularMembers = j.getInt("value");
                        break;
                    case "Extraordinary Members":
                        if (hasEventInfos && EventDatabase.instance.eventData.eventType == EventData.EventType.None)  //also imply event type
                            EventDatabase.instance.eventData.eventType = EventData.EventType.GV;
                        EventDatabase.instance.extraordinaryMembers = j.getInt("value");
                        break;
                    case "Honorary Members":
                        EventDatabase.instance.honoraryMembers = j.getInt("value");
                        break;
                    case "Total Members Present":
                        EventDatabase.instance.totalMembers = j.getInt("value");
                        break;
                    case "Total Non-Members Present":
                        EventDatabase.instance.totalNonMembers = j.getInt("value");
                        break;
                    case "Total Attendance":
                        EventDatabase.instance.currentAttendance = j.getInt("value");
                        break;
                    case "Maximum Attendance":
                        EventDatabase.instance.maxAttendance = j.getInt("value");
                        break;
                    /*default:
                        Log.e("postrequest", "Unknown/unhandled statistics key found in json during UpdateMemberDB(), key: " + j.get(key).toString() + ", value: " + j.get("value").toString());
                        break;*/
                }
            }
            if (hasEventInfos && EventDatabase.instance.eventData.eventType == EventData.EventType.None)  //imply event type if we do not have the event infos
                EventDatabase.instance.eventData.eventType = EventData.EventType.Event;
        }
        catch (JSONException e) {
            Log.e("postrequest", "Error parsing received JsonObject in GetStats().");
            e.printStackTrace();
        }
    }
}
