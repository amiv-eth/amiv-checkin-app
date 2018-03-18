package ch.amiv.checkin;

import android.annotation.TargetApi;
import android.support.annotation.RequiresApi;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Handler;

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
    public Comparator<Member> memberComparator;
    public enum MemberComparator {None, Name, Membership, Status, Legi}
    private MemberComparator currentSorting = MemberComparator.None;
    private boolean invertSorting = false;

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

        SortMembers();
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

    public void SetMemberSortingType(Comparator<Member> comparator)
    {
        memberComparator = comparator;
        SortMembers();
    }

    public void SetMemberSortingType(final MemberComparator sortingType)
    {
        if(sortingType == MemberComparator.None)
                return;

        if(currentSorting == sortingType)
            invertSorting = !invertSorting;
        else
            invertSorting = false;
        currentSorting = sortingType;

        Comparator<Member> comparator;
        comparator = new Comparator<Member>() {
            @Override
            public int compare(Member a, Member b) {
                switch (sortingType)
                {
                    case Name:
                        return a.firstname.compareTo(b.firstname);
                    case Membership:
                        return a.membership.compareTo(b.membership);
                    case Status:
                        return (b.checkedIn == a.checkedIn ? 0 : (a.checkedIn ? 1 : -1));
                    case Legi:
                        return a.legi.compareTo(b.legi);
                }
                return 0;
            }
        };

        if(invertSorting && android.os.Build.VERSION.SDK_INT >= 24)
            SetMemberSortingType(comparator.reversed());
        else
            SetMemberSortingType(comparator);
    }

    /**
     * Way of sorting the array of members using a custom defined comparator
     */
    private void SortMembers()
    {
        if(memberComparator == null)
            return;

        Collections.sort(members, memberComparator);
    }
}
