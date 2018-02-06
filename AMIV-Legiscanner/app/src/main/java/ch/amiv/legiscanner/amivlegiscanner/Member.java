package ch.amiv.legiscanner.amivlegiscanner;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Roger on 06-Feb-18.
 * This class stores the data that an individual member has
 */

public class Member {
    public String serverId;
    public boolean checkedIn;
    public String email;
    public String firstname;
    public String lastname;
    public String legi;
    public String membership;   //as in membership type: ordinary extraordinary honorary
    public String nethz;

    public Member (JSONObject _member)
    {
        try {
            serverId    = _member.getString("_id");
            checkedIn   = _member.getBoolean("checked_in");
            email       = _member.getString("email");
            firstname   = _member.getString("firstname");
            lastname    = _member.getString("lastname");
            legi        = _member.getString("legi");
            membership  = _member.getString("membership");
            nethz       = _member.getString("nethz");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public Member (String _serverId, boolean _checkedIn, String _email, String _firstname, String _lastname, String _legi, String _membership, String _nethz)
    {
        serverId    = _serverId;
        checkedIn   = _checkedIn;
        email       = _email;
        firstname   = _firstname;
        lastname    = _lastname;
        legi        = _legi;
        membership  = _membership;
        nethz       = _nethz;
    }
}
