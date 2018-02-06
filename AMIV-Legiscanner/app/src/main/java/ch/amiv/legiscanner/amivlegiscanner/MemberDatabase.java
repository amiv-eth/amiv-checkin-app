package ch.amiv.legiscanner.amivlegiscanner;

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
public class MemberDatabase {
    final List<Member> members = new ArrayList<Member>();

    //-----Statistics------
    public int totalSignups;
    public int currentAttendance;

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
