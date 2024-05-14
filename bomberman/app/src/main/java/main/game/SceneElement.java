package main.game;


import main.game.events.EventObject;
import main.rendering.elements.RenderElement;

public abstract class SceneElement extends EventObject
{   
    protected int mState;
    private int mObjectID;
    private int mObjectType;
    private int mObjectSubtype;
    public int mPreviousState;
    public int mPositionX;
    public int mPositionY;
    public int[] mAdditionals;
    public int mLayer;
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

    @Override
    public int getState()
    {
        return mState;
    }

    @Override
    public int getId()
    {
        return mObjectID;
    }

    @Override
    public int getSubtype()
    {
        return mObjectSubtype;
    }

    @Override
    public int getType()
    {
        return mObjectType;
    }

    public int getZ()
    {
        return 0;
    }

    public int getLayer()
    {
        return mLayer;
    }
}
