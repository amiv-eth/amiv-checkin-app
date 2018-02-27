package ch.amiv.checkin;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Roger on 06-Feb-18.
 * Contains all the data received by the server with GET /checkin_update_data in a android friendly format. The JSON file received by the server fills this.
 * See README_API on the server side repo
 */
public class MemberDatabase {
    public static MemberDatabase instance;
    public enum EventType {GV, Event, none}
    public EventType eventType = EventType.none;

    final List<Member> members = new ArrayList<Member>();

    //-----Statistics------
    public int totalSignups;        //For Events
    public int currentAttendance;   //GV and Events
    public int regularMembers;      //For GV
    public int extraordinaryMembers;
    public int honoraryMembers;
    public int totalMembers;
    public int totalNonMembers;
    public int maxAttendance;

    public MemberDatabase ()
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
}
