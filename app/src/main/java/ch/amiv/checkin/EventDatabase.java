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

                //also imply event type if it has not been parsed from the json
                if ((!hasEventInfos || EventDatabase.instance.eventData.eventType == EventData.EventType.NotSet || EventDatabase.instance.eventData.eventType == EventData.EventType.Unknown)
                        && j.getString("key").equalsIgnoreCase("Extraordinary Members"))
                    EventDatabase.instance.eventData.eventType = EventData.EventType.GV;
            }
            if (hasEventInfos && EventDatabase.instance.eventData.eventType == EventData.EventType.NotSet)  //imply event type if we do not have the event infos
                EventDatabase.instance.eventData.eventType = EventData.EventType.Event;
        }
        catch (JSONException e) {
            Log.e("postrequest", "Error parsing received JsonObject in GetStats().");
            e.printStackTrace();
        }
    }
}
