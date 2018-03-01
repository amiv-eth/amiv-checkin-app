package ch.amiv.checkin;

import android.util.Log;

/**
 * This stores all event data received by /get_event_data get request. It can be accessed by the static instance. The instance should be deleted when a new session is started
 * Created by Roger on 28-Feb-18.
 */

public class EventData {
    public int serverId = 0;
    public enum EventType {None, Event, PVK, GV}
    public EventType eventType = EventType.None;
    public String name = "";
    public String description;
    public int signupCount;
    public int spots;
    public String startTime = "";

    public boolean Update(int _serverId, String _eventType, String _name, String _description, int _signupCount, int _spots, String _startTime)
    {
        serverId = _serverId;
        name= _name;
        description = _description;
        signupCount = _signupCount;
        spots = _spots;
        startTime = _startTime;

        return SetEventType(_eventType);
    }

    public boolean SetEventType(String s)
    {
        if(s.equalsIgnoreCase("AMIV Events"))
            eventType = EventType.Event;
        else if(s.equalsIgnoreCase("AMIV PVK"))   //May need to be adjusted to what is given
            eventType = EventType.PVK;
        else if(s.equalsIgnoreCase("AMIV General Assemblies"))
            eventType = EventType.GV;
        else {
            eventType = EventType.None;
            return false;
        }

        Log.e("asd", "parsing event type successful!");
        return true;
    }
}
