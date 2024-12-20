package main.game.events;

import main.Logger;
import main.Messages;

public class Events
{
    private final static String TAG = "Events";
    private EventObject[] mEvents;
    private int mIndex;
    public Events(){
        mEvents = new EventObject[60];
        mIndex = 0;
    }

    public void addEvent(EventObject go){
        mEvents[mIndex] = go;
        ++mIndex;
    }

    public EventObject getEvent(int idx){
        if(idx>mIndex)
        {
            Logger.log(Logger.INFO,TAG, Messages.eTextUnknownEvent.concat(Integer.toString(idx)));
            return null;
        }
        return mEvents[idx];
    }

    public boolean removeEvent(EventObject go)
    {
        int j = 0;
        for(int i=0; i<mIndex; ++i)
        {
            mEvents[j] = mEvents[i];
            if(mEvents[i] != go)
            {
                j+=1;
            }
        }

        mIndex = j;
        return false;
    }

    public int getCount(){
        return mIndex;
    }

    public void resetEvents(){
        mIndex= 0;
    }
}
