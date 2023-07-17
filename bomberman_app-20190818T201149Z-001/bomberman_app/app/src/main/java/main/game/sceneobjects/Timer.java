package main.game.sceneobjects;

import static main.game.SceneManager.SOBJ_TIMER;

import main.game.SceneElement;

public class Timer extends SceneElement
{
    public int mTime;
    public int mNewTime;
    public Timer(int id,  int posx, int posy, int subtype) {
        super(SOBJ_TIMER, id);
        mObjectSubtype = subtype;
        mPositionX = posx;
        mPositionY = posy;
        mTime = 0;
        mAdditionals = new int[4];
    }

    @Override
    public void updateState(long dt, int[] input)
    {
        if(mNewTime != mTime)
        {
            mTime = mNewTime;
            int t = mTime;

            /* Secs */
            mAdditionals[3] = t%10;
            t /=10;

            /* 10'secs*/
            mAdditionals[2] = t%6;
            t /=6;

            /* Mins */
            mAdditionals[1] = t%10;
            t /=6;

            /* 10's Mins*/
            mAdditionals[0] = t%10;

            /* Needs redraw*/
            mPreviousState = mState;
            mState = ~mState;
        }
    }

    public void setTime(int time)
    {
        mNewTime = time;
    }
}

