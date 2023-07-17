package main.game;


import main.game.events.EventObject;
import main.rendering.elements.RenderElement;

public class SceneElement extends EventObject
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

    public SceneElement(int objtype, int objid)
    {
        mObjectID = objid;
        mObjectType = objtype;
        mState = -1;
        mPreviousState = -1;
    }

    public void updateState(long dt, int[] input){};

    public int[] getUpdatedAdditionals()
    {
        return mAdditionals;
    }
}