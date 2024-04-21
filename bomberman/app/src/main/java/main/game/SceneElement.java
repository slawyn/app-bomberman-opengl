package main.game;


import main.game.events.EventObject;
import main.rendering.elements.RenderElement;

public abstract class SceneElement extends EventObject
{
    public int mObjectID;
    public int mObjectType;
    public int mObjectSubtype;
    public int mPreviousState;
    public int mState;
    public int mPositionX;
    public int mPositionY;
    public int[] mAdditionals;
    public int mLayer;
    public RenderElement ro;
    public int mAction;

    public SceneElement(int objtype, int objid, int objsubtype)
    {
        mObjectID = objid;
        mObjectType = objtype;
        mObjectSubtype = objsubtype;
        mState = -1;
        mPreviousState = -1;
        mAction = -1;
    }

    public boolean updateState(long dt, int[] input) {
        return false;
    };

    public int[] getUpdatedAdditionals()
    {
        return mAdditionals;
    }
}
